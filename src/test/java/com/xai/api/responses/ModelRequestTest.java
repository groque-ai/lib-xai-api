package com.xai.api.responses;

import ai.x.grok.api.client.ColumnPrinter;
import ai.x.grok.api.client.EntityBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.input.part.ModelInputPartMessage;
import com.xai.api.type.Role;
import com.xai.api.util.ModelRequestBuilder;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Key Bridge
 */
public class ModelRequestTest {

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setUpClass() {
    MAPPER = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  private void printJson(Object obj) throws IOException {
    if (obj == null) {
      System.out.println("(null)");
      return;
    }
    String json = MAPPER.writeValueAsString(obj);
    System.out.println(json);
  }

//  @Test
  public void testRoundTrip() throws IOException {
    ModelRequest mr = EntityBuilder.buildModelRequest();
//    printJson(mr);

    String json = MAPPER.writeValueAsString(mr);
    ModelRequest recovered = MAPPER.readValue(json, ModelRequest.class);
    ColumnPrinter.printAtCol(json, MAPPER.writeValueAsString(recovered));
  }

  private static final ModelRequestBuilder builder = new ModelRequestBuilder();

  @Test
  public void testInput() throws IOException {
    ModelRequest mr = new ModelRequest();
//    mr.setModel("grok-4-0709");
    mr.setModel("my-text-model");

//    ModelInput mi = new ModelInput();// = EntityBuilder.buildModelInput();
    ////    printJson(mi);
//
    System.out.println("");
    System.out.println("Plain text (most common)");
    ModelInputPartMessage mi = ModelRequestBuilder.buildModelInputPart(Role.user, "Hello world");

    //    mi.setRole(Role.user);    mi.setContent("hello world");
//    mr.getInput().clear();
//    mr.getInput().add(mi);
    printJson(mr);

    System.out.println("");
    System.out.println("Plain text with system prompt");
//    mr.getInput().clear();
    ModelInputArray modelInput = new ModelInputArray();
    modelInput.addValue(ModelRequestBuilder.buildModelInputPart(Role.system, "You are a helpful assistant that can answer questions and help with tasks."));
    modelInput.addValue(ModelRequestBuilder.buildModelInputPart(Role.user, "What is 101 * 3?"));
    mr.setInput(modelInput);
//    mr.addInput(new ModelInput(Role.system, "You are a helpful assistant that can answer questions and help with tasks."));
//    mr.addInput(new ModelInput(Role.user, "What is 101 * 3?"));
    printJson(mr);

//    System.out.println("");
//    System.out.println("Vision/multimodal");
//    ModelInputContentItemImage imageItem = builder.image("http://foo.bar/image.png");
//    ModelInputContentItemText textItem = builder.text("Describe this image");
//    mi = ModelRequestBuilder.buildModelInputPart(Role.user, imageItem, textItem);
//    mi = new ModelInput();
//    mi.setRole(Role.user);
//    TextContentPart part = new TextContentPart("Describe this");
//    ImageUrlContentPart imgPart = new ImageUrlContentPart("http://foo.bar", ImageDetail.auto);
//    ArrayList<Object> contentParts = new ArrayList<>();
//    contentParts.add(part);
//    contentParts.add(imgPart);
//    mi.setContent(contentParts);
//    mr.getInput().clear();
    modelInput = new ModelInputArray();
    modelInput.addValue(mi);
//
//    mr.getInput().add(mi);
    mr.setInput(modelInput);
    printJson(mr);

    System.out.println("");
    System.out.println("Multi-turn continuation");

//    textItem = builder.text("What is 2*2?");
    mi = ModelRequestBuilder.buildModelInputPart(Role.user, "What is 2*2?");

//    mi = new ModelInput();    mi.setRole(Role.user);    mi.setContent("What is 2*2?");
    modelInput = new ModelInputArray();
    modelInput.addValue(mi);
    mr.setInput(modelInput);
//  mr.getInput().clear();
//    mr.addInput(mi);
    mr.setPreviousResponseId("resp_abc123");
    printJson(mr);
//



////    String json = MAPPER.writeValueAsString(mi);
////    ModelInput recovered = MAPPER.readValue(json, ModelInput.class);
////    ColumnPrinter.printAtCol(json, MAPPER.writeValueAsString(recovered));
  }

}
