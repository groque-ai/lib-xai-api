package com.xai.api.responses.tool;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModelToolChoiceDeserializer extends JsonDeserializer<ModelToolChoice> {

  @Override
  public ModelToolChoice deserialize(JsonParser p, DeserializationContext ctxt) {
    try {
      JsonNode node = p.getCodec().readTree(p);

      // Case 1: string value
      if (node.isTextual()) {
        return new ModelToolChoiceString(node.asText());
      }

      // Case 2: object with "type": "function"
      if (node.isObject()) {
        ObjectNode obj = (ObjectNode) node;
        JsonNode typeNode = obj.get("type");

        if (typeNode != null && "function".equals(typeNode.asText())) {
          return p.getCodec().treeToValue(node, ModelToolChoiceFunction.class);
        }
      }

      // Unknown shape → fail loudly
      throw new JsonMappingException(p, "Invalid ModelToolChoice: " + node.toString());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
