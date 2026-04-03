package com.xai.api.responses.input.part.content.item;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInputContentItemFile extends ModelInputContentItem {

  /**
   * "The file ID from the Files API.\n\nSet this to reference a
   * previously-uploaded file."
   */
  @JsonProperty("file_id")
  private String fileId;

  /**
   * "Inline file bytes (base64 encoded).
   * <p>
   * When set, the file content is provided directly in the request and does NOT
   * need to be uploaded to the Files API first.
   * <p>
   * Exactly one of `file_id` or `file_data` should be set."
   */
  @JsonProperty("file_data")
  private String fileData;
  /**
   * "Filename for inline uploads.\n\nRequired when `file_data` is set."
   */
  @JsonProperty("filename")
  private String filename;
  /**
   * 'Optional MIME type for inline uploads (e.g. "application/pdf").'
   */
  @JsonProperty("mime_type")
  private String mimeType;

  public ModelInputContentItemFile() {
    super("file");
  }

  public String getFileData() {
    return fileData;
  }

  public void setFileData(String fileData) {
    this.fileData = fileData;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

}
