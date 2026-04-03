package com.xai.api.collections.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DocumentsSource {

  /**
   * The collection IDs to search in.
   */
  @JsonProperty("collection_ids")
  private List<String> collectionIds;

  public List<String> getCollectionIds() {
    return collectionIds;
  }

  public void setCollectionIds(List<String> collectionIds) {
    this.collectionIds = collectionIds;
  }
}
