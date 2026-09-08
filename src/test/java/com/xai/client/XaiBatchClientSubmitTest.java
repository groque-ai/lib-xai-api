package com.xai.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.batch.Batch;
import com.xai.api.batch.BatchRequest;
import com.xai.api.batch.BatchState;
import com.xai.api.images.EditImageRequest;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.responses.ModelRequest;
import com.xai.api.video.EditVideoRequest;
import com.xai.api.video.GenerateVideoRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for submit wrapping, names, and completion.
 *
 * @author Key Bridge
 */
public class XaiBatchClientSubmitTest {

  private static final ObjectMapper MAPPER = new ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  @Test
  public void wrapModelRequestUsesResponsesKey() throws Exception {
    ModelRequest request = new ModelRequest();
    request.setModel("grok-4.3");
    BatchRequest wrapped = XaiBatchClient.toBatchRequest(request);
    String json = MAPPER.writeValueAsString(wrapped);
    Assert.assertTrue(json.contains("\"responses\""));
    Assert.assertTrue(json.contains("grok-4.3"));
    Assert.assertFalse(json.contains("image_generation"));
  }

  @Test
  public void wrapEditVideoUsesVideoGenerationKey() throws Exception {
    EditVideoRequest request = new EditVideoRequest();
    request.setPrompt("Make it slow motion");
    BatchRequest wrapped = XaiBatchClient.toBatchRequest(request);
    String json = MAPPER.writeValueAsString(wrapped);
    Assert.assertTrue(json.contains("\"video_generation\""));
    Assert.assertFalse(json.contains("video_extension"));
  }

  @Test
  public void wrapImageTypesUseGenerationAndEditKeys() throws Exception {
    GenerateImageRequest generate = new GenerateImageRequest();
    generate.setPrompt("laptop");
    String generateJson = MAPPER.writeValueAsString(XaiBatchClient.toBatchRequest(generate));
    Assert.assertTrue(generateJson.contains("\"image_generation\""));

    EditImageRequest edit = new EditImageRequest();
    edit.setPrompt("rainbow");
    String editJson = MAPPER.writeValueAsString(XaiBatchClient.toBatchRequest(edit));
    Assert.assertTrue(editJson.contains("\"image_edit\""));
  }

  @Test
  public void wrapGenerateVideoUsesVideoGenerationKey() throws Exception {
    GenerateVideoRequest request = new GenerateVideoRequest();
    request.setPrompt("turntable");
    String json = MAPPER.writeValueAsString(XaiBatchClient.toBatchRequest(request));
    Assert.assertTrue(json.contains("\"video_generation\""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrapRejectsUnknownType() {
    XaiBatchClient.toBatchRequest("not a request");
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrapRejectsNull() {
    XaiBatchClient.toBatchRequest(null);
  }

  @Test
  public void submitNameUsesSimpleClassAndLocalTime() {
    Clock clock = Clock.fixed(Instant.parse("2026-09-08T14:04:00Z"), ZoneOffset.UTC);
    String name = XaiBatchClient.submitName(new ModelRequest(), clock);
    Assert.assertEquals("ModelRequest submitted at 2:04 PM", name);
  }

  @Test
  public void completeWhenRequestsExistAndNonePending() {
    Assert.assertFalse(XaiBatchClient.isComplete(null));
    Assert.assertFalse(XaiBatchClient.isComplete(new Batch()));

    Batch empty = new Batch();
    BatchState emptyState = new BatchState();
    empty.setState(emptyState);
    Assert.assertFalse(XaiBatchClient.isComplete(empty));

    Batch pending = new Batch();
    BatchState pendingState = new BatchState();
    pendingState.setNumRequests(2);
    pendingState.setNumPending(1);
    pending.setState(pendingState);
    Assert.assertFalse(XaiBatchClient.isComplete(pending));

    Batch done = new Batch();
    BatchState doneState = new BatchState();
    doneState.setNumRequests(2);
    doneState.setNumPending(0);
    done.setState(doneState);
    Assert.assertTrue(XaiBatchClient.isComplete(done));
  }

  @Test
  public void pageQueryUsesSnakeCaseKeys() {
    Assert.assertEquals("", XaiBatchClient.pageQuery(null, null));
    Assert.assertEquals("?limit=100", XaiBatchClient.pageQuery(100, null));
    Assert.assertEquals("?limit=100&pagination_token=tok",
      XaiBatchClient.pageQuery(100, "tok"));
  }

}
