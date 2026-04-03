package com.xai.api.images.dto;

/**
 * "Response format to return the image in. Can be url or b64_json. If b64_json
 * is specified, the image will be returned as a base64-encoded string instead
 * of a url to the generated image file."
 *
 * @author Key Bridge
 */
public enum ImageResponseFormat {
  url,
  /**
   * If b64_json is specified, the image will be returned as a base64-encoded
   * string instead of a url to the generated image file.
   */
  b64_json;

}
