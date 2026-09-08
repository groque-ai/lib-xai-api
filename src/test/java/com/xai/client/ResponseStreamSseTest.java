package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.responses.stream.ResponseEventType;
import com.xai.api.responses.stream.dto.OutputTextDeltaEvent;
import com.xai.api.responses.stream.dto.SnapshotEvent;
import com.xai.api.responses.stream.dto.StreamEvent;
import com.xai.api.responses.stream.dto.UnknownStreamEvent;
import com.xai.client.exception.ApiParseException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * SSE framing now lives on the stream handle; frames unmarshal to StreamEvent.
 */
public class ResponseStreamSseTest {

  private ObjectMapper mapper;
  private ResponseStreamPumpTest.RecordingListener listener;

  @Before
  public void setUp() {
    mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    listener = new ResponseStreamPumpTest.RecordingListener();
  }

  @Test
  public void parsesTypedEventsAndDone() {
    feed(""
      + "event: response.created\n"
      + "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\"}}\n"
      + "\n"
      + ": keep-alive\n"
      + "\n"
      + "event: response.output_text.delta\n"
      + "data: {\"type\":\"response.output_text.delta\",\"delta\":\"Hi\"}\n"
      + "\n"
      + "data: [DONE]\n"
      + "\n");
    List<StreamEvent> events = listener.events;
    assertEquals(2, events.size());
    assertTrue(events.get(0) instanceof SnapshotEvent);
    assertSame(ResponseEventType.RESPONSE_CREATED, events.get(0).getEvent());
    assertEquals("resp_1", ((SnapshotEvent) events.get(0)).getResponse().getId());
    assertTrue(events.get(1) instanceof OutputTextDeltaEvent);
    assertEquals("Hi", ((OutputTextDeltaEvent) events.get(1)).getDelta());
    assertEquals(1, listener.completes);
  }

  @Test
  public void prefersJsonTypeOverSseEventField() {
    feed(""
      + "event: ignored\n"
      + "data: {\"type\":\"response.completed\"}\n"
      + "\n");
    assertEquals(1, listener.events.size());
    assertSame(ResponseEventType.RESPONSE_COMPLETED, listener.events.get(0).getEvent());
  }

  @Test
  public void usesSseEventWhenJsonHasNoType() {
    feed(""
      + "event: response.failed\n"
      + "data: {\"reason\":\"boom\"}\n"
      + "\n");
    assertEquals(1, listener.events.size());
    assertTrue(listener.events.get(0) instanceof SnapshotEvent);
    assertSame(ResponseEventType.RESPONSE_FAILED, listener.events.get(0).getEvent());
    assertEquals("response.failed", listener.events.get(0).getType());
  }

  @Test
  public void unknownTypeStillDelivered() {
    feed(""
      + "data: {\"type\":\"response.custom.future\",\"x\":1}\n"
      + "\n");
    assertEquals(1, listener.events.size());
    assertTrue(listener.events.get(0) instanceof UnknownStreamEvent);
    assertSame(ResponseEventType.UNKNOWN, listener.events.get(0).getEvent());
    assertEquals("response.custom.future", listener.events.get(0).getType());
  }

  @Test
  public void doneIsNotAnEvent() {
    feed("data: [DONE]\n\n");
    assertTrue(listener.events.isEmpty());
    assertEquals(1, listener.completes);
  }

  @Test
  public void badJsonIsParseException() {
    feed("data: {not-json\n\n");
    assertEquals(1, listener.errors.size());
    assertTrue(listener.errors.get(0) instanceof ApiParseException);
    assertEquals(0, listener.completes);
  }

  @Test
  public void emptyBodyCompletesWithNoEvents() {
    feed("");
    assertTrue(listener.events.isEmpty());
    assertEquals(1, listener.completes);
  }

  private void feed(String document) {
    ResponseStreamHandleImpl handle = new ResponseStreamHandleImpl(listener);
    handle.attachBody(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
    handle.readLoop(mapper);
  }
}
