package com.xai.api.responses.part;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.type.ImageDetail;

@JsonTypeName("image_url")
public class ImageUrlContentPart extends ContentPart {

  @JsonProperty("image_url")
  private ImageUrl imageUrl;

  public ImageUrlContentPart() {
    super("image_url");
  }

  public ImageUrlContentPart(String url) {
    super("image_url");
    this.imageUrl = new ImageUrl(url, ImageDetail.auto);
  }

  public ImageUrlContentPart(String url, ImageDetail detail) {
    super("image_url");
    this.imageUrl = new ImageUrl(url, detail);
  }

  public ImageUrl getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(ImageUrl imageUrl) {
    this.imageUrl = imageUrl;
  }

}
