package com.llamacpp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;

/**
 * Immutable configuration class for the Llama.cpp HTTP client.
 * <p>
 * Encapsulates connection and request behavior including API authentication,
 * base URL, timeouts, retry policy, and redirect handling. Instances are
 * created exclusively through the {@link Builder} and are guaranteed to be
 * immutable after construction.
 * <p>
 * Configuration is resolved from multiple sources. Environment variables take
 * precedence over properties defined in the user's {@code ~/.llamacpp} file.
 * Missing or invalid values fall back to documented defaults.
 * <p>
 * <strong>Thread safety:</strong> This class is immutable and therefore
 * thread-safe for concurrent read access. The nested {@link Builder} is not
 * thread-safe.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-09
 */
public class LlamaClientConfig {

  /**
   * Filename of the optional user configuration file located in the home
   * directory.
   */
  private static final String DOT_FILE = ".llamacpp";

  /**
   * Default base URL used when no explicit value is configured. Trailing
   * slashes are always removed during normalization.
   */
  private static final String DEFAULT_BASE_URL = "http:";

  /**
   * The API key to be used for authentication. Stored as {@code null} when no
   * key is present.
   */
  private final String apiKey;

  /**
   * Indicates whether a non-empty API key was successfully configured. This
   * flag avoids repeated null/empty checks by callers.
   */
  private final boolean hasApiKey;

  /**
   * The normalized base URL for all requests. Never {@code null} and never ends
   * with a trailing slash.
   */
  private final String baseUrl;

  /**
   * Timeout used when establishing a connection to the server. Never
   * {@code null}.
   */
  private final Duration connectTimeout;

  /**
   * Timeout used for the complete request/response cycle. Never {@code null}.
   */
  private final Duration requestTimeout;

  /**
   * Maximum number of retry attempts for transient failures.
   */
  private final int maxRetries;

  /**
   * Whether the client should automatically follow HTTP redirects.
   */
  private final boolean followRedirects;

  /**
   * Constructs an immutable configuration instance from the given builder.
   * <p>
   * Performs normalization of the API key and base URL, and applies default
   * timeout values when the builder did not explicitly set them.
   *
   * @param builder the builder containing raw configuration values
   */
  private LlamaClientConfig(Builder builder) {
    // Developer note: Trimming occurs here rather than in the builder so that
    // all normalization logic is centralized in one place and cannot be bypassed.
    String key = builder.apiKey;
    if (key != null) {
      key = key.trim();
    }

    this.hasApiKey = key != null && !key.isEmpty();
    this.apiKey = this.hasApiKey ? key : null;
    this.baseUrl = builder.baseUrl != null && !builder.baseUrl.isBlank()
                   ? trimSlash(builder.baseUrl)
                   : DEFAULT_BASE_URL;
    this.connectTimeout = builder.connectTimeout != null ? builder.connectTimeout : Duration.ofSeconds(10);
    this.requestTimeout = builder.requestTimeout != null ? builder.requestTimeout : Duration.ofSeconds(60);
    this.maxRetries = builder.maxRetries;
    this.followRedirects = builder.followRedirects;
  }

  /**
   * Removes a single trailing forward slash from the URL if present.
   * <p>
   * This normalization ensures consistent URL handling across all configuration
   * sources.
   *
   * @param url the URL to normalize
   * @return the URL without a trailing slash
   */
  private static String trimSlash(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  /**
   * Creates a configuration using the default sources.
   * <p>
   * Equivalent to calling {@link #fromSources(Properties, Function)} with the
   * contents of {@code ~/.llamacpp} (if readable) and {@link System#getenv()}.
   *
   * @return a new immutable {@code LlamaClientConfig}
   */
  public static LlamaClientConfig readConfig() {
    return fromSources(readDotFile(), System::getenv);
  }

  /**
   * Constructs a configuration by merging file properties and environment
   * variables.
   * <p>
   * Environment variables have higher precedence than properties loaded from
   * the dot file. The following variable/property pairs are recognized:
   * <ul>
   * <li>{@code LLAMACPP_API_KEY} / {@code API_KEY}</li>
   * <li>{@code LLAMACPP_BASE_URL} / {@code BASE_URL}</li>
   * <li>{@code LLAMACPP_CONNECT_TIMEOUT} / {@code CONNECT_TIMEOUT}</li>
   * <li>{@code LLAMACPP_REQUEST_TIMEOUT} / {@code REQUEST_TIMEOUT}</li>
   * <li>{@code LLAMACPP_FOLLOW_REDIRECTS} / {@code FOLLOW_REDIRECTS}</li>
   * <li>{@code LLAMACPP_MAX_RETRIES} / {@code MAX_RETRIES}</li>
   * </ul>
   * <p>
   * Invalid numeric values are ignored and the builder default is used instead.
   *
   * @param fileProps properties loaded from {@code ~/.llamacpp}, may be
   *                  {@code null}
   * @param env       function used to look up environment variables, may be
   *                  {@code null}
   * @return a new immutable configuration instance
   */
  public static LlamaClientConfig fromSources(Properties fileProps, Function<String, String> env) {
    Properties props = fileProps != null ? fileProps : new Properties();
    // Developer note: Defaulting the environment function prevents NullPointerException
    // when callers pass null while still allowing test injection of a controlled map.
    Function<String, String> getenv = env != null ? env : k -> null;
    Builder b = new Builder();
    b.withApiKey(first(getenv.apply("LLAMACPP_API_KEY"), props.getProperty("API_KEY")));
    b.withBaseUrl(first(getenv.apply("LLAMACPP_BASE_URL"), props.getProperty("BASE_URL")));
    Duration connect = parseSeconds(first(getenv.apply("LLAMACPP_CONNECT_TIMEOUT"), props.getProperty("CONNECT_TIMEOUT")));
    if (connect != null) {
      b.withConnectTimeout(connect);
    }
    Duration request = parseSeconds(first(getenv.apply("LLAMACPP_REQUEST_TIMEOUT"), props.getProperty("REQUEST_TIMEOUT")));
    if (request != null) {
      b.withRequestTimeout(request);
    }
    String follow = first(getenv.apply("LLAMACPP_FOLLOW_REDIRECTS"), props.getProperty("FOLLOW_REDIRECTS"));
    if (follow != null) {
      b.withFollowRedirects(isTrue(follow));
    }
    String retries = first(getenv.apply("LLAMACPP_MAX_RETRIES"), props.getProperty("MAX_RETRIES"));
    if (retries != null) {
      try {
        b.withMaxRetries(Integer.parseInt(retries.trim()));
      } catch (NumberFormatException ignored) {
        // Developer note: Invalid retry values are silently ignored so that
        // a misconfigured environment does not prevent the client from starting.
      }
    }
    return b.build();
  }

  /**
   * Reads the optional dot file from the current user's home directory.
   * <p>
   * Returns an empty {@code Properties} object if the file does not exist, is
   * not readable, or if an I/O error occurs. I/O errors are reported to
   * {@link System#err} but never thrown to the caller.
   *
   * @return properties loaded from the dot file, never {@code null}
   */
  private static Properties readDotFile() {
    Properties props = new Properties();
    String home = System.getProperty("user.home");
    if (home == null) {
      return props;
    }
    File dotFile = new File(home, DOT_FILE);
    if (!dotFile.isFile() || !dotFile.canRead()) {
      return props;
    }
    try (FileInputStream fis = new FileInputStream(dotFile)) {
      props.load(fis);
    } catch (IOException e) {
      System.err.println("Warning: Could not read ~/.llamacpp: " + e.getMessage());
    }
    return props;
  }

  /**
   * Returns the first non-blank value after trimming, preferring the
   * environment value.
   * <p>
   * Used to implement strict precedence of environment variables over file
   * properties.
   *
   * @param envVal  value obtained from the environment (higher priority)
   * @param fileVal value obtained from the properties file
   * @return the first usable value, or {@code null} if both are absent or blank
   */
  private static String first(String envVal, String fileVal) {
    if (envVal != null && !envVal.trim().isEmpty()) {
      return envVal.trim();
    }
    if (fileVal != null && !fileVal.trim().isEmpty()) {
      return fileVal.trim();
    }
    return null;
  }

  /**
   * Parses the given string as a number of seconds.
   * <p>
   * Returns {@code null} for {@code null} input or any value that cannot be
   * parsed as a {@code long}.
   *
   * @param value the textual representation of seconds
   * @return a {@code Duration} or {@code null} if parsing fails
   */
  private static Duration parseSeconds(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Duration.ofSeconds(Long.parseLong(value.trim()));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Evaluates common boolean representations in a case-insensitive manner.
   *
   * @param value the string to evaluate
   * @return {@code true} if the value represents a positive boolean
   */
  private static boolean isTrue(String value) {
    String trimmed = value.trim().toLowerCase();
    return "true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed);
  }

  /**
   * Returns the API key if one was configured.
   *
   * @return the configured API key, or {@code null} if none was provided
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Indicates whether a non-empty API key is available.
   *
   * @return {@code true} if an API key has been configured
   */
  public boolean hasApiKey() {
    return hasApiKey;
  }

  /**
   * Returns the base URL for all API requests.
   * <p>
   * The returned value is guaranteed never to end with a trailing slash.
   *
   * @return the base URL, never {@code null}
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the timeout used when establishing new connections.
   *
   * @return the connect timeout, never {@code null}
   */
  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Returns the maximum time allowed for a complete request.
   *
   * @return the request timeout, never {@code null}
   */
  public Duration getRequestTimeout() {
    return requestTimeout;
  }

  /**
   * Returns the maximum number of times a request will be retried.
   *
   * @return configured maximum retry count
   */
  public int getMaxRetries() {
    return maxRetries;
  }

  /**
   * Returns whether the client should follow HTTP 3xx redirects.
   *
   * @return {@code true} if redirects are followed automatically
   */
  public boolean isFollowRedirects() {
    return followRedirects;
  }

  /**
   * Fluent builder for {@link LlamaClientConfig}.
   * <p>
   * Default values are:
   * <ul>
   * <li>{@code maxRetries} = 3</li>
   * <li>{@code followRedirects} = true</li>
   * <li>{@code connectTimeout} = 10 seconds (applied at build time if
   * unset)</li>
   * <li>{@code requestTimeout} = 60 seconds (applied at build time if
   * unset)</li>
   * </ul>
   * <p>
   * <strong>Thread safety:</strong> This builder is not thread-safe. Each
   * instance should be used by only one thread.
   */
  public static class Builder {

    private String apiKey;
    private String baseUrl;
    private Duration connectTimeout;
    private Duration requestTimeout;
    private int maxRetries = 3;
    private boolean followRedirects = true;

    /**
     * Sets the API key to be used for authentication.
     *
     * @param apiKey the API key (may be {@code null} or blank)
     * @return this builder for method chaining
     */
    public Builder withApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * Sets the base URL of the Llama.cpp server.
     * <p>
     * A trailing slash, if present, will be removed during {@code build()}.
     *
     * @param baseUrl the server base URL
     * @return this builder for method chaining
     */
    public Builder withBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Sets the connection establishment timeout.
     * <p>
     * If not set, a default of 10 seconds is applied when {@code build()} is
     * called.
     *
     * @param timeout the desired connect timeout, or {@code null} for default
     * @return this builder for method chaining
     */
    public Builder withConnectTimeout(Duration timeout) {
      this.connectTimeout = timeout;
      return this;
    }

    /**
     * Sets the maximum duration for a single request.
     * <p>
     * If not set, a default of 60 seconds is applied when {@code build()} is
     * called.
     *
     * @param timeout the desired request timeout, or {@code null} for default
     * @return this builder for method chaining
     */
    public Builder withRequestTimeout(Duration timeout) {
      this.requestTimeout = timeout;
      return this;
    }

    /**
     * Sets the maximum number of retry attempts.
     *
     * @param retries the number of retries (must be non-negative)
     * @return this builder for method chaining
     */
    public Builder withMaxRetries(int retries) {
      this.maxRetries = retries;
      return this;
    }

    /**
     * Configures whether HTTP redirects should be followed.
     *
     * @param follow {@code true} to follow redirects
     * @return this builder for method chaining
     */
    public Builder withFollowRedirects(boolean follow) {
      this.followRedirects = follow;
      return this;
    }

    /**
     * Creates an immutable {@link LlamaClientConfig} from the current builder
     * state.
     *
     * @return a new configuration instance
     */
    public LlamaClientConfig build() {
      return new LlamaClientConfig(this);
    }
  }
}
