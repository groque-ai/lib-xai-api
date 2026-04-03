package com.xai.api.collections.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class SearchMatch {

  /**
   * The chunk content.
   */
  @JsonProperty("chunk_content")
  private String chunkContent;

  /**
   * The chunk ID.
   */
  @JsonProperty("chunk_id")
  private String chunkId;

  /**
   * The collection ID(s).
   */
  @JsonProperty("collection_ids")
  private List<String> collectionIds;

  /**
   * Metadata fields belonging to the document of this chunk.
   */
  @JsonProperty("fields")
  private Map<String, String> // Metadata fields belonging to the document of this chunk."
    fields;

  /**
   * The document ID.
   */
  @JsonProperty("file_id")
  private String fileId;

  /**
   * The relevance score.
   */
  @JsonProperty("score")
  private float score;

  public String getChunkContent() {
    return chunkContent;
  }

  public void setChunkContent(String chunkContent) {
    this.chunkContent = chunkContent;
  }

  public String getChunkId() {
    return chunkId;
  }

  public void setChunkId(String chunkId) {
    this.chunkId = chunkId;
  }

  public List<String> getCollectionIds() {
    return collectionIds;
  }

  public void setCollectionIds(List<String> collectionIds) {
    this.collectionIds = collectionIds;
  }

  /**
   * Returns Metadata fields belonging to the document of this chunk.
   *
   * @return the fields
   */
  public Map<String, String> getFields() {
    return fields;
  }

  /**
   * Sets Metadata fields belonging to the document of this chunk.
   *
   * @param fields the fields
   */
  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  /**
   * Returns The relevance score.
   *
   * @return the score
   */
  public float getScore() {
    return score;
  }

  /**
   * Sets The relevance score.
   *
   * @param score the score
   */
  public void setScore(float score) {
    this.score = score;
  }
}
