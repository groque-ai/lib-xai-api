package com.xai.api.responses.output.web;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.type.OutputType;

@JsonTypeName("web_search_call")
public class WebSearchCall extends ModelOutput {

  /**
   * An object describing the specific action taken in this web search call.
   * Includes details on how the model used the web (search, open_page, find).
   */
  private WebSearchAction action;

  public WebSearchCall() {
    super(OutputType.web_search_call);
  }

  /**
   * Returns An object describing the specific action taken in this web search
   * call. Includes details on how the model used the web (search, open_page,
   * find).
   *
   * @return the action
   */
  public WebSearchAction getAction() {
    return action;
  }

  /**
   * Sets An object describing the specific action taken in this web search
   * call. Includes details on how the model used the web (search, open_page,
   * find).
   *
   * @param action the action
   */
  public void setAction(WebSearchAction action) {
    this.action = action;
  }
}
