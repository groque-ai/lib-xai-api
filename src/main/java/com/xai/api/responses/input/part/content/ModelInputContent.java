package com.xai.api.responses.input.part.content;

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
//@JsonSerialize(using = ModelInputContentSerializer.class)  // Custom serializer for clean output
//@JsonDeserialize(using = ModelInputContentDeserializer.class) // Custom deserializer if neede
// only ModelInputContentString is supported by grok API
public abstract class ModelInputContent {
}
