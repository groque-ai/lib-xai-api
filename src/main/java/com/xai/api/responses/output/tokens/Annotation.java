package com.xai.api.responses.output.tokens;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Annotation {

  /**
   * The type of the annotation. Only supported type currently is
   * `url_citation`.
   */
  @JsonProperty("type")
  private String // "The type of the annotation. Only supported type currently is `url_citation`."
    type;

  /**
   * The URL of the web resource.
   */
  @JsonProperty("url")
  private String // "The URL of the web resource."
    url;

  /**
   * The summary of the annotation.
   */
  @JsonProperty("start_index")
  private Integer startIndex;

  /**
   * The end index of the annotation.
   */
  @JsonProperty("end_index")
  private Integer endIndex;

  /**
   * The title of the annotation.
   */
  @JsonProperty("title")
  private String title;

  public Integer getEndIndex() {
    return endIndex;
  }

  public void setEndIndex(Integer endIndex) {
    this.endIndex = endIndex;
  }

  public Integer getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(Integer startIndex) {
    this.startIndex = startIndex;
  }

  /**
   * Returns The title of the annotation.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets The title of the annotation.
   *
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns The type of the annotation. Only supported type currently is
   * `url_citation`.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets The type of the annotation. Only supported type currently is
   * `url_citation`.
   *
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns The URL of the web resource.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets The URL of the web resource.
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }
}
