package com.xai.client.impl;

import com.xai.api.tokenize.TokenizeRequest;
import com.xai.api.tokenize.TokenizeResponse;
import com.xai.client.XaiTokenizeClient;
import java.io.IOException;
import org.junit.*;

/**
 *
 * @author Key Bridge
 */
public class TokenizeServiceImplTest extends AbstractServiceImpTest {

  private static XaiTokenizeClient service;

  public TokenizeServiceImplTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    service = new XaiTokenizeClient();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
  @Test
  public void testCreateTokenizedText() throws IOException {

    TokenizeRequest request = new TokenizeRequest();
    request.setModel("grok-4-0709");
    request.setText("Hello World");

    System.out.println(">>>");
    printJson(request);

    TokenizeResponse response = service.createTokenizedText(request);

    System.out.println("<<<");
    printJson(response);
  }

}
