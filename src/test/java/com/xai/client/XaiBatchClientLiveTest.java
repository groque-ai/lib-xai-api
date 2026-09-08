package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.batch.Batch;
import com.xai.api.batch.BatchMetadata;
import com.xai.api.batch.BatchResult;
import com.xai.api.responses.ModelRequest;
import com.xai.api.util.ModelRequestBuilder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Live {@code /v1/batches} tests against xAI. Uses
 * {@link XaiClientConfig#readConfig()}.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class XaiBatchClientLiveTest {

  private static final String MODEL = "grok-4.3";
  private static final long WAIT_SECONDS = 180;

  private XaiBatchClient client;

  @Before
  public void setUp() {
    XaiClientConfig env = XaiClientConfig.readConfig();
    XaiClientConfig config = new XaiClientConfig.Builder()
      .withApiKey(env.getApiKey())
      .withBaseUrl(env.getBaseUrl())
      .withConnectTimeout(env.getConnectTimeout())
      .withRequestTimeout(Duration.ofSeconds(60))
      .withFollowRedirects(env.isFollowRedirects())
      .build();
    client = new XaiBatchClient(config);
    client.setPollInterval(Duration.ofSeconds(3));
  }

  @After
  public void tearDown() throws Exception {
    if (client != null) {
      client.close();
    }
  }

  @Test
  public void createGetListCancel() throws Exception {
    Batch created = client.create("lib-xai-api live create");
    assertNotNull(created);
    assertNotNull(created.getBatchId());
    System.out.println("created " + created.getBatchId());

    Batch got = client.get(created.getBatchId());
    assertEquals(created.getBatchId(), got.getBatchId());
    assertNotNull(got.getState());
    assertEquals(0, got.getState().getNumRequests());

    List<Batch> listed = client.list();
    assertNotNull(listed);
    boolean found = false;
    for (Batch batch : listed) {
      if (created.getBatchId().equals(batch.getBatchId())) {
        found = true;
        break;
      }
    }
    assertTrue("created batch should appear in list", found);

    Batch cancelled = client.cancel(created.getBatchId());
    assertNotNull(cancelled);
    assertEquals(created.getBatchId(), cancelled.getBatchId());
  }

  @Test
  public void submitGetListRequests() throws Exception {
    String batchId = client.submit(tinyRequest());
    assertNotNull(batchId);
    System.out.println("submitted " + batchId);

    Batch got = client.get(batchId);
    assertEquals(batchId, got.getBatchId());
    assertNotNull(got.getName());
    assertTrue(got.getName().startsWith("ModelRequest submitted at "));
    assertEquals(1, got.getState().getNumRequests());

    List<BatchMetadata> meta = client.listRequests(batchId);
    assertEquals(1, meta.size());
    assertNotNull(meta.get(0).getBatchRequestId());

    client.cancel(batchId);
  }

  @Test
  public void submitAppendsToExistingBatch() throws Exception {
    String batchId = client.submit(tinyRequest());
    client.submit(batchId, tinyRequest());

    Batch got = client.get(batchId);
    assertEquals(2, got.getState().getNumRequests());

    List<BatchMetadata> meta = client.listRequests(batchId);
    assertEquals(2, meta.size());

    client.cancel(batchId);
  }

  @Test
  public void pollUntilCompleteThenListResults() throws Exception {
    String batchId = client.submit(tinyRequest());
    Batch done = waitUntilComplete(batchId);
    assertNotNull("batch did not finish within " + WAIT_SECONDS + "s: " + batchId, done);
    assertEquals(0, done.getState().getNumPending());
    assertTrue(done.getState().getNumSuccess() + done.getState().getNumError() >= 1);

    List<BatchResult> results = client.listResults(batchId);
    assertFalse("expected at least one result row", results.isEmpty());
    assertNotNull(results.get(0).getBatchRequestId());
    System.out.println("results size=" + results.size()
      + " first=" + results.get(0).getBatchResult());
  }

  @Test
  public void listenerReceivesCompletion() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    AtomicReference<String> completedId = new AtomicReference<>();
    AtomicReference<JsonNode> completedResults = new AtomicReference<>();
    AtomicReference<Throwable> error = new AtomicReference<>();

    client.setListener(new BatchListener() {
      @Override
      public void onProgress(Batch batch) {
        System.out.println("progress " + batch.getBatchId()
          + " pending=" + (batch.getState() == null ? "?" : batch.getState().getNumPending()));
      }

      @Override
      public void onComplete(String batchId, JsonNode results) {
        completedId.set(batchId);
        completedResults.set(results);
        done.countDown();
      }

      @Override
      public void onError(String batchId, Throwable err) {
        error.set(err);
        done.countDown();
      }
    });

    String batchId = client.submit(tinyRequest());
    boolean signaled = done.await(WAIT_SECONDS, TimeUnit.SECONDS);
    if (error.get() != null) {
      throw new AssertionError("listener error for " + batchId, error.get());
    }
    assertTrue("listener did not complete within " + WAIT_SECONDS + "s", signaled);
    assertEquals(batchId, completedId.get());
    assertNotNull(completedResults.get());
    assertTrue(completedResults.get().isArray());
    assertTrue(completedResults.get().size() >= 1);
  }

  private ModelRequest tinyRequest() {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("Reply with exactly one word: pong")
      .build();
    request.setMaxOutputTokens(16);
    return request;
  }

  private Batch waitUntilComplete(String batchId) throws InterruptedException {
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(WAIT_SECONDS);
    while (System.currentTimeMillis() < deadline) {
      Batch batch = client.get(batchId);
      if (batch != null && batch.getState() != null
        && batch.getState().getNumRequests() > 0
        && batch.getState().getNumPending() == 0) {
        return batch;
      }
      Thread.sleep(3000);
    }
    return null;
  }

}
