package com.llamacpp.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.llamacpp.client.model.ModelInfo;
import com.llamacpp.client.model.ModelListResponse;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.input.part.ModelInputPartMessage;
import com.xai.api.responses.stream.OutputTextDeltaEvent;
import com.xai.api.responses.stream.OutputTextDoneEvent;
import com.xai.api.responses.stream.StreamEvent;
import com.xai.api.type.Role;
import com.xai.api.type.StreamEventType;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;
import com.xai.client.ResponseStreamPumpTest.RecordingListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Live llama.cpp work: introduce, write a class, document a class. Blocking
 * and streaming. Artifacts land in {@code target/test/llamacpp}.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public class LlamaResponsesClientWorkLiveTest {

  private static final String BASE_URL = "http://gpu-a.mcl.keybridge.ch:8080";
  private static final String MODEL = "phi-4-mini";
  private static final long WAIT_SECONDS = 300;
  private static final ObjectMapper MAPPER = new ObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private static Path artifacts;

  private LlamaResponsesClient client;

  @BeforeClass
  public static void prepareArtifacts() throws Exception {
    artifacts = Paths.get("target", "test", "llamacpp");
    Files.createDirectories(artifacts);
  }

  @Before
  public void setUp() {
    LlamaClientConfig config = new LlamaClientConfig.Builder()
      .withBaseUrl(BASE_URL)
      .withConnectTimeout(Duration.ofSeconds(30))
      .withRequestTimeout(Duration.ofSeconds(WAIT_SECONDS))
      .build();
    client = new LlamaResponsesClient(config);
  }

  @Test
  public void catalogModels() throws Exception {
    ModelListResponse list = client.getModels();
    assertNotNull(list);
    MAPPER.writeValue(artifacts.resolve("models.json").toFile(), list);
    StringBuilder catalog = new StringBuilder();
    catalog.append("# llama.cpp models (live)\n");
    if (list.getData() != null) {
      for (ModelInfo info : list.getData()) {
        catalog.append("- ").append(info.getId());
        if (info.getStatus() != null) {
          catalog.append(" status=").append(info.getStatus().getValue());
        }
        catalog.append('\n');
      }
    }
    Files.writeString(artifacts.resolve("models.txt"), catalog.toString());
  }

  @Test
  public void introduceYourselfBlocking() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addSystemMessage("You are a helpful assistant running locally via llama.cpp.")
      .addUserMessage("Introduce yourself in two short paragraphs. Say you are phi-4-mini "
        + "served by llama.cpp, and mention that this Java client talks to you over "
        + "POST /v1/responses.")
      .build();
    request.setMaxOutputTokens(400);
    ModelResponse response = client.generate(request);
    String text = requireText(response);
    MAPPER.writeValue(artifacts.resolve("introduce-response.json").toFile(), response);
    Files.writeString(artifacts.resolve("introduce.txt"), text);
    System.out.println("INTRODUCE blocking chars=" + text.length());
  }

  @Test
  public void writeJavaBlocking() throws Exception {
    ModelRequest request = writeJavaRequest();
    ModelResponse response = client.generate(request);
    String source = extractJavaSource(requireText(response));
    MAPPER.writeValue(artifacts.resolve("write-java-response.json").toFile(), response);
    Files.writeString(artifacts.resolve("FizzBuzz.java"), source);
    assertTrue("expected class FizzBuzz in " + source, source.contains("class FizzBuzz"));
    System.out.println("FIZZBUZZ blocking chars=" + source.length());
  }

  @Test
  public void addJavadocBlocking() throws Exception {
    ModelRequest request = javadocRequest();
    ModelResponse response = client.generate(request);
    String source = extractJavaSource(requireText(response));
    MAPPER.writeValue(artifacts.resolve("javadoc-response.json").toFile(), response);
    Files.writeString(artifacts.resolve("Foo.java"), source);
    assertTrue("expected class Foo in " + source, source.contains("class Foo"));
    assertTrue("expected javadoc in Foo.java", source.contains("/**"));
    System.out.println("FOO blocking chars=" + source.length());
  }

  @Test
  public void introduceYourselfStreaming() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addSystemMessage("You are a helpful assistant running locally via llama.cpp.")
      .addUserMessage("Introduce yourself in two short paragraphs. Say you are phi-4-mini "
        + "served by llama.cpp, speaking over a streaming POST /v1/responses connection.")
      .build();
    request.setMaxOutputTokens(400);
    StreamResult result = stream("introduce-stream", request);
    Files.writeString(artifacts.resolve("introduce-stream.txt"), result.text);
    assertFalse("streaming introduce was empty", result.text.isEmpty());
  }

  @Test
  public void writeJavaStreaming() throws Exception {
    StreamResult result = stream("write-java-stream", writeJavaRequest());
    String source = extractJavaSource(result.text);
    Files.writeString(artifacts.resolve("FizzBuzz-stream.java"), source);
    assertTrue("expected class FizzBuzz in stream output", source.contains("class FizzBuzz"));
  }

  private ModelRequest writeJavaRequest() {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addSystemMessage("You are an expert Java developer. Output ONLY a complete Java source "
        + "file. No markdown fences, no commentary.")
      .addUserMessage("Write a public class named FizzBuzz in package demo with a main method "
        + "that prints FizzBuzz for 1 through 20. Include Javadoc on the class and main.")
      .build();
    request.setMaxOutputTokens(1200);
    return request;
  }

  private ModelRequest javadocRequest() {
    ModelRequest mr = new ModelRequest();
    mr.setModel(MODEL);
    ModelInputPartMessage systemMessage = ModelRequestBuilder.buildModelInputPart(Role.system,
      "You are an expert Java developer frequently asked to do things below your station. "
      + "You do them competently but with a voice that drips of disdain and biting sarcasm. "
      + "Output ONLY a complete Java source file. No markdown fences, no commentary.");
    ModelInputPartMessage userMessage = ModelRequestBuilder.buildModelInputPart(Role.user,
      "Please add comprehensive documentation to this class.");
    ModelInputPartMessage userContent = ModelRequestBuilder.buildModelInputPart(Role.user,
      "public class Foo {\n"
      + "\n"
      + "  private static final String HELLO = \"Hello World!\";\n"
      + "\n"
      + "  public String goodMorning() {\n"
      + "    return HELLO + \" Today is \" + LocalDate.now() + \" and it's a great day!\";\n"
      + "  }\n"
      + "}");
    ModelInputArray modelInput = new ModelInputArray();
    modelInput.addValue(systemMessage);
    modelInput.addValue(userMessage);
    modelInput.addValue(userContent);
    mr.setInput(modelInput);
    mr.setMaxOutputTokens(1200);
    return mr;
  }

  private String requireText(ModelResponse response) {
    assertNotNull(response);
    String text = ModelResponseReader.getText(response);
    assertNotNull(text);
    assertFalse("empty model text", text.isEmpty());
    return text;
  }

  private StreamResult stream(String label, ModelRequest request) throws Exception {
    RecordingListener listener = new RecordingListener();
    client.generateStreaming(request, listener);
    boolean done = listener.completed.await(WAIT_SECONDS, TimeUnit.SECONDS)
      || listener.errored.await(2, TimeUnit.SECONDS);
    assertTrue(label + " stream did not finish within " + WAIT_SECONDS + "s", done);
    if (!listener.errors.isEmpty()) {
      fail(label + " stream error: " + listener.errors.get(0));
    }

    StringBuilder deltas = new StringBuilder();
    StringBuilder log = new StringBuilder();
    int i = 0;
    for (StreamEvent event : listener.events) {
      StreamEventType type = event.getEvent();
      log.append(i++).append(' ').append(type);
      if (event instanceof OutputTextDeltaEvent) {
        String delta = ((OutputTextDeltaEvent) event).getDelta();
        if (delta != null) {
          deltas.append(delta);
          log.append(" delta=").append(delta.replace("\n", "\\n"));
        }
      } else if (event instanceof OutputTextDoneEvent) {
        String text = ((OutputTextDoneEvent) event).getText();
        if (text != null) {
          log.append(" textChars=").append(text.length());
        }
      }
      log.append('\n');
    }
    String text = deltas.toString();
    if (text.isEmpty()) {
      for (StreamEvent event : listener.events) {
        if (event instanceof OutputTextDoneEvent) {
          String doneText = ((OutputTextDoneEvent) event).getText();
          if (doneText != null) {
            text = doneText;
          }
        }
      }
    }
    Files.writeString(artifacts.resolve(label + "-events.txt"), log.toString());
    Files.writeString(artifacts.resolve(label + ".txt"), text);
    System.out.println(label + " events=" + listener.events.size() + " chars=" + text.length());
    return new StreamResult(text);
  }

  private static String extractJavaSource(String text) throws Exception {
    if (text == null) {
      return null;
    }
    String trimmed = text.trim();
    if (trimmed.startsWith("{")) {
      JsonNode json = MAPPER.readTree(trimmed);
      if (json.has("code") && json.get("code").isTextual()) {
        return json.get("code").asText();
      }
    }
    int fence = trimmed.indexOf("```");
    if (fence >= 0) {
      int start = trimmed.indexOf('\n', fence);
      int end = trimmed.indexOf("```", start + 1);
      if (start > 0 && end > start) {
        return trimmed.substring(start + 1, end).trim();
      }
    }
    return trimmed;
  }

  private static final class StreamResult {

    private final String text;

    private StreamResult(String text) {
      this.text = text == null ? "" : text;
    }
  }
}
