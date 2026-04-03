package com.xai.api.collections.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchRequest {

  /**
   * Optional metadata filter string to apply to search results. Uses AIP-160
   * filter syntax for querying document metadata. Supports comparison
   * operators: `=`, `!=`, `>`, `>=`, `<`, `<=`
   * Supports logical operators: `AND`, `OR`
   * Supports range syntax: `field:10..20` (inclusive)
   * Examples: `author = "John"` or `year > 2020 AND category = "finance"`
   */
  @JsonProperty("filter")
  private String filter;

  /**
   * User-defined instructions to be included in the search query. Defaults to
   * generic search instructions.
   */
  @JsonProperty("instructions")
  private String instructions;

  /**
   * The number of chunks to return. Will always return the top matching chunks.
   * Optional, defaults to 10.
   */
  @JsonProperty("limit")
  private Integer limit;

  /**
   * The query to search for which will be embedded using the same embedding
   * model as the one used for the source to query.
   */
  @JsonProperty("query")
  private String query;

  /**
   * Deprecated: Metric now comes from collection creation.
   */
  @JsonProperty("ranking_metric")
  private String rankingMetric;

  /**
   * How to perform the document search. Defaults to hybrid retrieval.
   */
  @JsonProperty("retrieval_mode")
  private String retrievalMode;

  /**
   * The source to query.
   */
  @JsonProperty("source")
  private DocumentsSource source;

  /**
   * Returns Optional metadata filter string to apply to search results. Uses
   * AIP-160 filter syntax for querying document metadata. Supports comparison
   * operators: `=`, `!=`, `>`, `>=`, `<`, `<=`
   * Supports logical operators: `AND`, `OR`
   * Supports range syntax: `field:10..20` (inclusive)
   * Examples: `author = "John"` or `year > 2020 AND category = "finance"`
   *
   * @return the filter
   */
  public String getFilter() {
    return filter;
  }

  /**
   * Sets Optional metadata filter string to apply to search results. Uses
   * AIP-160 filter syntax for querying document metadata. Supports comparison
   * operators: `=`, `!=`, `>`, `>=`, `<`, `<=`
   * Supports logical operators: `AND`, `OR`
   * Supports range syntax: `field:10..20` (inclusive)
   * Examples: `author = "John"` or `year > 2020 AND category = "finance"`
   *
   * @param filter the filter
   */
  public void setFilter(String filter) {
    this.filter = filter;
  }

  /**
   * Returns User-defined instructions to be included in the search query.
   * Defaults to generic search instructions.
   *
   * @return the instructions
   */
  public String getInstructions() {
    return instructions;
  }

  /**
   * Sets User-defined instructions to be included in the search query. Defaults
   * to generic search instructions.
   *
   * @param instructions the instructions
   */
  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  /**
   * Returns The number of chunks to return. Will always return the top matching
   * chunks. Optional, defaults to 10.
   *
   * @return the limit
   */
  public Integer getLimit() {
    return limit;
  }

  /**
   * Sets The number of chunks to return. Will always return the top matching
   * chunks. Optional, defaults to 10.
   *
   * @param limit the limit
   */
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  /**
   * Returns The query to search for which will be embedded using the same
   * embedding model as the one used for the source to query.
   *
   * @return the query
   */
  public String getQuery() {
    return query;
  }

  /**
   * Sets The query to search for which will be embedded using the same
   * embedding model as the one used for the source to query.
   *
   * @param query the query
   */
  public void setQuery(String query) {
    this.query = query;
  }

  public String getRankingMetric() {
    return rankingMetric;
  }

  public void setRankingMetric(String rankingMetric) {
    this.rankingMetric = rankingMetric;
  }

  public String getRetrievalMode() {
    return retrievalMode;
  }

  public void setRetrievalMode(String retrievalMode) {
    this.retrievalMode = retrievalMode;
  }

  /**
   * Returns The source to query.
   *
   * @return the source
   */
  public DocumentsSource getSource() {
    return source;
  }

  /**
   * Sets The source to query.
   *
   * @param source the source
   */
  public void setSource(DocumentsSource source) {
    this.source = source;
  }
}
