package com.xai.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.responses.config.ModelResponseConfiguration;
import com.xai.api.responses.config.ReasoningConfiguration;
import com.xai.api.responses.config.SearchParameters;
import com.xai.api.responses.input.ModelInput;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.tool.ModelTool;
import com.xai.api.responses.tool.ModelToolChoice;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *  ModelRequest
 *  ├── ModelInput          (via input)
 *  ├── ReasoningConfiguration   (via reasoning)
 *  ├── SearchParameters         (via search_parameters)
 *  └── ModelResponseConfiguration   (via text)
 * </pre>
 *
 * @author Key Bridge
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelRequest {

  /**
   * Key Bridge extension recording the timestamp when this object was created.
   */
  @JsonIgnore
  private Instant created;

  /**
   * Model name for the model to use. Obtainable from
   * <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   */
  private String model;
  /**
   * REQUIRED: The input passed to the model The core prompt/messages array —
   * polymorphic text/image. The input passed to the model. Can be text, image
   * or file.
   */
  private ModelInput input;
  /**
   * The ID of the previous response from the model.
   */
  @JsonProperty("previous_response_id")
  private String previousResponseId;
  /**
   * Controls which (if any) tool is called by the model. `none` means the model
   * will not call any tool and instead generates a message. auto means the
   * model can pick between generating a message or calling one or more tools.
   * required means the model must call one or more tools. Specifying a
   * particular tool via `{"type": "function", "function": {"name":
   * "my_function"}}` forces the model to call that tool. `none` is the default
   * when no tools are present. `auto` is the default if tools are present.
   */
  @JsonProperty("tool_choice")
  private ModelToolChoice toolChoice;  // Tools (JSON-schema definitions)
  /**
   * A list of tools the model may call in JSON-schema. Currently, only
   * functions and web search are supported as tools. A max of 128 tools are
   * supported.`web_search_preview` tool, if specified, will be overridden by
   * `search_parameters`.
   */
  private Collection<ModelTool> tools;
  /**
   * Max number of tokens that can be generated in a response. This includes
   * both output and reasoning tokens.
   */
  @JsonProperty("max_output_tokens")
  private Integer maxOutputTokens;
  /**
   * Whether to allow the model to run parallel tool calls.
   */
  @JsonProperty("parallel_tool_calls")
  private Boolean parallelToolCalls;
  /**
   * Reasoning configuration. Only for reasoning models.
   */
  private ReasoningConfiguration reasoning;
  /**
   * Set the parameters to be used for searched data. Takes precedence over
   * `web_search_preview` tool if specified in the tools.
   */
  @JsonProperty("search_parameters")
  private SearchParameters searchParameters;
  /**
   * Whether to store the input message(s) and model response for later
   * retrieval.
   */
  private Boolean store;
  /**
   * If set, partial message deltas will be sent. Tokens will be sent as
   * data-only server-sent events as they become available, with the stream
   * terminated by a `data: [DONE]` message.
   */
  private Boolean stream;
  /**
   * What sampling temperature to use, between 0 and 2. Higher values like 0.8
   * will make the output more random, while lower values like 0.2 will make it
   * more focused and deterministic.
   */
  private Float temperature;
  /**
   * Whether to return log probabilities of the output tokens or not. If true,
   * returns the log probabilities of each output token returned in the content
   * of message.
   */
  private Boolean logprobs;
  /**
   * An integer between 0 and 8 specifying the number of most likely tokens to
   * return at each token position, each with an associated log probability.
   * logprobs must be set to true if this parameter is used.
   */
  @JsonProperty("top_logprobs")
  private Integer topLogprobs;
  /**
   * An alternative to sampling with `temperature`, called nucleus sampling,
   * where the model considers the results of the tokens with `top_p`
   * probability mass. So 0.1 means only the tokens comprising the top 10%
   * probability mass are considered. It is generally recommended to alter this
   * or `temperature` but not both.
   */
  @JsonProperty("top_p")
  private Double topP;
  /**
   * A unique identifier representing your end-user, which can help xAI to
   * monitor and detect abuse.
   */
  private String user;
  /**
   * For specifying the response format (json schema) Settings for customizing a
   * text response from the model.
   */
  private ModelResponseConfiguration text;
  /**
   * (Unsupported) Whether to process the response asynchronously in the
   * background.
   */
  @Deprecated
  private Boolean background;
  /**
   * Not supported. Only maintained for compatibility reasons.
   */
  @Deprecated
  private Map<String, Object> metadata;
  /**
   * Not supported. Only maintained for compatibility reasons.
   */
  @Deprecated
  @JsonProperty("service_tier")
  private String serviceTier;
  /**
   * Not supported. Only maintained for compatibility reasons.
   */
  @Deprecated
  private String truncation;
  /**
   * Additional output data to include (e.g. "reasoning.encrypted_content")
   * <p>
   * "What additional output data to include in the response. Currently the only
   * supported value is `reasoning.encrypted_content` which returns an encrypted
   * version of the reasoning tokens."
   */
  @Deprecated
  private List<String> include;
  /**
   * An alternate way to specify the system prompt. Note that this cannot be
   * used alongside `previous_response_id`, where the system prompt of the
   * previous message will be used.
   */
  @Deprecated
  private String instructions;
  /**
   * Plumbed to x-grok-conv-id for Open Responses compatibility, used for
   * routing.
   */
  @Deprecated
  @JsonProperty("prompt_cache_key")
  private String promptCacheKey;

  public ModelRequest() {
    this.created = Instant.now(Clock.systemUTC());
  }

  //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
  /**
   * @deprecated use {@link #getInputArray()}
   *
   * Returns The input passed to the model. Can be text, image or file.
   *
   * @return the input
   */
  @Deprecated
  public ModelInput getInput() {
    return input;
  }

  @JsonIgnore
  public ModelInputArray getInputArray() {
    if (input == null) {
      input = new ModelInputArray();
    }
    if (input instanceof ModelInputArray) {
      return (ModelInputArray) input;
    } else {
      return null;
    }
  }

  /**
   * Sets The input passed to the model. Can be text, image or file.
   *
   * @param input the input
   */
  public void setInput(ModelInput input) {
    this.input = input;
  }

  /**
   * Returns Whether to return log probabilities of the output tokens or not. If
   * true, returns the log probabilities of each output token returned in the
   * content of message.
   *
   * @return the logprobs
   */
  public Boolean getLogprobs() {
    return logprobs;
  }

  /**
   * Sets Whether to return log probabilities of the output tokens or not. If
   * true, returns the log probabilities of each output token returned in the
   * content of message.
   *
   * @param logprobs the logprobs
   */
  public void setLogprobs(Boolean logprobs) {
    this.logprobs = logprobs;
  }

  public Integer getMaxOutputTokens() {
    return maxOutputTokens;
  }

  public void setMaxOutputTokens(Integer maxOutputTokens) {
    this.maxOutputTokens = maxOutputTokens;
  }

  /**
   * Returns Model name for the model to use. Obtainable from
   * <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   *
   * @return the model
   */
  public String getModel() {
    return model;
  }

  /**
   * Sets Model name for the model to use. Obtainable from
   * <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   *
   * @param model the model
   */
  public void setModel(String model) {
    this.model = model;
  }

  public Boolean getParallelToolCalls() {
    return parallelToolCalls;
  }

  public void setParallelToolCalls(Boolean parallelToolCalls) {
    this.parallelToolCalls = parallelToolCalls;
  }

  public String getPreviousResponseId() {
    return previousResponseId;
  }

  public void setPreviousResponseId(String previousResponseId) {
    this.previousResponseId = previousResponseId;
  }

  /**
   * Returns Reasoning configuration. Only for reasoning models.
   *
   * @return the reasoning
   */
  public ReasoningConfiguration getReasoning() {
    return reasoning;
  }

  /**
   * Sets Reasoning configuration. Only for reasoning models.
   *
   * @param reasoning the reasoning
   */
  public void setReasoning(ReasoningConfiguration reasoning) {
    this.reasoning = reasoning;
  }

  /**
   * Returns Settings for customizing a text response from the model.
   *
   * @return the text
   */
  public ModelResponseConfiguration getText() {
    return text;
  }

  /**
   * Sets Settings for customizing a text response from the model.
   *
   * @param text the text
   */
  public void setText(ModelResponseConfiguration text) {
    this.text = text;
  }

  public SearchParameters getSearchParameters() {
    return searchParameters;
  }

  public void setSearchParameters(SearchParameters searchParameters) {
    this.searchParameters = searchParameters;
  }

  /**
   * Returns Whether to store the input message(s) and model response for later
   * retrieval.
   *
   * @return the store
   */
  public Boolean getStore() {
    return store;
  }

  /**
   * Sets Whether to store the input message(s) and model response for later
   * retrieval.
   *
   * @param store the store
   */
  public void setStore(Boolean store) {
    this.store = store;
  }

  /**
   * Returns If set, partial message deltas will be sent. Tokens will be sent as
   * data-only server-sent events as they become available, with the stream
   * terminated by a `data: [DONE]` message.
   *
   * @return the stream
   */
  public Boolean getStream() {
    return stream;
  }

  /**
   * Sets If set, partial message deltas will be sent. Tokens will be sent as
   * data-only server-sent events as they become available, with the stream
   * terminated by a `data: [DONE]` message.
   *
   * @param stream the stream
   */
  public void setStream(Boolean stream) {
    this.stream = stream;
  }

  /**
   * Returns What sampling temperature to use, between 0 and 2. Higher values
   * like 0.8 will make the output more random, while lower values like 0.2 will
   * make it more focused and deterministic.
   *
   * @return the temperature
   */
  public Float getTemperature() {
    return temperature;
  }

  /**
   * Sets What sampling temperature to use, between 0 and 2. Higher values like
   * 0.8 will make the output more random, while lower values like 0.2 will make
   * it more focused and deterministic.
   *
   * @param temperature the temperature
   */
  public void setTemperature(Float temperature) {
    this.temperature = temperature;
  }

  public ModelToolChoice getToolChoice() {
    return toolChoice;
  }

  public void setToolChoice(ModelToolChoice toolChoice) {
    this.toolChoice = toolChoice;
  }

  /**
   * Returns A list of tools the model may call in JSON-schema. Currently, only
   * functions and web search are supported as tools. A max of 128 tools are
   * supported.`web_search_preview` tool, if specified, will be overridden by
   * `search_parameters`.
   *
   * @return the tools
   */
  public Collection<ModelTool> getTools() {
    return tools;
  }

  /**
   * Sets A list of tools the model may call in JSON-schema. Currently, only
   * functions and web search are supported as tools. A max of 128 tools are
   * supported.`web_search_preview` tool, if specified, will be overridden by
   * `search_parameters`.
   *
   * @param tools the tools
   */
  public void setTools(Collection<ModelTool> tools) {
    this.tools = tools;
  }

  public Integer getTopLogprobs() {
    return topLogprobs;
  }

  public void setTopLogprobs(Integer topLogprobs) {
    this.topLogprobs = topLogprobs;
  }

  public Double getTopP() {
    return topP;
  }

  public void setTopP(Double topP) {
    this.topP = topP;
  }

  /**
   * Returns A unique identifier representing your end-user, which can help xAI
   * to monitor and detect abuse.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets A unique identifier representing your end-user, which can help xAI to
   * monitor and detect abuse.
   *
   * @param user the user
   */
  public void setUser(String user) {
    this.user = user;
  }
  //</editor-fold>}

  // Extensions
  /**
   * Key Bridge extension recording the timestamp when this object was created.
   *
   * @return the timestamp
   */
  public Instant getCreated() {
    return created;
  }

}
