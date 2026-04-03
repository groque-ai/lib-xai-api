package com.xai.client.impl;

import com.xai.api.images.EditImageRequest;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.images.GeneratedImageResponse;
import com.xai.client.XaiImageClient;
import java.io.IOException;
import org.junit.*;

/**
 *
 * @author Key Bridge
 */
public class ImageServiceImplTest extends AbstractServiceImpTest {

  private static XaiImageClient service;

  @BeforeClass
  public static void setUpClass() {
    service = new XaiImageClient();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testCreateImages() throws IOException {

    GenerateImageRequest request = new GenerateImageRequest();
    request.setModel("grok-imagine-image");
    request.setPrompt("A cat in a tree");
//    request.setResponseFormat("url");
    System.out.println(">>>");
    printJson(request);
    GeneratedImageResponse response = service.createImages(request);
    System.out.println("<<<");
    printJson(response);

  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testEditImage() throws IOException {

    String url = "https://imgen.x.ai/xai-imgen/xai-tmp-imgen-88a0d0f0-349c-4acf-8df8-96459e590630.jpeg";

    EditImageRequest request = new EditImageRequest();
    request.setModel("grok-imagine-image");
    request.setImageUrl(url);
    request.setPrompt("Add a silly hat to this cat.");
    System.out.println(">>>");
    printJson(request);

    GeneratedImageResponse response = service.editImage(request);

    System.out.println("<<<");
    printJson(response);

  }

}
