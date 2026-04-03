package com.xai.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.admin.DeleteStoredCompletionResponse;
import com.xai.api.responses.config.ModelResponseConfiguration;
import com.xai.api.responses.config.ModelResponseFormatJsonSchema;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.input.part.ModelInputPartMessage;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.message.OutputMessageContent;
import com.xai.api.responses.output.message.OutputMessageContentText;
import com.xai.api.type.Role;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.client.XaiResponsesClient;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author Key Bridge
 */
public class ResponsesServiceImplTest extends AbstractServiceImpTest {

  private static XaiResponsesClient service;
  private static final ModelRequestBuilder builder = new ModelRequestBuilder();

  private static final ObjectMapper mapper = new ObjectMapper();

  public ResponsesServiceImplTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    service = new XaiResponsesClient();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Build a json schema to hold the response
   * <pre>
   * {
   * "type": "object",
   * "properties": {
   * "code": {
   * "type": "string",
   * "description": "The complete, standalone Java source code with added Javadoc. No markdown, no fences, no extra text."
   * }
   * },
   * "required": ["code"],
   * "additionalProperties": false
   * }</pre>
   */
//  @Test
  public ObjectNode testBuildSchema() throws IOException {

// schema root
    ObjectNode schema = mapper.createObjectNode();
    schema.put("type", "object");

// properties object
    ObjectNode properties = mapper.createObjectNode();

// code property
    ObjectNode codeProp = mapper.createObjectNode();
    codeProp.put("type", "string");
    codeProp.put("description", "The Java source code.");

    properties.set("code", codeProp);
    schema.set("properties", properties);

// required array
    ArrayNode required = mapper.createArrayNode();
    required.add("code");
    schema.set("required", required);

// additionalProperties: false
    schema.put("additionalProperties", false);

//    System.out.println("Schema >>>");
//    printJson(schema);
    return schema;

  }

//  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testCreateResponse() throws IOException {
    ModelRequest mr = new ModelRequest();
//    mr.setModel("grok-4-0709");
    mr.setModel("grok-code-fast-1");
//    mr.setLogprobs(Boolean.TRUE);

    ModelInputPartMessage systemMessage = ModelRequestBuilder.buildModelInputPart(Role.system,
                                                                                  "You are an expert Java developer frequently asked to do things below your station. You do them competently but with a voice that drips of disdain and biting sarcasm.");

    ModelInputPartMessage systemInstruction = ModelRequestBuilder.buildModelInputPart(Role.system,
                                                                                      "Always respond in valid JSON with this exact schema and nothing else:\n"
                                                                                      + "\n"
                                                                                      + "{\n"
                                                                                      + "  \"code\": \"the complete, standalone Java source file content as a string (including all Javadoc, imports if needed, class definition, etc.)\",\n"
                                                                                      + "  \"language\": \"java\",\n"
                                                                                      + "  \"notes\": \"optional very short note ONLY if something important needs user attention (usually empty string)\"\n"
                                                                                      + "}\n"
                                                                                      + "\n"
                                                                                      + "Do NOT include markdown, explanations, or any text outside the JSON object.\n"
                                                                                      + "Output pure JSON only — no ```json fence, no extra lines.");
    ModelInputPartMessage systemExclusive = ModelRequestBuilder.buildModelInputPart(Role.system,
                                                                                    "You are an expert Java developer. Your ONLY job is to return the FULL updated Java class with comprehensive Javadoc added. \n"
                                                                                    + "\n"
                                                                                    + "Rules you MUST follow:\n"
                                                                                    + "- Do NOT add any introductory text, explanations, apologies, sarcasm, or closing remarks.\n"
                                                                                    + "- Do NOT wrap the code in any additional commentary.\n"
                                                                                    + "- Output ONLY the complete Java source file content, exactly as it should be written to disk.\n"
                                                                                    + "- Start directly with the package (if any) or the first import / class declaration.\n"
                                                                                    + "- Enclose the entire code in a single ```java\n"
                                                                                    + "- Make sure the code is syntactically correct and includes all original functionality.\n"
                                                                                    + "\n"
                                                                                    + "User will provide the original class code. Respond with nothing else.");

    ModelInputPartMessage userMessage = ModelRequestBuilder.buildModelInputPart(Role.user, "Please add comprehensive documentation to this class.");
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
//    modelInput.addValue(systemInstruction);
//    modelInput.addValue(systemExclusive);
    modelInput.addValue(userMessage);
    modelInput.addValue(userContent);

    mr.setInput(modelInput);

    // add a jsonschema to guide the response
    ModelResponseConfiguration configuration = new ModelResponseConfiguration();
    ModelResponseFormatJsonSchema formatSchema = new ModelResponseFormatJsonSchema();
    formatSchema.setName("java_source_file");
    formatSchema.setStrict(Boolean.TRUE);
    formatSchema.setSchema(testBuildSchema());
    configuration.setFormat(formatSchema);
    mr.setText(configuration);

//    mr.addInput(systemMessage);
//    mr.addInput(userMessage);
//    mr.addInput(new ModelInputPart(Role.system, "You are a reluctant assistant that can answer questions but only with cutting sarcasm."));
//    ModelInput mi = new ModelInput(Role.user, "Hello world");
//    mr.addInput(mi);
    System.out.println("");
    System.out.println("ModelRequest >>>");
    printJson(mr);

    System.out.println("");
    ModelResponse response = service.generate(mr);
    System.out.println("ModelResponse <<<");
    printJson(response);

    System.out.println("");
    System.out.println("Response text <<< ");
    for (ModelOutput modelOutput : response.getOutput()) {
      if (modelOutput instanceof OutputMessage) {
        OutputMessage outputMessage = (OutputMessage) modelOutput;
        OutputMessageContent outputContent = outputMessage.getContent().iterator().next();
        if (outputContent instanceof OutputMessageContentText) {
          OutputMessageContentText outputMessageContentText = (OutputMessageContentText) outputContent;
          String text = outputMessageContentText.getText();

          /**
           * Parse the response as a new, nested JSON object.
           */
          JsonNode jsonNode = mapper.readTree(text.trim());
          JsonNode codeNode = jsonNode.get("code");
          if (codeNode.isTextual()) {
            String codeText = codeNode.asText(); // produces clean code
            System.out.println("Text <<<");
            System.out.println(codeText);  // RETURN THIS
          }
        }
      }
    }
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testGetResponse() throws IOException {
    String responseId = "5988d09e-b38c-353f-0d28-852557036973";
    ModelResponse response = service.get(responseId);
    printJson(response);
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testDeleteResponse() throws IOException {
    String responseId = "5988d09e-b38c-353f-0d28-852557036973";
    DeleteStoredCompletionResponse response = service.delete(responseId);
    printJson(response);
  }

}
