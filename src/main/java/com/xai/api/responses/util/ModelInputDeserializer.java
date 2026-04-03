/*
 * Copyright 2026 Key Bridge.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xai.api.responses.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.responses.input.ModelInput;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.input.ModelInputString;
import com.xai.api.responses.input.part.ModelInputPart;
import java.io.IOException;
import java.util.List;

public class ModelInputDeserializer extends JsonDeserializer<ModelInput> {

  @Override
  public ModelInput deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);

    // Case 1: string
    if (node.isTextual()) {
      ModelInputString s = new ModelInputString();
      setField(ModelInputString.class, s, "value", node.asText());
      return s;
    }

    // Case 2: array of ModelInputPart
    if (node.isArray()) {
      List<ModelInputPart> parts
                           = mapper.convertValue(node,
                                                 mapper.getTypeFactory()
                                                   .constructCollectionType(List.class,
                                                                            ModelInputPart.class));

      ModelInputArray arr = new ModelInputArray();
      setField(ModelInputArray.class, arr, "value", parts);
      return arr;
    }

    throw new IOException("Invalid ModelInput: expected string or array");
  }

  private void setField(Class<?> clazz, Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
