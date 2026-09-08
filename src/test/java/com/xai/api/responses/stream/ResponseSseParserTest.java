package com.xai.api.responses.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.client.exception.ApiParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ResponseSseParserTest {

  private ResponseSseParser parser;

  @Before
  public void setUp() {
    parser = new ResponseSseParser(new ObjectMapper());
  }

  private List<ResponseStreamEvent> feed(String document) {
    List<ResponseStreamEvent> events = new ArrayList<>();
    String[] lines = document.split("\n", -1);
    boolean done = false;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.endsWith("\r")) {
        line = line.substring(0, line.length() - 1);
      }
      ResponseSseParser.Result result = parser.consumeLine(line);
      if (result.getKind() == ResponseSseParser.Kind.EVENT) {
        events.add(result.getEvent());
      } else if (result.getKind() == ResponseSseParser.Kind.DONE) {
        done = true;
        break;
      }
    }
    if (!done) {
      ResponseSseParser.Result end = parser.finish();
      if (end.getKind() == ResponseSseParser.Kind.EVENT) {
        events.add(end.getEvent());
      }
    }
    return events;
  }

  @Test
  public void parsesTypedEventsAndDone() {
    String sse = ""
      + "event: response.created\n"
      + "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\"}}\n"
      + "\n"
      + ": keep-alive\n"
      + "\n"
      + "event: response.output_text.delta\n"
      + "data: {\"type\":\"response.output_text.delta\",\"delta\":\"Hi\"}\n"
      + "\n"
      + "data: [DONE]\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(2, events.size());
    assertSame(ResponseEventType.RESPONSE_CREATED, events.get(0).getEvent());
    assertEquals("response.created", events.get(0).getType());
    assertEquals("resp_1", events.get(0).getData().get("response").get("id").asText());
    assertEquals("resp_1", events.get(0).getResponse().getId());
    assertSame(ResponseEventType.RESPONSE_OUTPUT_TEXT_DELTA, events.get(1).getEvent());
    assertEquals("Hi", events.get(1).getData().get("delta").asText());
    assertEquals("Hi", events.get(1).getDelta());
  }

  @Test
  public void prefersJsonTypeOverSseEventField() {
    String sse = ""
      + "event: ignored\n"
      + "data: {\"type\":\"response.completed\"}\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(1, events.size());
    assertSame(ResponseEventType.RESPONSE_COMPLETED, events.get(0).getEvent());
    assertEquals("response.completed", events.get(0).getType());
  }

  @Test
  public void usesSseEventWhenJsonHasNoType() {
    String sse = ""
      + "event: response.failed\n"
      + "data: {\"reason\":\"boom\"}\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(1, events.size());
    assertSame(ResponseEventType.RESPONSE_FAILED, events.get(0).getEvent());
    assertEquals("response.failed", events.get(0).getType());
  }

  @Test
  public void unknownTypeStillDelivered() {
    String sse = ""
      + "data: {\"type\":\"response.custom.future\",\"x\":1}\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(1, events.size());
    assertSame(ResponseEventType.UNKNOWN, events.get(0).getEvent());
    assertEquals("response.custom.future", events.get(0).getType());
    assertEquals(1, events.get(0).getData().get("x").asInt());
  }

  @Test
  public void doneIsNotAnEnvelope() {
    ResponseSseParser.Result result = parser.consumeLine("data: [DONE]");
    assertEquals(ResponseSseParser.Kind.NONE, result.getKind());
    result = parser.consumeLine("");
    assertEquals(ResponseSseParser.Kind.DONE, result.getKind());
    assertNull(result.getEvent());
  }

  @Test(expected = ApiParseException.class)
  public void badJsonIsParseException() {
    parser.consumeLine("data: {not-json");
    parser.consumeLine("");
  }

  @Test
  public void finishWithoutDoneYieldsNoneWhenBufferEmpty() {
    ResponseSseParser.Result result = parser.finish();
    assertEquals(ResponseSseParser.Kind.NONE, result.getKind());
  }
}
