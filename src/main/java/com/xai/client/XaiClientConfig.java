package com.xai.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuration class for the XAI API client.
 * <p>
 * This class encapsulates the configuration parameters required to interact
 * with the XAI API. It is designed to be immutable and thread-safe, ensuring
 * that once created, its state cannot be modified. Instances of this class are
 * created using the {@link Builder} pattern for programmatic configuration or
 * via the {@link #readConfig()} method, which reads configuration from
 * environment variables and a dotfile.
 * <p>
 * Concurrency assumptions: This class is thread-safe because it is immutable.
 * Multiple threads can safely access and read its properties concurrently
 * without the need for external synchronization.
 * <p>
 * Resource management: This class does not hold any resources that require
 * explicit closing. All fields are either primitive types or immutable objects
 * (e.g., {@link String}, {@link Duration}).
 * <p>
 * Usage notes: Ensure that the API key is provided either through environment
 * variables or the dotfile. The dotfile is located at {@code ~/.grok} and
 * should contain key-value pairs in properties format. Environment variables
 * take precedence over the dotfile values.
 */
public class XaiClientConfig {

  /**
   * The prefix for environment variables used by this configuration.
   */
  private static final String ENV_PREFIX = "GROK_";

  /**
   * The name of the dotfile used for configuration, located in the user's home
   * directory.
   */
  private static final String DOT_FILE = ".grok";

  /**
   * The API key used for authentication with the XAI service.
   */
  private String apiKey;

  /**
   * The base URL for the XAI API endpoints.
   */
  private String baseUrl;

  /**
   * The timeout duration for establishing a connection to the API.
   */
  private Duration connectTimeout;

  /**
   * The timeout duration for completing an API request.
   */
  private Duration requestTimeout;

  /**
   * The maximum number of retries for failed API requests.
   */
  private int maxRetries;

  /**
   * Whether to follow HTTP redirects automatically.
   */
  private boolean followRedirects;

  /**
   * Private constructor to create an instance from the builder.
   * <p>
   * This constructor sets the fields from the builder, providing default values
   * where applicable.
   *
   * @param builder the builder containing the configuration parameters
   * @throws IllegalArgumentException if the builder is null or contains invalid
   *                                  parameters
   */
  @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
  private XaiClientConfig(Builder builder) {
    this.apiKey = builder.apiKey;
    this.baseUrl = builder.baseUrl != null ? builder.baseUrl : "https://api.x.ai/v1";
    this.connectTimeout = builder.connectTimeout != null ? builder.connectTimeout : Duration.ofSeconds(10);
    this.requestTimeout = builder.requestTimeout != null ? builder.requestTimeout : Duration.ofSeconds(60);
    this.maxRetries = builder.maxRetries;
    this.followRedirects = builder.followRedirects;
  }

  /**
   * Returns the API key.
   * <p>
   * This is the key used for authenticating requests to the XAI API.
   *
   * @return the API key as a string
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Returns the base URL for the XAI API.
   * <p>
   * This URL is used as the prefix for all API endpoints.
   *
   * @return the base URL as a string
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the connection timeout duration.
   * <p>
   * This specifies the maximum time to wait for establishing a connection.
   *
   * @return the connection timeout as a {@link Duration}
   */
  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Returns the request timeout duration.
   * <p>
   * This specifies the maximum time to wait for a complete API response.
   *
   * @return the request timeout as a {@link Duration}
   */
  public Duration getRequestTimeout() {
    return requestTimeout;
  }

  /**
   * Returns the maximum number of retries for API requests.
   * <p>
   * This indicates how many times a failed request should be retried before
   * giving up.
   *
   * @return the maximum number of retries as an integer
   */
  public int getMaxRetries() {
    return maxRetries;
  }

  /**
   * Returns whether HTTP redirects are followed.
   * <p>
   * If true, the client will automatically follow HTTP redirects.
   *
   * @return true if redirects are followed, false otherwise
   */
  public boolean isFollowRedirects() {
    return followRedirects;
  }

  /**
   * Creates a configuration instance by reading from environment variables and
   * a dotfile.
   * <p>
   * This method attempts to load configuration from the following sources, in
   * order of precedence: environment variables prefixed with "GROK_", and then
   * from a properties file at {@code ~/.grok}. Environment variables override
   * dotfile values. The API key is mandatory and must be provided either via
   * {@code GROK_API_KEY} environment variable or in the dotfile.
   * <p>
   * If the API key is missing or empty, an {@link IllegalStateException} is
   * thrown. Other parameters have sensible defaults if not specified.
   * <p>
   * Concurrency: This method is not thread-safe if the environment or dotfile
   * changes during execution, but typically these are static. It performs file
   * I/O, so it should not be called frequently.
   *
   * @return a new {@link XaiClientConfig} instance populated from environment
   *         and dotfile
   * @throws IllegalStateException if the API key is not provided or is empty
   */
  public static XaiClientConfig readConfig() {
    Properties props = readDotFile();

    Builder builder = new Builder();

    // Env vars override .grok file
    String apiKey = getEnvOrDot("API_KEY", props);
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new IllegalStateException("GROK_API_KEY is required (env var or .grok file)");
    }
    builder.apiKey(apiKey);

    builder.baseUrl(getEnvOrDot("BASE_URL", props));
    builder.connectTimeout(getEnvDuration("CONNECT_TIMEOUT", props));
    builder.requestTimeout(getEnvDuration("REQUEST_TIMEOUT", props));
    builder.followRedirects(getEnvBoolean("FOLLOW_REDIRECTS", props, true));

    return builder.build();
  }

  /**
   * Reads the properties from the dotfile located at {@code ~/.grok}.
   * <p>
   * If the file does not exist, is not readable, or cannot be loaded, an empty
   * properties object is returned. Any IO errors are logged to stderr but do
   * not throw exceptions.
   *
   * @return a {@link Properties} object containing the dotfile contents, or
   *         empty if not available
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
      System.err.println("Warning: Could not read ~/.grok: " + e.getMessage());
    }
    return props;
  }

  /**
   * Retrieves a value from either an environment variable or the dotfile
   * properties.
   * <p>
   * Environment variables take precedence. The environment variable name is
   * constructed as {@code ENV_PREFIX + keySuffix}. If the environment variable
   * is set and non-empty, its trimmed value is returned. Otherwise, the value
   * from the properties is returned, or null if not present.
   *
   * @param keySuffix the suffix for the key (e.g., "API_KEY")
   * @param props     the properties loaded from the dotfile
   * @return the value from env or props, or null if not found
   */
  private static String getEnvOrDot(String keySuffix, Properties props) {
    String envKey = ENV_PREFIX + keySuffix;
    String envVal = System.getenv(envKey);
    if (envVal != null && !envVal.trim().isEmpty()) {
      return envVal.trim();
    }
    return props.getProperty(envKey, null);
  }

  /**
   * Retrieves a duration value from environment or dotfile, parsing it as
   * seconds.
   * <p>
   * The value is expected to be a long integer representing seconds. If parsing
   * fails, a warning is printed to stderr and null is returned.
   *
   * @param keySuffix the suffix for the key
   * @param props     the properties
   * @return the parsed {@link Duration}, or null if invalid or missing
   */
  private static Duration getEnvDuration(String keySuffix, Properties props) {
    String value = getEnvOrDot(keySuffix, props);
    if (value == null) {
      return null;
    }
    try {
      long seconds = Long.parseLong(value.trim());
      return Duration.ofSeconds(seconds);
    } catch (NumberFormatException e) {
      System.err.println("Invalid duration for " + ENV_PREFIX + keySuffix + ": " + value);
      return null;
    }
  }

  /**
   * Retrieves a boolean value from environment or dotfile.
   * <p>
   * Accepts "true", "yes", "1" (case-insensitive) as true, others as false. If
   * the value is null or empty, the default value is returned.
   *
   * @param keySuffix    the suffix for the key
   * @param props        the properties
   * @param defaultValue the default value if not specified
   * @return the parsed boolean, or the default
   */
  private static boolean getEnvBoolean(String keySuffix, Properties props, boolean defaultValue) {
    String value = getEnvOrDot(keySuffix, props);
    if (value == null) {
      return defaultValue;
    }
    String trimmed = value.trim().toLowerCase();
    return "true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed);
  }

  /**
   * Builder class for constructing {@link XaiClientConfig} instances.
   * <p>
   * This builder allows fluent configuration of the client settings. It is not
   * thread-safe and should not be shared between threads.
   */
  public static class Builder {

    /**
     * The API key for the configuration.
     */
    private String apiKey;

    /**
     * The base URL for the API.
     */
    private String baseUrl;

    /**
     * The connection timeout duration.
     */
    private Duration connectTimeout;

    /**
     * The request timeout duration.
     */
    private Duration requestTimeout;

    /**
     * The maximum number of retries.
     */
    private int maxRetries = 3;

    /**
     * Whether to follow redirects.
     */
    protected boolean followRedirects = true;

    /**
     * Sets the API key.
     *
     * @param apiKey the API key string
     * @return this builder for chaining
     */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * Sets the base URL.
     *
     * @param baseUrl the base URL string
     * @return this builder for chaining
     */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param timeout the timeout duration
     * @return this builder for chaining
     */
    public Builder connectTimeout(Duration timeout) {
      this.connectTimeout = timeout;
      return this;
    }

    /**
     * Sets the request timeout.
     *
     * @param timeout the timeout duration
     * @return this builder for chaining
     */
    public Builder requestTimeout(Duration timeout) {
      this.requestTimeout = timeout;
      return this;
    }

    /**
     * Sets the maximum number of retries.
     *
     * @param retries the number of retries
     * @return this builder for chaining
     */
    public Builder maxRetries(int retries) {
      this.maxRetries = retries;
      return this;
    }

    /**
     * Sets whether to follow redirects.
     *
     * @param follow true to follow redirects, false otherwise
     * @return this builder for chaining
     */
    public Builder followRedirects(boolean follow) {
      this.followRedirects = follow;
      return this;
    }

    /**
     * Builds the {@link XaiClientConfig} instance.
     * <p>
     * Validates that the API key is provided and non-blank.
     *
     * @return a new {@link XaiClientConfig} with the configured parameters
     * @throws IllegalArgumentException if apiKey is null or blank
     */
    public XaiClientConfig build() {
      if (apiKey == null || apiKey.isBlank()) {
        throw new IllegalArgumentException("apiKey is required");
      }
      return new XaiClientConfig(this);
    }
  }
}
