package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.type.ModelToolType;
import java.util.List;

public class KnowledgeBaseSearchTool extends ModelTool {

  @JsonProperty("vector_store_ids")
  private List<String> vectorStoreIds;

  @JsonProperty("max_num_results")
  private Integer maxNumResults;

  @JsonProperty("filters")
  private Object filters;

  @JsonProperty("ranking_options")
  private Object rankingOptions;

  public KnowledgeBaseSearchTool() {
    super(ModelToolType.knowledge_search);
  }

  public Object getFilters() {
    return filters;
  }

  public void setFilters(Object filters) {
    this.filters = filters;
  }

  public Integer getMaxNumResults() {
    return maxNumResults;
  }

  public void setMaxNumResults(Integer maxNumResults) {
    this.maxNumResults = maxNumResults;
  }

  public Object getRankingOptions() {
    return rankingOptions;
  }

  public void setRankingOptions(Object rankingOptions) {
    this.rankingOptions = rankingOptions;
  }

  public List<String> getVectorStoreIds() {
    return vectorStoreIds;
  }

  public void setVectorStoreIds(List<String> vectorStoreIds) {
    this.vectorStoreIds = vectorStoreIds;
  }
}
