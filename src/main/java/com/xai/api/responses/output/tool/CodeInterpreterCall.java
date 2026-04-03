package com.xai.api.responses.output.tool;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.type.OutputType;
import java.util.List;

@JsonTypeName("code_interpreter_call")
public class CodeInterpreterCall extends ModelOutput {

  /**
   * The code of the code interpreter tool call.
   */
  private String code;

  /**
   * The outputs of the code interpreter tool call.
   */
  private List<Object> outputs;

  public CodeInterpreterCall() {
    super(OutputType.code_interpreter_call);
  }

  /**
   * Returns The code of the code interpreter tool call.
   *
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * Sets The code of the code interpreter tool call.
   *
   * @param code the code
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Returns The outputs of the code interpreter tool call.
   *
   * @return the outputs
   */
  public List<Object> getOutputs() {
    return outputs;
  }

  /**
   * Sets The outputs of the code interpreter tool call.
   *
   * @param outputs the outputs
   */
  public void setOutputs(List<Object> outputs) {
    this.outputs = outputs;
  }
}
