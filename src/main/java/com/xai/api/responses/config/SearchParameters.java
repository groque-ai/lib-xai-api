package com.xai.api.responses.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.responses.config.search.SearchSource;
import java.time.LocalDate;
import java.util.List;

public class SearchParameters {

  /**
   * Date from which to consider the results in ISO-8601 YYYY-MM-DD. See
   * <https://en.wikipedia.org/wiki/ISO_8601>.
   */
  @JsonProperty("from_date")
  private LocalDate fromDate;

  /**
   * Date up to which to consider the results in ISO-8601 YYYY-MM-DD. See
   * <https://en.wikipedia.org/wiki/ISO_8601>.
   */
  @JsonProperty("to_date")
  private LocalDate toDate;

  /**
   * Maximum number of search results to use.
   */
  @JsonProperty("max_search_results")
  private Integer maxSearchResults;

  /**
   * Choose the mode to query realtime data: * `off`: no search performed and no
   * external will be considered. * `on` (default): the model will search in
   * every sources for relevant data. * `auto`: the model choose whether to
   * search data or not and where to search the data.
   */
  @JsonProperty("mode")
  private String mode;

  /**
   * Whether to return citations in the response or not.
   */
  @JsonProperty("return_citations")
  private Boolean returnCitations;

  /**
   * List of sources to search in. If no sources specified, the model will look
   * over the web and X by default.
   */
  @JsonProperty("sources")
  private List<SearchSource> sources;

  public LocalDate getFromDate() {
    return fromDate;
  }

  public void setFromDate(LocalDate fromDate) {
    this.fromDate = fromDate;
  }

  public Integer getMaxSearchResults() {
    return maxSearchResults;
  }

  public void setMaxSearchResults(Integer maxSearchResults) {
    this.maxSearchResults = maxSearchResults;
  }

  /**
   * Returns Choose the mode to query realtime data: * `off`: no search
   * performed and no external will be considered. * `on` (default): the model
   * will search in every sources for relevant data. * `auto`: the model choose
   * whether to search data or not and where to search the data.
   *
   * @return the mode
   */
  public String getMode() {
    return mode;
  }

  /**
   * Sets Choose the mode to query realtime data: * `off`: no search performed
   * and no external will be considered. * `on` (default): the model will search
   * in every sources for relevant data. * `auto`: the model choose whether to
   * search data or not and where to search the data.
   *
   * @param mode the mode
   */
  public void setMode(String mode) {
    this.mode = mode;
  }

  public Boolean getReturnCitations() {
    return returnCitations;
  }

  public void setReturnCitations(Boolean returnCitations) {
    this.returnCitations = returnCitations;
  }

  /**
   * Returns List of sources to search in. If no sources specified, the model
   * will look over the web and X by default.
   *
   * @return the sources
   */
  public List<SearchSource> getSources() {
    return sources;
  }

  /**
   * Sets List of sources to search in. If no sources specified, the model will
   * look over the web and X by default.
   *
   * @param sources the sources
   */
  public void setSources(List<SearchSource> sources) {
    this.sources = sources;
  }

  public LocalDate getToDate() {
    return toDate;
  }

  public void setToDate(LocalDate toDate) {
    this.toDate = toDate;
  }
}
