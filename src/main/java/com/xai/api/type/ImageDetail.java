package com.xai.api.type;

/**
 * Detail level for image processing in vision requests. Controls how much
 * detail the model pays attention to in the image.
 * <p>
 * <p>
 * Corresponds to the "detail" field in image_url objects.
 * <p>
 * <ul>
 * <li>{@link #LOW} - Faster, lower resolution (good for simple images or when
 * speed matters)</li>
 * <li>{@link #HIGH} - Higher resolution, more tokens used (best for detailed
 * analysis, charts, small text)</li>
 * <li>{@link #AUTO} - Model decides based on image complexity
 * (default/recommended in most cases)</li>
 * </ul>
 */
public enum ImageDetail {
  /**
   * Automatic mode: the model chooses the appropriate detail level based on the
   * image. This is usually the best default choice.
   */
  auto,
  /**
   * Low detail mode: faster processing, fewer tokens, suitable for simple or
   * large-scale images.
   */
  low,
  /**
   * High detail mode: more precise analysis, higher token cost, ideal for fine
   * details, text in images, diagrams.
   */
  high;

}
