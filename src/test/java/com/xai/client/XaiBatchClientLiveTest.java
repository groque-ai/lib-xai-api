package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.batch.Batch;
import com.xai.api.batch.BatchMetadata;
import com.xai.api.batch.BatchResult;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.models.ImageGenerationModel;
import com.xai.api.models.LanguageModel;
import com.xai.api.models.ListImageGenerationModelsResponse;
import com.xai.api.models.ListLanguageModelsResponse;
import com.xai.api.models.ListModelsResponse;
import com.xai.api.models.Model;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.config.ModelResponseConfiguration;
import com.xai.api.responses.config.ModelResponseFormatJsonSchema;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.input.part.ModelInputPartMessage;
import com.xai.api.type.Role;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.video.GenerateVideoRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Live {@code /v1/batches} tests against xAI. Uses
 * {@link XaiClientConfig#readConfig()}. Artifacts land in
 * {@code target/test/xai}.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class XaiBatchClientLiveTest {

  private static final String TEXT_MODEL = "grok-4.3";
  private static final String VIDEO_MODEL = "grok-imagine-video";
  private static final long WAIT_SECONDS = 180;
  private static final long MEDIA_WAIT_SECONDS = 600;
  private static final ObjectMapper MAPPER = new ObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private static Path artifacts;
  private static String codeModel;
  private static String imageModel;

  private XaiBatchClient client;

  @BeforeClass
  public static void loadCatalog() throws Exception {
    artifacts = Paths.get("target", "test", "xai");
    Files.createDirectories(artifacts);

    XaiModelClient models = new XaiModelClient();
    ListModelsResponse all = models.getModels();
    ListLanguageModelsResponse language = models.getLanguageModels();
    ListImageGenerationModelsResponse images = models.getImageGenerationModels();

    StringBuilder catalog = new StringBuilder();
    catalog.append("# xAI model catalog (live)\n");
    catalog.append("\n## /v1/models\n");
    if (all != null) {
      for (Model model : all.getData()) {
        catalog.append("- ").append(model.getId()).append('\n');
      }
    }
    catalog.append("\n## /v1/language-models\n");
    List<String> languageIds = new ArrayList<>();
    List<String> languageAliases = new ArrayList<>();
    if (language != null) {
      for (LanguageModel model : language.getModels()) {
        languageIds.add(model.getId());
        catalog.append("- ").append(model.getId());
        if (model.getAliases() != null && !model.getAliases().isEmpty()) {
          languageAliases.addAll(model.getAliases());
          catalog.append(" aliases=").append(model.getAliases());
        }
        catalog.append('\n');
      }
    }
    catalog.append("\n## /v1/image-generation-models\n");
    List<String> imageIds = new ArrayList<>();
    if (images != null && images.getModels() != null) {
      for (ImageGenerationModel model : images.getModels()) {
        imageIds.add(model.getId());
        catalog.append("- ").append(model.getId());
        if (model.getAliases() != null && !model.getAliases().isEmpty()) {
          catalog.append(" aliases=").append(model.getAliases());
        }
        catalog.append('\n');
      }
    }
    Files.writeString(artifacts.resolve("models.txt"), catalog.toString());
    MAPPER.writeValue(artifacts.resolve("models-language.json").toFile(), language);
    MAPPER.writeValue(artifacts.resolve("models-image.json").toFile(), images);

    String requestedCode = resolveCodeModel(languageIds, languageAliases);
    // Batch rejects grok-code-fast and its canonical grok-build-0.1.
    codeModel = TEXT_MODEL;
    imageModel = resolveImageModel(imageIds);
    Files.writeString(artifacts.resolve("resolved-models.txt"),
      "requestedCode=" + requestedCode + "\n"
      + "code=" + codeModel + " (batch fallback; grok-code-fast/grok-build-0.1 not batch-enabled)\n"
      + "image=" + imageModel + "\nvideo=" + VIDEO_MODEL + "\ntext=" + TEXT_MODEL + "\n");
    Files.writeString(artifacts.resolve("NOTE-grok-code-fast.txt"),
      "Models API: grok-code-fast is an alias of grok-build-0.1.\n"
      + "POST /v1/batches/.../requests rejects both:\n"
      + "  Model grok-code-fast is not supported for batch processing.\n"
      + "  Model grok-build-0.1 is not supported for batch processing.\n"
      + "Java/Javadoc batch tests therefore use " + TEXT_MODEL + ".\n");
    System.out.println("resolved " + Files.readString(artifacts.resolve("resolved-models.txt")));
  }

  @Before
  public void setUp() {
    XaiClientConfig env = XaiClientConfig.readConfig();
    XaiClientConfig config = new XaiClientConfig.Builder()
      .withApiKey(env.getApiKey())
      .withBaseUrl(env.getBaseUrl())
      .withConnectTimeout(env.getConnectTimeout())
      .withRequestTimeout(Duration.ofSeconds(60))
      .withFollowRedirects(env.isFollowRedirects())
      .build();
    client = new XaiBatchClient(config);
    client.setPollInterval(Duration.ofSeconds(3));
  }

  @After
  public void tearDown() throws Exception {
    if (client != null) {
      client.close();
    }
  }

  @Test
  public void createGetListCancel() throws Exception {
    Batch created = client.create("lib-xai-api live create");
    assertNotNull(created);
    assertNotNull(created.getBatchId());
    System.out.println("created " + created.getBatchId());

    Batch got = client.get(created.getBatchId());
    assertEquals(created.getBatchId(), got.getBatchId());
    assertNotNull(got.getState());
    assertEquals(0, got.getState().getNumRequests());

    List<Batch> listed = client.list();
    assertNotNull(listed);
    boolean found = false;
    for (Batch batch : listed) {
      if (created.getBatchId().equals(batch.getBatchId())) {
        found = true;
        break;
      }
    }
    assertTrue("created batch should appear in list", found);

    Batch cancelled = client.cancel(created.getBatchId());
    assertNotNull(cancelled);
    assertEquals(created.getBatchId(), cancelled.getBatchId());
  }

  @Test
  public void submitGetListRequests() throws Exception {
    String batchId = client.submit(tinyRequest());
    assertNotNull(batchId);
    System.out.println("submitted " + batchId);

    Batch got = client.get(batchId);
    assertEquals(batchId, got.getBatchId());
    assertNotNull(got.getName());
    assertTrue(got.getName().startsWith("ModelRequest submitted at "));
    assertEquals(1, got.getState().getNumRequests());

    List<BatchMetadata> meta = client.listRequests(batchId);
    assertEquals(1, meta.size());
    assertNotNull(meta.get(0).getBatchRequestId());

    client.cancel(batchId);
  }

  @Test
  public void submitAppendsToExistingBatch() throws Exception {
    String batchId = client.submit(tinyRequest());
    client.submit(batchId, tinyRequest());

    Batch got = client.get(batchId);
    assertEquals(2, got.getState().getNumRequests());

    List<BatchMetadata> meta = client.listRequests(batchId);
    assertEquals(2, meta.size());

    client.cancel(batchId);
  }

  @Test
  public void pollUntilCompleteThenListResults() throws Exception {
    String batchId = client.submit(tinyRequest());
    List<BatchResult> results = submitWaitAndSave(batchId, "pong", WAIT_SECONDS);
    assertFalse("expected at least one result row", results.isEmpty());
    assertNotNull(results.get(0).getBatchRequestId());
  }

  @Test
  public void listenerReceivesCompletion() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    AtomicReference<String> completedId = new AtomicReference<>();
    AtomicReference<JsonNode> completedResults = new AtomicReference<>();
    AtomicReference<Throwable> error = new AtomicReference<>();

    client.setListener(new BatchListener() {
      @Override
      public void onProgress(Batch batch) {
        System.out.println("progress " + batch.getBatchId()
          + " pending=" + (batch.getState() == null ? "?" : batch.getState().getNumPending()));
      }

      @Override
      public void onComplete(String batchId, JsonNode results) {
        completedId.set(batchId);
        completedResults.set(results);
        done.countDown();
      }

      @Override
      public void onError(String batchId, Throwable err) {
        error.set(err);
        done.countDown();
      }
    });

    String batchId = client.submit(tinyRequest());
    boolean signaled = done.await(WAIT_SECONDS, TimeUnit.SECONDS);
    if (error.get() != null) {
      throw new AssertionError("listener error for " + batchId, error.get());
    }
    assertTrue("listener did not complete within " + WAIT_SECONDS + "s", signaled);
    assertEquals(batchId, completedId.get());
    assertNotNull(completedResults.get());
    assertTrue(completedResults.get().isArray());
    assertTrue(completedResults.get().size() >= 1);
    Files.writeString(artifacts.resolve("listener-" + batchId + ".json"),
      MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completedResults.get()));
  }

  @Test
  public void generateImage() throws Exception {
    assertNotNull("no image model in catalog", imageModel);
    GenerateImageRequest request = new GenerateImageRequest();
    request.setModel(imageModel);
    request.setPrompt("A cat in a tree");
    String batchId = client.submit(request);
    List<BatchResult> results = submitWaitAndSave(batchId, "image", MEDIA_WAIT_SECONDS);
    String url = findUrl(results.get(0).getBatchResult());
    assertNotNull("image result had no url: " + results.get(0).getBatchResult(), url);
    Path file = download(url, artifacts.resolve("cat-in-a-tree.jpg"));
    assertTrue(Files.size(file) > 0);
  }

  @Test
  public void generateVideo() throws Exception {
    GenerateVideoRequest request = new GenerateVideoRequest();
    request.setModel(VIDEO_MODEL);
    request.setPrompt("A cat playing with a ball");
    request.setDuration(3);
    String batchId = client.submit(request);
    List<BatchResult> results = submitWaitAndSave(batchId, "video", MEDIA_WAIT_SECONDS);
    String url = findUrl(results.get(0).getBatchResult());
    assertNotNull("video result had no url: " + results.get(0).getBatchResult(), url);
    Path file = download(url, artifacts.resolve("cat-playing-ball.mp4"));
    assertTrue(Files.size(file) > 0);
  }

  @Test
  public void writeJava() throws Exception {
    assertNotNull("no grok-code-fast* model in catalog", codeModel);
    ModelRequest request = new ModelRequestBuilder()
      .withModel(codeModel)
      .addSystemMessage("You are an expert Java developer. Output ONLY a complete Java source file. "
        + "No markdown fences, no commentary.")
      .addUserMessage("Write a public class named FizzBuzz in package demo with a main method "
        + "that prints FizzBuzz for 1 through 20. Include Javadoc on the class and main.")
      .build();
    request.setMaxOutputTokens(1200);
    String batchId = client.submit(request);
    List<BatchResult> results = submitWaitAndSave(batchId, "write-java", MEDIA_WAIT_SECONDS);
    String source = extractJavaSource(chatContent(results.get(0).getBatchResult()));
    assertNotNull(source);
    assertTrue(source.contains("class FizzBuzz"));
    Files.writeString(artifacts.resolve("FizzBuzz.java"), source);
  }

  @Test
  public void addJavadocToExistingClass() throws Exception {
    assertNotNull("no grok-code-fast* model in catalog", codeModel);
    ModelRequest request = javadocRequest(codeModel);
    String batchId = client.submit(request);
    List<BatchResult> results = submitWaitAndSave(batchId, "javadoc", MEDIA_WAIT_SECONDS);
    String source = extractJavaSource(chatContent(results.get(0).getBatchResult()));
    assertNotNull(source);
    assertTrue(source.contains("class Foo"));
    assertTrue(source.contains("/**"));
    Files.writeString(artifacts.resolve("Foo.java"), source);
  }

  private ModelRequest tinyRequest() {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(TEXT_MODEL)
      .addUserMessage("Reply with exactly one word: pong")
      .build();
    request.setMaxOutputTokens(16);
    return request;
  }

  private ModelRequest javadocRequest(String model) throws Exception {
    ModelRequest mr = new ModelRequest();
    mr.setModel(model);
    ModelInputPartMessage systemMessage = ModelRequestBuilder.buildModelInputPart(Role.system,
      "You are an expert Java developer frequently asked to do things below your station. "
      + "You do them competently but with a voice that drips of disdain and biting sarcasm.");
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

    ObjectNode schema = MAPPER.createObjectNode();
    schema.put("type", "object");
    ObjectNode properties = MAPPER.createObjectNode();
    ObjectNode codeProp = MAPPER.createObjectNode();
    codeProp.put("type", "string");
    codeProp.put("description", "The complete, standalone Java source file content as a string.");
    properties.set("code", codeProp);
    ObjectNode lang = MAPPER.createObjectNode();
    lang.put("type", "string");
    properties.set("language", lang);
    ObjectNode notes = MAPPER.createObjectNode();
    notes.put("type", "string");
    properties.set("notes", notes);
    schema.set("properties", properties);
    ArrayNode required = MAPPER.createArrayNode();
    required.add("code");
    schema.set("required", required);
    schema.put("additionalProperties", false);

    ModelResponseConfiguration configuration = new ModelResponseConfiguration();
    ModelResponseFormatJsonSchema formatSchema = new ModelResponseFormatJsonSchema();
    formatSchema.setName("java_source_file");
    formatSchema.setStrict(Boolean.TRUE);
    formatSchema.setSchema(schema);
    configuration.setFormat(formatSchema);
    mr.setText(configuration);
    return mr;
  }

  private List<BatchResult> submitWaitAndSave(String batchId, String label, long waitSeconds)
    throws Exception {
    Batch done = waitUntilComplete(batchId, waitSeconds);
    if (done == null) {
      Batch last = client.get(batchId);
      Files.writeString(artifacts.resolve(label + "-timeout-batch.json"),
        MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(last));
    }
    assertNotNull("batch did not finish within " + waitSeconds + "s: " + batchId, done);
    Files.writeString(artifacts.resolve(label + "-batch.json"),
      MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(done));
    List<BatchResult> results = client.listResults(batchId);
    Files.writeString(artifacts.resolve(label + "-results.json"),
      MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(results));
    assertFalse(label + " expected results", results.isEmpty());
    if (done.getState().getNumError() > 0 && done.getState().getNumSuccess() == 0) {
      throw new AssertionError(label + " batch finished with errors only: "
        + results.get(0).getBatchResult());
    }
    return results;
  }

  private Batch waitUntilComplete(String batchId, long waitSeconds) throws InterruptedException {
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(waitSeconds);
    while (System.currentTimeMillis() < deadline) {
      Batch batch = client.get(batchId);
      if (batch != null && batch.getState() != null) {
        System.out.println("wait " + batchId
          + " pending=" + batch.getState().getNumPending()
          + " success=" + batch.getState().getNumSuccess()
          + " error=" + batch.getState().getNumError());
        if (batch.getState().getNumRequests() > 0
          && batch.getState().getNumPending() == 0) {
          return batch;
        }
      }
      Thread.sleep(5000);
    }
    return null;
  }

  private static String resolveCodeModel(List<String> ids, List<String> aliases) {
    // grok-code-fast is an alias of grok-build-0.1. Batch rejects the alias
    // ("not supported for batch processing") but accepts the canonical id.
    if (ids.contains("grok-build-0.1")) {
      return "grok-build-0.1";
    }
    if (ids.contains("grok-code-fast")) {
      return "grok-code-fast";
    }
    if (aliases.contains("grok-code-fast") || aliases.contains("grok-code-fast-1")) {
      return "grok-code-fast";
    }
    for (String id : ids) {
      if (id != null && (id.contains("code-fast") || id.startsWith("grok-build"))) {
        return id;
      }
    }
    return null;
  }

  private static String resolveImageModel(List<String> ids) {
    if (ids.contains("grok-imagine-image")) {
      return "grok-imagine-image";
    }
    if (ids.contains("grok-imagine-image-2.0")) {
      return "grok-imagine-image-2.0";
    }
    for (String id : ids) {
      if (id != null && id.startsWith("grok-imagine-image")) {
        return id;
      }
    }
    return ids.isEmpty() ? null : ids.get(0);
  }

  private static String chatContent(JsonNode batchResult) {
    if (batchResult == null) {
      return null;
    }
    JsonNode content = batchResult.at("/response/chat_get_completion/choices/0/message/content");
    if (content.isTextual()) {
      return content.asText();
    }
    JsonNode outputText = batchResult.at("/response/responses/output_text");
    if (outputText.isTextual()) {
      return outputText.asText();
    }
    return batchResult.toString();
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

  private static String findUrl(JsonNode node) {
    if (node == null || node.isNull() || node.isMissingNode()) {
      return null;
    }
    if (node.isTextual()) {
      String text = node.asText();
      if (text.startsWith("http://") || text.startsWith("https://")) {
        return text;
      }
      return null;
    }
    if (node.has("url") && node.get("url").isTextual()) {
      String url = node.get("url").asText();
      if (url.startsWith("http")) {
        return url;
      }
    }
    if (node.isObject()) {
      java.util.Iterator<JsonNode> values = node.elements();
      while (values.hasNext()) {
        String found = findUrl(values.next());
        if (found != null) {
          return found;
        }
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        String found = findUrl(child);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static Path download(String url, Path dest) throws Exception {
    HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    HttpResponse<Path> response = http.send(
      HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofMinutes(2)).build(),
      HttpResponse.BodyHandlers.ofFile(dest));
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new AssertionError("download " + url + " status=" + response.statusCode());
    }
    return dest;
  }

}
