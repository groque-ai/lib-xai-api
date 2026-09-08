package com.xai.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.api.batch.BatchRequest;
import com.xai.api.images.EditImageRequest;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.responses.ModelRequest;
import com.xai.api.video.EditVideoRequest;
import com.xai.api.video.GenerateVideoRequest;
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
    BatchRequest wrapped = BatchRequest.getInstance(request);
    String json = MAPPER.writeValueAsString(wrapped);
    Assert.assertTrue(json.contains("\"responses\""));
    Assert.assertTrue(json.contains("grok-4.3"));
    Assert.assertFalse(json.contains("image_generation"));
  }

  @Test
  public void wrapEditVideoUsesVideoGenerationKey() throws Exception {
    EditVideoRequest request = new EditVideoRequest();
    request.setPrompt("Make it slow motion");
    BatchRequest wrapped = BatchRequest.getInstance(request);
    String json = MAPPER.writeValueAsString(wrapped);
    Assert.assertTrue(json.contains("\"video_generation\""));
    Assert.assertFalse(json.contains("video_extension"));
  }

  @Test
  public void wrapImageTypesUseGenerationAndEditKeys() throws Exception {
    GenerateImageRequest generate = new GenerateImageRequest();
    generate.setPrompt("laptop");
    String generateJson = MAPPER.writeValueAsString(BatchRequest.getInstance(generate));
    Assert.assertTrue(generateJson.contains("\"image_generation\""));

    EditImageRequest edit = new EditImageRequest();
    edit.setPrompt("rainbow");
    String editJson = MAPPER.writeValueAsString(BatchRequest.getInstance(edit));
    Assert.assertTrue(editJson.contains("\"image_edit\""));
  }

  @Test
  public void wrapGenerateVideoUsesVideoGenerationKey() throws Exception {
    GenerateVideoRequest request = new GenerateVideoRequest();
    request.setPrompt("turntable");
    String json = MAPPER.writeValueAsString(BatchRequest.getInstance(request));
    Assert.assertTrue(json.contains("\"video_generation\""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrapRejectsUnknownType() {
    BatchRequest.getInstance("not a request");
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrapRejectsNull() {
    BatchRequest.getInstance(null);
  }

}
