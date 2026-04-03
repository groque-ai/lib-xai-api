package com.xai.api.responses.part;

// Nested object inside image_url part
import com.xai.api.type.ImageDetail;

public class ImageUrl {

  /**
   * URL of the image.
   */
  private String url;

  // "low", "high", "auto" – optional
  private ImageDetail detail;

  public ImageUrl() {
  }

  public ImageUrl(String url) {
    this.url = url;
    this.detail = ImageDetail.auto;
  }

  public ImageUrl(String url, ImageDetail detail) {
    this.url = url;
    this.detail = detail;
  }

  public ImageDetail getDetail() {
    return detail;
  }

  public void setDetail(ImageDetail detail) {
    this.detail = detail;
  }

  /**
   * Returns URL of the image.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets URL of the image.
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }
}
