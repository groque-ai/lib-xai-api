package com.xai.api.responses.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.reasoning.Reasoning;
import com.xai.api.responses.output.tool.CodeInterpreterCall;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.output.web.WebSearchCall;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ResponseStreamEventUnmarshalTest {

  private ObjectMapper mapper;
  private ResponseSseParser parser;

  @Before
  public void setUp() {
    mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    parser = new ResponseSseParser(mapper);
  }

  @Test
  public void unmarshalsTextDelta() {
    ResponseStreamEvent event = parseOne(
      "event: response.output_text.delta\n"
      + "data: {\"type\":\"response.output_text.delta\",\"delta\":\"pong\",\"item_id\":\"msg_1\",\"output_index\":1,\"content_index\":0,\"sequence_number\":23}\n"
      + "\n");
    assertEquals(ResponseEventType.RESPONSE_OUTPUT_TEXT_DELTA, event.getEvent());
    assertEquals("pong", event.getDelta());
    assertEquals("msg_1", event.getItemId());
    assertEquals(Integer.valueOf(1), event.getOutputIndex());
    assertEquals(Integer.valueOf(23), event.getSequenceNumber());
  }

  @Test
  public void unmarshalsCreatedResponse() {
    ResponseStreamEvent event = parseOne(
      "event: response.created\n"
      + "data: {\"type\":\"response.created\",\"sequence_number\":0,\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"model\":\"grok-4.6\",\"status\":\"in_progress\"}}\n"
      + "\n");
    assertNotNull(event.getResponse());
    assertEquals("resp_1", event.getResponse().getId());
    assertEquals("grok-4.6", event.getResponse().getModel());
  }

  @Test
  public void unmarshalsOutputItemTypes() {
    ResponseStreamEvent reasoning = parseOne(
      "data: {\"type\":\"response.output_item.added\",\"output_index\":0,\"item\":{\"id\":\"rs_1\",\"type\":\"reasoning\",\"status\":\"in_progress\",\"summary\":[]}}\n\n");
    assertTrue(reasoning.getItem() instanceof Reasoning);

    ResponseStreamEvent message = parseOne(
      "data: {\"type\":\"response.output_item.added\",\"output_index\":1,\"item\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"status\":\"in_progress\",\"content\":[]}}\n\n");
    assertTrue(message.getItem() instanceof OutputMessage);

    ResponseStreamEvent fn = parseOne(
      "data: {\"type\":\"response.output_item.added\",\"output_index\":1,\"item\":{\"id\":\"fc_1\",\"type\":\"function_call\",\"call_id\":\"call-1\",\"name\":\"get_weather\",\"arguments\":\"\",\"status\":\"in_progress\"}}\n\n");
    assertTrue(fn.getItem() instanceof FunctionToolCall);
    assertEquals("get_weather", ((FunctionToolCall) fn.getItem()).getName());

    ResponseStreamEvent web = parseOne(
      "data: {\"type\":\"response.output_item.added\",\"output_index\":1,\"item\":{\"id\":\"ws_1\",\"type\":\"web_search_call\",\"status\":\"in_progress\",\"action\":{\"type\":\"search\",\"query\":\"\",\"sources\":[]}}}\n\n");
    assertTrue(web.getItem() instanceof WebSearchCall);

    ResponseStreamEvent code = parseOne(
      "data: {\"type\":\"response.output_item.added\",\"output_index\":1,\"item\":{\"id\":\"ci_1\",\"type\":\"code_interpreter_call\",\"status\":\"in_progress\",\"code\":\"\",\"outputs\":[]}}\n\n");
    assertTrue(code.getItem() instanceof CodeInterpreterCall);
  }

  @Test
  public void unmarshalsFunctionArgumentsAndAnnotation() {
    ResponseStreamEvent args = parseOne(
      "data: {\"type\":\"response.function_call_arguments.done\",\"arguments\":\"{\\\"city\\\":\\\"Paris\\\"}\",\"item_id\":\"fc_1\",\"name\":\"get_weather\",\"output_index\":1}\n\n");
    assertEquals("get_weather", args.getName());
    assertEquals("{\"city\":\"Paris\"}", args.getArguments());

    ResponseStreamEvent ann = parseOne(
      "data: {\"type\":\"response.output_text.annotation.added\",\"annotation\":{\"type\":\"url_citation\",\"url\":\"https://example.com\",\"start_index\":0,\"end_index\":0,\"title\":\"Example\"},\"annotation_index\":0,\"item_id\":\"msg_1\",\"output_index\":7,\"content_index\":0}\n\n");
    assertEquals(ResponseEventType.RESPONSE_OUTPUT_TEXT_ANNOTATION_DOT_ADDED, ann.getEvent());
    assertNotNull(ann.getAnnotation());
    assertEquals("https://example.com", ann.getAnnotation().getUrl());
  }

  @Test
  public void unmarshalsLiveCaptureFilesIfPresent() throws Exception {
    Path captures = Paths.get("docs", "superpowers", "streaming-captures");
    if (!Files.isDirectory(captures)) {
      return;
    }
    List<String> failures = new ArrayList<>();
    int events = 0;
    int files = 0;
    try (DirectoryStream<Path> dirs = Files.newDirectoryStream(captures)) {
      for (Path dir : dirs) {
        Path sse = dir.resolve("response.sse");
        if (!Files.isRegularFile(sse)) {
          continue;
        }
        files++;
        ResponseSseParser local = new ResponseSseParser(mapper);
        for (String line : Files.readAllLines(sse)) {
          ResponseSseParser.Result result = local.consumeLine(line);
          events += countParsed(dir, result, failures);
        }
        events += countParsed(dir, local.finish(), failures);
      }
    }
    if (files == 0) {
      return;
    }
    if (!failures.isEmpty()) {
      fail(events + " events, failures: " + failures);
    }
    assertTrue("expected events from raw response.sse", events > 0);
  }

  private static int countParsed(Path dir, ResponseSseParser.Result result, List<String> failures) {
    if (result.getKind() != ResponseSseParser.Kind.EVENT) {
      return 0;
    }
    ResponseStreamEvent event = result.getEvent();
    if (event.getEvent() == ResponseEventType.UNKNOWN) {
      failures.add(dir.getFileName() + " unknown type=" + event.getType());
    }
    if (ResponseEventType.RESPONSE_OUTPUT_TEXT_DELTA.equals(event.getEvent()) && event.getDelta() == null) {
      failures.add(dir.getFileName() + " missing delta");
    }
    if (ResponseEventType.RESPONSE_CREATED.equals(event.getEvent())
        && (event.getResponse() == null || event.getResponse().getId() == null)) {
      failures.add(dir.getFileName() + " missing created response.id");
    }
    return 1;
  }

  private ResponseStreamEvent parseOne(String sse) {
    ResponseSseParser local = new ResponseSseParser(mapper);
    ResponseStreamEvent found = null;
    String[] lines = sse.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      ResponseSseParser.Result result = local.consumeLine(line);
      if (result.getKind() == ResponseSseParser.Kind.EVENT) {
        found = result.getEvent();
      }
    }
    if (found == null) {
      ResponseSseParser.Result end = local.finish();
      found = end.getEvent();
    }
    assertNotNull(found);
    return found;
  }
}
