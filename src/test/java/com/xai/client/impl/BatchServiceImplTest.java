package com.xai.client.impl;

import com.xai.api.batch.Batch;
import com.xai.api.responses.ModelRequest;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.client.XaiBatchClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Live batch smoke. Requires API key.
 *
 * @author Key Bridge
 */
public class BatchServiceImplTest extends AbstractServiceImpTest {

  private static XaiBatchClient service;

  @BeforeClass
  public static void setUpClass() {
    service = new XaiBatchClient();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    if (service != null) {
      service.close();
    }
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testSubmitGetCancel() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel("grok-4.3")
      .addUserMessage("Reply with the single word pong.")
      .build();

    String batchId = service.submit(request);
    Assert.assertNotNull(batchId);

    Batch got = service.get(batchId);
    printJson(got);
    Assert.assertEquals(batchId, got.getBatchId());
    Assert.assertEquals(1, got.getState().getNumRequests());

    Batch cancelled = service.cancel(batchId);
    printJson(cancelled);
  }

}
