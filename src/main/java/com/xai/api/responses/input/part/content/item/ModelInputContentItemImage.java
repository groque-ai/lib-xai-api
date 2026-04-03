package com.xai.api.responses.input.part.content.item;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInputContentItemImage extends ModelInputContentItem {

  @Deprecated
  @JsonProperty("file_id")
  private String fileId;	// "Only included for compatibility."

  @JsonProperty("image_url")
  private String imageUrl; // "A public URL of image prompt, only available for vision models."

  public ModelInputContentItemImage() {
    super("image");
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

}
