package com.xai.client.impl;

import com.xai.api.auth.ApiKey;
import com.xai.client.XaiApiKeyClient;
import java.io.IOException;
import org.junit.*;

/**
 *
 * @author Key Bridge
 */
public class ApiKeyServiceImplTest extends AbstractServiceImpTest {

  @BeforeClass
  public static void setUpClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testGetApiKey() throws IOException {
    ApiKey apiKey = new XaiApiKeyClient().getApiKey();
    printJson(apiKey);
  }

}
