package com.llamacpp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import org.junit.Test;

public class LlamaClientConfigTest {

  private static Function<String, String> env(Map<String, String> map) {
    return key -> map.get(key);
  }

  @Test
  public void builderWithoutKeyIsValid() {
    LlamaClientConfig config = new LlamaClientConfig.Builder().build();
    assertFalse(config.hasApiKey());
    assertNull(config.getApiKey());
    assertEquals("http://127.0.0.1:8080", config.getBaseUrl());
    assertEquals(Duration.ofSeconds(10), config.getConnectTimeout());
    assertEquals(Duration.ofSeconds(60), config.getRequestTimeout());
    assertEquals(3, config.getMaxRetries());
    assertTrue(config.isFollowRedirects());
  }

  @Test
  public void builderBlankKeyDoesNotEnableAuth() {
    LlamaClientConfig config = new LlamaClientConfig.Builder().withApiKey("  ").build();
    assertFalse(config.hasApiKey());
  }

  @Test
  public void builderKeyEnablesAuth() {
    LlamaClientConfig config = new LlamaClientConfig.Builder().withApiKey("user-1").build();
    assertTrue(config.hasApiKey());
    assertEquals("user-1", config.getApiKey());
  }

  @Test
  public void missingFileAndEnvUsesDefaults() {
    LlamaClientConfig config = LlamaClientConfig.fromSources(new Properties(), env(new HashMap<>()));
    assertFalse(config.hasApiKey());
    assertEquals("http://127.0.0.1:8080", config.getBaseUrl());
  }

  @Test
  public void fileKeyEnablesAuth() {
    Properties file = new Properties();
    file.setProperty("API_KEY", "from-file");
    file.setProperty("BASE_URL", "http://192.168.1.10:8080");
    LlamaClientConfig config = LlamaClientConfig.fromSources(file, env(new HashMap<>()));
    assertTrue(config.hasApiKey());
    assertEquals("from-file", config.getApiKey());
    assertEquals("http://192.168.1.10:8080", config.getBaseUrl());
  }

  @Test
  public void envWinsOverFile() {
    Properties file = new Properties();
    file.setProperty("API_KEY", "from-file");
    file.setProperty("BASE_URL", "http://file:8080");
    Map<String, String> map = new HashMap<>();
    map.put("LLAMACPP_API_KEY", "from-env");
    map.put("LLAMACPP_BASE_URL", "http://env:9090");
    LlamaClientConfig config = LlamaClientConfig.fromSources(file, env(map));
    assertEquals("from-env", config.getApiKey());
    assertEquals("http://env:9090", config.getBaseUrl());
  }

  @Test
  public void unprefixedApiKeyEnvIsIgnored() {
    Map<String, String> map = new HashMap<>();
    map.put("API_KEY", "xai-should-not-apply");
    LlamaClientConfig config = LlamaClientConfig.fromSources(new Properties(), env(map));
    assertFalse(config.hasApiKey());
  }
}
