package com.xai.api.responses.util;

// Custom serializer: outputs String directly or List as array
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xai.api.responses.input.part.content.ModelInputContentString;
import java.io.IOException;
import java.util.List;

public class ModelInputContentSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value instanceof ModelInputContentString) {
      ModelInputContentString mics = (ModelInputContentString) value;
      gen.writeString(mics.getValue());
    } else if (value instanceof List) {
      gen.writeStartArray();
      for (Object item : (List<?>) value) {
        gen.writeObject(item);  // Serialize each ContentPart or Map
      }
      gen.writeEndArray();
    } else {
      gen.writeNull();  // Or throw exception for invalid types
    }
  }
}
