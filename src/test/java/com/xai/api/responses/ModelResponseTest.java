package com.xai.api.responses;

import ai.x.grok.api.client.EntityBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Key Bridge
 */
public class ModelResponseTest {

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
  public void testJsonBuilder() throws IOException {

    ModelResponse mr = EntityBuilder.buildModelResponse();
    printJson(mr);

  }

  @Test
  public void testUnmashalResponse() throws URISyntaxException, IOException {
    URL url = this.getClass().getClassLoader().getResource("META-INF/json/responses-reply.json");
//    URL url = this.getClass().getClassLoader().getResource("META-INF/json/responses-sample.json");
    System.out.println("url: " + url);
    Path path = Paths.get(url.toURI());
    String json = new String(Files.readAllBytes(path));
//    System.out.println(json);
    ModelResponse mr = MAPPER.readValue(json, ModelResponse.class);
    System.out.println("mr: " + mr);

    System.out.println(" tool mode     :  " + mr.getToolChoice());
//    System.out.println(" tool function :  " + mr.getToolChoiceFunction());
  }

}
