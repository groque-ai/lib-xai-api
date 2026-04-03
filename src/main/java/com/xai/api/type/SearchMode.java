package com.xai.api.type;

/**
 * Enum representing different search modes or strategies that can be used when
 * configuring search parameters in the xAI API (e.g. in the "search_parameters"
 * field of ModelRequest).
 * <p>
 * These values control how the model performs web/live/document search when
 * tools or search features are enabled. The exact semantics depend on the model
 * and API version, but this enum provides a clean, type-safe way to specify
 * them.
 * <p>
 * Not all models support all modes — some (especially newer reasoning-focused
 * ones) may ignore this field or always use a fixed behavior.
 * <p>
 * The names (AUTO, QUICK, STANDARD, DEEP, MAXIMUM, NONE) are modeled after
 * common patterns in modern LLM APIs (e.g. search depth in Perplexity, Grok’s
 * own live search tiers, Anthropic tool use, etc.).
 * <p>
 * xAI’s actual search_parameters is currently quite minimal (often just
 * max_results or source filters), and many models ignore complex modes.
 */
/**
 * Enum representing different search modes for the xAI API's search_parameters.
 * Values are lowercase to match typical API expectations (e.g. "auto", "deep",
 * "none"). You can pass enum.name() directly into the map.
 */
public enum SearchMode {

  /**
   * Automatic: model decides search depth and behavior
   */
  auto,
  /**
   * Quick: fast, lightweight search with minimal sources
   */
  quick,
  /**
   * Standard: balanced speed, depth and cost (often default)
   */
  standard,
  /**
   * Deep: more thorough search, more sources, higher quality
   */
  deep,
  /**
   * Maximum: exhaustive search — most sources, highest cost/time
   */
  maximum,
  /**
   * None: disable external / live search completely
   */
  none;

  /**
   * Whether this mode is expected to trigger actual external search.
   */
  public boolean performsSearch() {
    return this != none;
  }

  /**
   * * Short description for logging / debugging / documentation.
   */
  public String getDescription() {
    switch (this) {
      case auto:
        return "Automatic: model decides search depth";
      case quick:
        return "Quick: fast, lightweight search";
      case standard:
        return "Standard: balanced speed and depth";
      case deep:
        return "Deep: thorough search with more sources";
      case maximum:
        return "Maximum: exhaustive, comprehensive search";
      case none:
        return "None: no external search performed";
      default:
        throw new IllegalStateException("Unexpected value: " + this);
    }
  }
}
