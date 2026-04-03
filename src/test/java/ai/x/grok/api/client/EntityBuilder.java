package ai.x.grok.api.client;

import com.thedeanda.lorem.LoremIpsum;
import com.xai.api.models.LanguageModel;
import com.xai.api.models.ListLanguageModelsResponse;
import com.xai.api.models.ListModelsResponse;
import com.xai.api.models.Model;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.config.ReasoningConfiguration;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.message.OutputMessageContentText;
import com.xai.api.type.OutputStatus;
import com.xai.api.type.ReasoningEffort;
import com.xai.api.type.Role;
import java.time.Instant;
import java.util.*;

public class EntityBuilder {

  private static final LoremIpsum LOREM = new LoremIpsum();
  private static final Random RANDOM = new Random();

  // ─────────────────────────────────────────────────────────────────────────────
  //  Helpers
  // ─────────────────────────────────────────────────────────────────────────────
  public static <T extends Enum<T>> T randomEnum(Class<T> enumClass) {
    T[] values = enumClass.getEnumConstants();
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("Not an enum or empty: " + enumClass);
    }
    return values[RANDOM.nextInt(values.length)];
  }

  public static String randomId(String prefix) {
    return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
  }

  public static String randomUuid() {
    return UUID.randomUUID().toString();
  }

  public static long randomTimestamp() {
    return Instant.now().minusSeconds(RANDOM.nextInt(31536000)).getEpochSecond(); // up to 1 year ago
  }

  public static String randomText(int words) {
    return LOREM.getWords(words);
  }

  public static List<String> randomStringList(int size) {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      list.add(randomId("item"));
    }
    return list;
  }

  // Add this helper method to EntityBuilder
  private static String randomSentence() {
    int wordCount = 6 + RANDOM.nextInt(15); // 6–20 words
    String text = LOREM.getWords(wordCount);
    return text.substring(0, 1).toUpperCase() + text.substring(1) + ".";
  }

  /**
   * Generates the requested number of paragraphs with variable sentence and
   * word counts.
   */
  public static String randomParagraph(int numParagraphs) {
    if (numParagraphs <= 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    for (int p = 0; p < numParagraphs; p++) {
      int numSentences = 3 + RANDOM.nextInt(10); // 3–12 sentences per paragraph
      StringBuilder para = new StringBuilder();

      for (int s = 0; s < numSentences; s++) {
        // 4–18 words per sentence
        int numWords = 4 + RANDOM.nextInt(15);
        String sentence = LOREM.getWords(numWords);

        // Capitalize first word of sentence
        if (sentence.length() > 0) {
          sentence = Character.toUpperCase(sentence.charAt(0)) + sentence.substring(1);
        }

        para.append(sentence);

        // Randomly choose ending punctuation
        String[] endings = {".", ".", ".", "!", "?"};
        para.append(endings[RANDOM.nextInt(endings.length)]);
        para.append(" ");
      }

      // Trim trailing space
      if (para.length() > 0) {
        para.setLength(para.length() - 1);
      }

      sb.append(para.toString().trim());

      if (p < numParagraphs - 1) {
        sb.append("\n\n");
      }
    }

    return sb.toString();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  //  Generic list builder
  // ─────────────────────────────────────────────────────────────────────────────
  public static <T> List<T> buildList(T itemTemplate, int count) {
    List<T> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(itemTemplate);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────────────────────────────
  //  Model / listing DTOs
  // ─────────────────────────────────────────────────────────────────────────────
  public static ListModelsResponse buildListModelsResponse() {
    ListModelsResponse resp = new ListModelsResponse();
    resp.setObject("list");
    resp.setData(buildList(buildModel(), 3 + RANDOM.nextInt(8)));
    return resp;
  }

  public static Model buildModel() {
    Model m = new Model();
    m.setId("grok-" + RANDOM.nextInt(5) + "-" + randomId(""));
    m.setCreated(randomTimestamp());
    m.setObject("model");
    m.setOwnedBy("xai");
    return m;
  }

  public static ListLanguageModelsResponse buildListLanguageModelsResponse() {
    ListLanguageModelsResponse resp = new ListLanguageModelsResponse();
    resp.setModels(buildList(buildLanguageModel(), 2 + RANDOM.nextInt(6)));
    return resp;
  }

  public static LanguageModel buildLanguageModel() {
    LanguageModel m = new LanguageModel();
    m.setId("grok-4-test");
    m.setFingerprint(randomUuid().substring(0, 12));
    m.setCreated(randomTimestamp());
    m.setObject("model");
    m.setOwnedBy("xai");
    m.setVersion("2025." + (1 + RANDOM.nextInt(12)));
    m.setInputModalities(Arrays.asList("text"));
    m.setOutputModalities(Arrays.asList("text"));
    m.setPromptTextTokenPrice(5 + RANDOM.nextInt(20));
    m.setCachedPromptTextTokenPrice(2 + RANDOM.nextInt(10));
    m.setPromptImageTokenPrice(0);
    m.setCompletionTextTokenPrice(15 + RANDOM.nextInt(50));
    m.setSearchPrice(0);
    m.setAliases(Collections.singletonList("grok-latest"));
    return m;
  }

  public static ModelRequest buildModelRequest() {
    ModelRequest request = new ModelRequest();

    request.setLogprobs(RANDOM.nextBoolean());

    request.setMaxOutputTokens(128 + RANDOM.nextInt(8065));
    // model - fake but plausible model name
    String[] modelPrefixes = {"grok-beta", "grok", "grok-vision", "grok-4", "grok-3", "grok-mini"};
    request.setModel(modelPrefixes[RANDOM.nextInt(modelPrefixes.length)] + "-" + (100 + RANDOM.nextInt(900)));

    // parallelToolCalls
    request.setParallelToolCalls(RANDOM.nextBoolean());

    // previousResponseId - UUID-like
    request.setPreviousResponseId(UUID.randomUUID().toString());

    ReasoningConfiguration reasoningConfiguration = new ReasoningConfiguration();
    reasoningConfiguration.setEffort(randomEnum(ReasoningEffort.class));
    request.setReasoning(reasoningConfiguration);
    request.setStore(RANDOM.nextBoolean());

    // stream
    request.setStream(RANDOM.nextBoolean());

    // temperature (0.0 – 2.0)
    request.setTemperature(Double.valueOf(0.1 + RANDOM.nextDouble() * 1.9).floatValue());

    request.setTopLogprobs(RANDOM.nextInt(6));

    // topP (0.0 – 1.0)
    request.setTopP(RANDOM.nextDouble());

    // truncation - usually not used, but populate randomly
    request.setUser("test-user-" + LOREM.getWords(1).toLowerCase() + "-" + RANDOM.nextInt(9999));

    return request;
  }

  /**
   * Internal helper to generate varied realistic text content for model input
   */
  private static String generateRandomInputText() {
    double r = RANDOM.nextDouble();
    if (r < 0.30) {
      // Short question or command
      return LOREM.getWords(4, 12) + (RANDOM.nextBoolean() ? "?" : "!");
    } else if (r < 0.65) {
      // Normal length message
      return LOREM.getWords(10, 35) + ".";
    } else if (r < 0.85) {
      // Longer paragraph
      return randomParagraph(1);
    } else {
      // Multi-sentence with some structure
      int sentenceCount = 2 + RANDOM.nextInt(5);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < sentenceCount; i++) {
        sb.append(LOREM.getWords(6, 18)).append(". ");
      }
      return sb.toString().trim();
    }
  }

  public static ModelResponse buildModelResponse() {
    ModelResponse response = new ModelResponse();

    response.setId("resp_" + UUID.randomUUID().toString().substring(0, 8));
    response.setObject("response");
    response.setCreatedAt(Instant.now().getEpochSecond() + RANDOM.nextInt(1000000));  // recent-ish timestamp

    response.setModel("grok-4-0709");

    // ─────────────────────────────────────────────
    // Output – mix of several possible ModelOutput types
    // ─────────────────────────────────────────────
    List<ModelOutput> outputs = new ArrayList<>();

    // 1. Typical output_text
    OutputMessage outputEntry = new OutputMessage();
    outputEntry.setRole(Role.assistant);
    outputEntry.setId(randomUuid());
    outputEntry.setStatus(OutputStatus.completed);

    OutputMessageContentText contentEntry = new OutputMessageContentText();
    contentEntry.setText(LOREM.getWords(40, 80) + " – that's basically the answer.");
    outputEntry.setContent(Arrays.asList(contentEntry));
    outputs.add(outputEntry);

    response.setOutput(outputs);
//

    return response;
  }

}
