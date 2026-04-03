package com.xai.api.responses.util;

// Custom deserializer (for symmetry/completeness): handles incoming String or array
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.responses.input.part.content.item.ModelInputContentItem;
import java.io.IOException;
import java.util.List;

/**
 * <pre>
 * ModelInputContent
 * oneOf
 * 0
 * type	"string"
 * description	"Text input."
 * 1
 * type	"array"
 * items	{ "$ref": "#/components/schemas/ModelInputContentItem" }
 * </pre>
 *
 * @author Key Bridge
 */
public class ModelInputContentDeserializer extends JsonDeserializer<Object> {

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);
    if (node.isTextual()) {
      return node.asText();
    } else if (node.isArray()) {
      List<ModelInputContentItem> parts = mapper
        .convertValue(node,
                      mapper
                        .getTypeFactory()
                        .constructCollectionType(List.class,
                                                 ModelInputContentItem.class));
      return parts;
    }
    return null;  // Or throw exception
  }
}
