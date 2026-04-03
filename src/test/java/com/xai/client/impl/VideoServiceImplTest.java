package com.xai.client.impl;

import com.xai.api.video.EditVideoRequest;
import com.xai.api.video.GenerateVideoRequest;
import com.xai.api.video.StartDeferredResponse;
import com.xai.api.video.dto.VideoResponse;
import com.xai.api.video.dto.VideoUrl;
import com.xai.client.XaiVideoClient;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author Key Bridge
 */
public class VideoServiceImplTest extends AbstractServiceImpTest {

  private static XaiVideoClient service;

  public VideoServiceImplTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    service = new XaiVideoClient();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testCreateVideo() throws IOException, InterruptedException {

    long start = System.currentTimeMillis();

//    String url = "https://imgen.x.ai/xai-imgen/xai-tmp-imgen-cc2b4aef-9428-4ab2-b6d4-ed26d29103d2.jpeg";
    GenerateVideoRequest request = new GenerateVideoRequest();
    request.setModel("grok-imagine-video");
    request.setPrompt("A cat playing with a ball");
//    request.setImage(new ImageUrl(url));

    System.out.println(">>>");
    printJson(request);
    StartDeferredResponse response = service.createVideo(request);
    System.out.println("<<<");
    printJson(response);

    System.out.println("Retrieving video");
    Thread.sleep(1000);
    VideoResponse videoResponse = service.getVideo(response.getRequestId());
    while (videoResponse.isPending()) {
      Thread.sleep(2000);
      videoResponse = service.getVideo(response.getRequestId());
      System.out.println("<<<");
      printJson(videoResponse);
    }

    long duration = System.currentTimeMillis() - start;
    System.out.println("Video completed in " + duration + " ms");

  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testEditVideo() throws IOException, InterruptedException {

    long start = System.currentTimeMillis();
    String videoUrl = "https://vidgen.x.ai/xai-vidgen-bucket/xai-video-794f878d-1a98-4656-a810-e249289bc37f.mp4";

    EditVideoRequest request = new EditVideoRequest();
    request.setModel("grok-imagine-video");
    request.setVideo(new VideoUrl(videoUrl));
    request.setPrompt("Set the scene on the moon. Add friendly alien cats.");
    System.out.println(">>>");
    printJson(request);
    StartDeferredResponse response = service.editVideo(request);
    System.out.println("<<<");
    printJson(response);

    System.out.println("Retrieving video");
    Thread.sleep(1000);
    VideoResponse videoResponse = service.getVideo(response.getRequestId());
    while (videoResponse.isPending()) {
      Thread.sleep(2000);
      videoResponse = service.getVideo(response.getRequestId());
      System.out.println("<<<");
      printJson(videoResponse);
    }

    long duration = System.currentTimeMillis() - start;
    System.out.println("Video completed in " + duration + " ms");
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testGetVideo() throws IOException {
//    String requestId = "e646381e-b7e2-8bb1-92b9-40f11dd3b9cf";
    String requestId = "b95f1633-3fad-a4e6-d7fa-fe5c35a9670f";

    VideoResponse response = service.getVideo(requestId);
    System.out.println("<<<");
    printJson(response);

  }

}
