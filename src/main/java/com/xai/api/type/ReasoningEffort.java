package com.xai.api.type;

/**
 * Enum representing different levels of reasoning effort that can be requested
 * when using models that support adjustable reasoning depth (e.g., Grok models
 * with thinking/reasoning modes).
 * <p>
 * Higher effort levels typically result in more thorough chain-of-thought, more
 * intermediate steps, better handling of complex/multi-step problems, but also
 * higher token usage, longer response time, and higher cost.
 */
public enum ReasoningEffort {

  /**
   * Minimal or no explicit reasoning. Fastest response, lowest token usage.
   * Suitable for simple questions, direct answers, creative writing, or when
   * speed is more important than depth.
   */
  none,
  /**
   * Light reasoning. A short chain-of-thought or quick verification step. Good
   * balance between speed and quality for moderately complex questions.
   */
  low,
  /**
   * Standard / default reasoning effort. Usually what the model uses when no
   * explicit effort level is specified. Solid reasoning with several
   * intermediate steps when needed.
   */
  medium,
  /**
   * High reasoning effort. More detailed breakdown, exploration of multiple
   * approaches, careful verification, handling of edge cases. Recommended for
   * math, science, coding, logic puzzles, planning, etc.
   */
  high,
  /**
   * Maximum reasoning effort (sometimes called "think hard", "deep reasoning",
   * etc.). Extremely thorough analysis, long chain-of-thought, multiple
   * verification passes, consideration of counter-arguments, very careful
   * step-by-step logic. Best for the hardest problems, but most expensive and
   * slowest.
   */
  maximum;

  /**
   * Determines if this reasoning effort level shows explicit reasoning.
   * <p>
   * Returns {@code true} unless the effort is {@link #none}.
   * <p>
   * @return {@code true} if reasoning is shown, {@code false} otherwise
   */
  public boolean showsReasoning() {
    return this != none;
  }

  /**
   * Provides the descriptive string for this reasoning effort level.
   * <p>
   * @return the description string
   */
  public String getDescription() {
    switch (this) {
      case none:
        return "No explicit reasoning steps – direct answer only";
      case low:
        return "Light reasoning – brief thinking if helpful";
      case medium:
        return "Standard reasoning – balanced depth and speed";
      case high:
        return "High reasoning – detailed step-by-step analysis";
      case maximum:
        return "Maximum reasoning – extremely thorough and careful";
      default:
        // Should never happen, but keeps JDK11 switch exhaustive
        throw new IllegalStateException("Unexpected value: " + this);
    }
  }
}
