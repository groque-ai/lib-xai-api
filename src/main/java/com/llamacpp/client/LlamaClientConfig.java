package com.llamacpp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;

/**
 * Immutable llama.cpp client configuration. API key is optional.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public class LlamaClientConfig {

  private static final String DOT_FILE = ".llamacpp";
  private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080";

  private final String apiKey;
  private final boolean hasApiKey;
  private final String baseUrl;
  private final Duration connectTimeout;
  private final Duration requestTimeout;
  private final int maxRetries;
  private final boolean followRedirects;

  private LlamaClientConfig(Builder builder) {
    String key = builder.apiKey;
    if (key != null) {
      key = key.trim();
    }
    // blank/null key means no Authorization header
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

  private static String trimSlash(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  public static LlamaClientConfig readConfig() {
    return fromSources(readDotFile(), System::getenv);
  }

  public static LlamaClientConfig fromSources(Properties fileProps, Function<String, String> env) {
    Properties props = fileProps != null ? fileProps : new Properties();
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
        // keep default
      }
    }
    return b.build();
  }

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

  private static String first(String envVal, String fileVal) {
    if (envVal != null && !envVal.trim().isEmpty()) {
      return envVal.trim();
    }
    if (fileVal != null && !fileVal.trim().isEmpty()) {
      return fileVal.trim();
    }
    return null;
  }

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

  private static boolean isTrue(String value) {
    String trimmed = value.trim().toLowerCase();
    return "true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed);
  }

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public String getApiKey() {
    return apiKey;
  }

  public boolean hasApiKey() {
    return hasApiKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  public Duration getRequestTimeout() {
    return requestTimeout;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public boolean isFollowRedirects() {
    return followRedirects;
  }
  //</editor-fold>

  public static class Builder {

    private String apiKey;
    private String baseUrl;
    private Duration connectTimeout;
    private Duration requestTimeout;
    private int maxRetries = 3;
    private boolean followRedirects = true;

    public Builder withApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder withBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder withConnectTimeout(Duration timeout) {
      this.connectTimeout = timeout;
      return this;
    }

    public Builder withRequestTimeout(Duration timeout) {
      this.requestTimeout = timeout;
      return this;
    }

    public Builder withMaxRetries(int retries) {
      this.maxRetries = retries;
      return this;
    }

    public Builder withFollowRedirects(boolean follow) {
      this.followRedirects = follow;
      return this;
    }

    public LlamaClientConfig build() {
      return new LlamaClientConfig(this);
    }
  }
}
