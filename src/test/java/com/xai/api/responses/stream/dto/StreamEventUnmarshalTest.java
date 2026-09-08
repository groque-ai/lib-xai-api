package com.xai.api.responses.stream.dto;

import com.xai.api.responses.stream.OutputTextDeltaEvent;
import com.xai.api.responses.stream.ReasoningSummaryPartEvent;
import com.xai.api.responses.stream.UnknownStreamEvent;
import com.xai.api.responses.stream.IndexedDeltaEvent;
import com.xai.api.responses.stream.ErrorEvent;
import com.xai.api.responses.stream.OutputTextDoneEvent;
import com.xai.api.responses.stream.StreamEvent;
import com.xai.api.responses.stream.ContentPartEvent;
import com.xai.api.responses.stream.SnapshotEvent;
import com.xai.api.responses.stream.FunctionCallArgumentsDoneEvent;
import com.xai.api.responses.stream.CodeInterpreterCodeDoneEvent;
import com.xai.api.responses.stream.OutputTextAnnotationEvent;
import com.xai.api.responses.stream.OutputItemEvent;
import com.xai.api.responses.stream.ToolPhaseEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.responses.config.ReasoningConfiguration;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.message.OutputMessageContentText;
import com.xai.api.responses.output.reasoning.Reasoning;
import com.xai.api.responses.output.reasoning.ReasoningText;
import com.xai.api.responses.output.tokens.Annotation;
import com.xai.api.responses.output.tokens.TokenLogProb;
import com.xai.api.responses.output.tool.CodeInterpreterCall;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.output.web.WebSearchCall;
import com.xai.api.type.StreamEventType;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Jackson family mapping for Responses SSE {@code data:} JSON.
 */
public class StreamEventUnmarshalTest {

  private ObjectMapper mapper;

  @Before
  public void setUp() {
    mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
  }

  @Test
  public void snapshotCreatedIsSnapshotEvent() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.created\",\"sequence_number\":0,"
      + "\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"model\":\"grok-4.6\","
      + "\"status\":\"in_progress\",\"reasoning\":{\"effort\":\"low\",\"summary\":\"detailed\"},"
      + "\"text\":{\"format\":{\"type\":\"text\"}}}}");
    assertTrue(event instanceof SnapshotEvent);
    SnapshotEvent snapshot = (SnapshotEvent) event;
    assertEquals(Integer.valueOf(0), snapshot.getSequenceNumber());
    assertEquals(StreamEventType.RESPONSE_CREATED, snapshot.getEvent());
    assertNotNull(snapshot.getResponse());
    assertEquals("resp_1", snapshot.getResponse().getId());
    assertTrue(snapshot.getResponse().getReasoning() instanceof ReasoningConfiguration);
    assertEquals("low", snapshot.getResponse().getReasoning().getEffort().name());
    assertNotNull(snapshot.getResponse().getText());
  }

  @Test
  public void outputItemFunctionCallUsesFunctionToolCall() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.output_item.done\",\"sequence_number\":30,\"output_index\":1,"
      + "\"item\":{\"id\":\"fc_1\",\"type\":\"function_call\",\"status\":\"completed\","
      + "\"name\":\"get_weather\",\"call_id\":\"call-1\",\"arguments\":\"{\\\"city\\\":\\\"Paris\\\"}\"}}");
    assertTrue(event instanceof OutputItemEvent);
    OutputItemEvent itemEvent = (OutputItemEvent) event;
    assertTrue(itemEvent.getItem() instanceof FunctionToolCall);
    FunctionToolCall call = (FunctionToolCall) itemEvent.getItem();
    assertEquals("get_weather", call.getName());
    assertEquals("{\"city\":\"Paris\"}", call.getArguments());
  }

  @Test
  public void outputItemMessageCarriesLogprobsList() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.output_item.done\",\"sequence_number\":70,\"output_index\":1,"
      + "\"item\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"status\":\"completed\","
      + "\"content\":[{\"type\":\"output_text\",\"text\":\"pong\",\"logprobs\":[],\"annotations\":[]}]}}");
    OutputItemEvent itemEvent = (OutputItemEvent) event;
    assertTrue(itemEvent.getItem() instanceof OutputMessage);
    OutputMessage message = (OutputMessage) itemEvent.getItem();
    assertTrue(message.getContent().get(0) instanceof OutputMessageContentText);
    List<TokenLogProb> logprobs = ((OutputMessageContentText) message.getContent().get(0)).getLogprobs();
    assertNotNull(logprobs);
    assertEquals(0, logprobs.size());
  }

  @Test
  public void contentPartUsesOutputMessageContent() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.content_part.added\",\"sequence_number\":39,\"output_index\":1,"
      + "\"item_id\":\"msg_1\",\"content_index\":0,"
      + "\"part\":{\"type\":\"output_text\",\"text\":\"\",\"logprobs\":[],\"annotations\":[]}}");
    assertTrue(event instanceof ContentPartEvent);
    ContentPartEvent partEvent = (ContentPartEvent) event;
    assertTrue(partEvent.getPart() instanceof OutputMessageContentText);
    assertEquals(Integer.valueOf(0), partEvent.getContentIndex());
  }

  @Test
  public void reasoningSummaryPartUsesReasoningText() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.reasoning_summary_part.done\",\"sequence_number\":36,\"output_index\":0,"
      + "\"item_id\":\"rs_1\",\"summary_index\":0,"
      + "\"part\":{\"type\":\"summary_text\",\"text\":\"thinking\"}}");
    assertTrue(event instanceof ReasoningSummaryPartEvent);
    ReasoningText part = ((ReasoningSummaryPartEvent) event).getPart();
    assertEquals("summary_text", part.getType());
    assertEquals("thinking", part.getText());
  }

  @Test
  public void outputTextDeltaAndDone() throws Exception {
    StreamEvent delta = read(
      "{\"type\":\"response.output_text.delta\",\"sequence_number\":40,\"output_index\":1,"
      + "\"item_id\":\"msg_1\",\"content_index\":0,\"delta\":\"pong\",\"logprobs\":[]}");
    assertTrue(delta instanceof OutputTextDeltaEvent);
    assertEquals("pong", ((OutputTextDeltaEvent) delta).getDelta());
    assertNotNull(((OutputTextDeltaEvent) delta).getLogprobs());

    StreamEvent done = read(
      "{\"type\":\"response.output_text.done\",\"sequence_number\":68,\"output_index\":1,"
      + "\"item_id\":\"msg_1\",\"content_index\":0,\"text\":\"pong\",\"logprobs\":[]}");
    assertTrue(done instanceof OutputTextDoneEvent);
    assertEquals("pong", ((OutputTextDoneEvent) done).getText());
  }

  @Test
  public void annotationAdded() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.output_text.annotation.added\",\"sequence_number\":72,"
      + "\"output_index\":5,\"item_id\":\"msg_1\",\"content_index\":0,\"annotation_index\":0,"
      + "\"annotation\":{\"type\":\"url_citation\",\"url\":\"https://example.com\","
      + "\"start_index\":0,\"end_index\":0,\"title\":\"Example\"}}");
    assertTrue(event instanceof OutputTextAnnotationEvent);
    Annotation annotation = ((OutputTextAnnotationEvent) event).getAnnotation();
    assertEquals("https://example.com", annotation.getUrl());
    assertEquals(StreamEventType.RESPONSE_OUTPUT_TEXT_ANNOTATION_DOT_ADDED, event.getEvent());
  }

  @Test
  public void functionAndCodeDeltasShareIndexedDeltaEvent() throws Exception {
    StreamEvent args = read(
      "{\"type\":\"response.function_call_arguments.delta\",\"sequence_number\":28,"
      + "\"output_index\":1,\"item_id\":\"fc_1\",\"delta\":\"{\\\"city\\\":\\\"Paris\\\"}\"}");
    assertTrue(args instanceof IndexedDeltaEvent);
    assertEquals("{\"city\":\"Paris\"}", ((IndexedDeltaEvent) args).getDelta());

    StreamEvent code = read(
      "{\"type\":\"response.code_interpreter_call_code.delta\",\"sequence_number\":30,"
      + "\"output_index\":1,\"item_id\":\"ci_1\",\"delta\":\"print(17*19)\"}");
    assertTrue(code instanceof IndexedDeltaEvent);
    assertEquals("print(17*19)", ((IndexedDeltaEvent) code).getDelta());
  }

  @Test
  public void functionArgsDoneAndCodeDoneAreSeparateFamilies() throws Exception {
    StreamEvent args = read(
      "{\"type\":\"response.function_call_arguments.done\",\"sequence_number\":29,"
      + "\"output_index\":1,\"item_id\":\"fc_1\",\"name\":\"get_weather\","
      + "\"arguments\":\"{\\\"city\\\":\\\"Paris\\\"}\"}");
    assertTrue(args instanceof FunctionCallArgumentsDoneEvent);
    FunctionCallArgumentsDoneEvent done = (FunctionCallArgumentsDoneEvent) args;
    assertEquals("get_weather", done.getName());
    assertEquals("{\"city\":\"Paris\"}", done.getArguments());

    StreamEvent code = read(
      "{\"type\":\"response.code_interpreter_call_code.done\",\"sequence_number\":31,"
      + "\"output_index\":1,\"item_id\":\"ci_1\",\"code\":\"print(17*19)\"}");
    assertTrue(code instanceof CodeInterpreterCodeDoneEvent);
    assertEquals("print(17*19)", ((CodeInterpreterCodeDoneEvent) code).getCode());
  }

  @Test
  public void toolPhaseEventsShareClass() throws Exception {
    StreamEvent searching = read(
      "{\"type\":\"response.web_search_call.searching\",\"sequence_number\":35,"
      + "\"output_index\":1,\"item_id\":\"ws_1\"}");
    assertTrue(searching instanceof ToolPhaseEvent);
    assertEquals(StreamEventType.RESPONSE_WEB_SEARCH_CALL_SEARCHING, searching.getEvent());
  }

  @Test
  public void errorEventKeepsRawErrorNode() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"error\",\"status\":400,\"error\":{\"code\":\"invalid\",\"message\":\"nope\",\"param\":\"input\"}}");
    assertTrue(event instanceof ErrorEvent);
    ErrorEvent error = (ErrorEvent) event;
    assertEquals(Integer.valueOf(400), error.getStatus());
    assertEquals("invalid", error.getError().get("code").asText());
  }

  @Test
  public void unknownTypeIsUnknownStreamEvent() throws Exception {
    StreamEvent event = read(
      "{\"type\":\"response.image_generation_call.generating\",\"sequence_number\":9,\"item_id\":\"ig_1\"}");
    assertTrue(event instanceof UnknownStreamEvent);
    assertEquals(StreamEventType.RESPONSE_IMAGE_GENERATION_CALL_GENERATING, event.getEvent());
  }

  @Test
  public void liveCapturesMapToKnownFamilies() throws Exception {
    Path captures = Paths.get("docs", "superpowers", "streaming-captures");
    if (!Files.isDirectory(captures)) {
      return;
    }
    List<String> failures = new ArrayList<>();
    int events = 0;
    int files = 0;
    try (DirectoryStream<Path> dirs = Files.newDirectoryStream(captures)) {
      for (Path dir : dirs) {
        if (!Files.isDirectory(dir)) {
          continue;
        }
        try (DirectoryStream<Path> sseFiles = Files.newDirectoryStream(dir, "*.sse")) {
          for (Path sse : sseFiles) {
            files++;
            for (String line : Files.readAllLines(sse)) {
              if (!line.startsWith("data:")) {
                continue;
              }
              String payload = line.substring(5).trim();
              if (payload.isEmpty() || "[DONE]".equals(payload)) {
                continue;
              }
              StreamEvent event = mapper.readValue(payload, StreamEvent.class);
              events++;
              if (event instanceof UnknownStreamEvent) {
                failures.add(dir.getFileName() + " unknown type=" + event.getType());
              }
              checkCaptureItem(dir, event, failures);
            }
          }
        }
      }
    }
    if (files == 0) {
      return;
    }
    if (!failures.isEmpty()) {
      fail(events + " events, failures: " + failures);
    }
    assertTrue("expected events from capture sse files", events > 0);
  }

  private static void checkCaptureItem(Path dir, StreamEvent event, List<String> failures) {
    if (!(event instanceof OutputItemEvent)) {
      return;
    }
    OutputItemEvent itemEvent = (OutputItemEvent) event;
    if (itemEvent.getItem() instanceof FunctionToolCall) {
      return;
    }
    if (itemEvent.getItem() instanceof OutputMessage) {
      return;
    }
    if (itemEvent.getItem() instanceof Reasoning) {
      return;
    }
    if (itemEvent.getItem() instanceof WebSearchCall) {
      return;
    }
    if (itemEvent.getItem() instanceof CodeInterpreterCall) {
      return;
    }
    failures.add(dir.getFileName() + " unexpected item " + itemEvent.getItem());
  }

  private StreamEvent read(String json) throws Exception {
    return mapper.readValue(json, StreamEvent.class);
  }
}
