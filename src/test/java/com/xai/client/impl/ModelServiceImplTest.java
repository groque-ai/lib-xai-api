package com.xai.client.impl;

import com.xai.api.models.ListImageGenerationModelsResponse;
import com.xai.api.models.ListLanguageModelsResponse;
import com.xai.api.models.ListModelsResponse;
import com.xai.client.XaiModelClient;
import java.io.IOException;
import org.junit.*;

/**
 *
 * @author Key Bridge
 */
public class ModelServiceImplTest extends AbstractServiceImpTest {

  private static XaiModelClient service;

  public ModelServiceImplTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    service = new XaiModelClient();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testGetModels() throws IOException {
    ListModelsResponse models = service.getModels();
    printJson(models);
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testGetLanguageModels() throws IOException {
    ListLanguageModelsResponse models = service.getLanguageModels();
    printJson(models);
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testGetImageGenerationModels() throws IOException {
    ListImageGenerationModelsResponse response = service.getImageGenerationModels();
    printJson(response);
  }

}
