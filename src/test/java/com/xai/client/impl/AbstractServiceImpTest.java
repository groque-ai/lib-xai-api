package com.xai.client.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;

/**
 *
 * @author Key Bridge
 */
public class AbstractServiceImpTest {

  protected static final ObjectMapper MAPPER = new ObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  protected void printJson(Object obj) throws IOException {
    if (obj == null) {
      System.out.println("(null)");
      return;
    }
    String json = MAPPER.writeValueAsString(obj);
    System.out.println(json);
  }

}
