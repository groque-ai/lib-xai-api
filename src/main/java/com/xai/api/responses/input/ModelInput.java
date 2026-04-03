package com.xai.api.responses.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.xai.api.responses.util.ModelInputDeserializer;

/**
 * 0	{ type: "string", description: "Text input." }
 * <p>
 * 1 type	"array" $ref	"#/components/schemas/ModelInputPart"
 * <pre>
 * ModelInput
 * ├─ String (simple prompt, rare, legacy)  ← convenience overload
 * └─ ModelInputPart[] (structured input)   ← canonical usage
 *     ├─ Message (common)
 *     │    ├─ role : "user" | "system" | "developer" | "assistant"
 *     │    ├─ name (optional)
 *     │    └─ content : ModelInputContent
 *     │         ├─ String (shorthand for single text)
 *     │         └─ ModelInputContentItem[]
 *     │              ├─ input_text       ← common
 *     │              ├─ input_image      ← optional, multimodal
 *     │              └─ input_file       ← optional, multimodal
 *     │
 *     └─ ModelInputItem (advanced / optional)
 *          ├─ wraps previous output
 *          ├─ polymorphic (no type field)
 *          └─ oneOf:
 *               ├─ FunctionToolCallOutput      ← most common for local tools
 *               └─ ModelOutput (any variant, advanced)
 *                    ├─ OutputMessage        ← optional, replay prior messages
 *                    ├─ Reasoning            ← optional, chain prior reasoning
 *                    ├─ FunctionToolCall     ← rare, previous tool call
 *                    ├─ McpCall              ← rare, multi-call plan
 *                    ├─ CustomToolCall       ← rare, user-defined tool
 *                    ├─ CodeInterpreterCall  ← rare, prior code execution
 *                    ├─ WebSearchCall        ← rare, prior search
 *                    └─ FileSearchCall       ← rare, prior file retrieval
 *
 * </pre>
 *
 * @author Key Bridge
 */
@JsonDeserialize(using = ModelInputDeserializer.class)
public abstract class ModelInput {
}
