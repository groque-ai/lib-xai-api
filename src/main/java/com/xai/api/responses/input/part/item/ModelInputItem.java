package com.xai.api.responses.input.part.item;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.xai.api.responses.input.part.ModelInputPart;
import com.xai.api.responses.output.ModelOutput;

/**
 * advanced, agent-use only.
 * <p>
 * <p>
 * Default path: most workflows only use FunctionToolCallOutput.
 * <p>
 * Advanced path: for agents that preserve reasoning traces, replay outputs, or
 * simulate multi-step tools, you can feed any ModelOutput.
 * <p>
 * 5️⃣ Mental Model
 * <pre>
 * ModelInputPart[]  → array of messages + optional previous outputs
 * ├─ Message (normal)
 * └─ ModelInputItem (optional, advanced)
 * ├─ FunctionToolCallOutput (most common)
 * └─ ModelOutput (any previous output variant)
 * </pre> Treat ModelInputItem as “previous event for the model to continue
 * from”.
 * <p>
 * Only include when building agentic workflows / multi-step reasoning.
 * <pre>
 * oneOf:
 *   ModelOutput
 *   FunctionToolCallOutput
 * </pre>
 *
 * @JsonUnwrapped means: * Serialize the fields of item as if they were fields
 * of ModelInputItem. So JSON becomes:  <pre>
 * {
 *  "id": "call_123",
 *  "name": "sum_numbers",
 *  "output": "12"
 * }</pre> instead of:
 * <pre>
 * {
 *  "item": { ... }
 * }</pre>
 *
 * @author Key Bridge
 */
//@JsonTypeInfo(
//  use = JsonTypeInfo.Id.DEDUCTION,
//  include = JsonTypeInfo.As.PROPERTY
//)
//@JsonSubTypes({
//  @JsonSubTypes.Type(ModelOutput.class),
//  @JsonSubTypes.Type(FunctionToolCallOutput.class)
//})
public class ModelInputItem extends ModelInputPart {

  @JsonUnwrapped
  private ModelInputItemPayload item;

  public ModelInputItem() {
  }

  public ModelInputItem(ModelInputItemPayload item) {
    this.item = item;
  }

  public ModelInputItemPayload getItem() {
    return item;
  }

  public void setItem(FunctionToolCallOutput item) {
    this.item = item;
  }

  public void setItem(ModelOutput item) {
    this.item = item;
  }
  /**
   * @param item the model input item
   * @deprecated use the typed setters
   */
  //  @Deprecated  public void setItem(ModelInputItemPayload item) {    this.item = item;  }
}
