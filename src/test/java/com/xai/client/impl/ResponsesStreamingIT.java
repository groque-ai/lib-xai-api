package com.xai.client.impl;

import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.input.ModelInputString;
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamPumpTest;
import com.xai.client.XaiResponsesClient;
import org.junit.Ignore;
import org.junit.Test;

public class ResponsesStreamingIT {

  @Ignore("Live xAI — run manually with API_KEY set")
  @Test
  public void streamHello() throws Exception {
    XaiResponsesClient client = new XaiResponsesClient();
    ModelRequest request = new ModelRequest();
    request.setModel("grok-4.6");
    ModelInputString input = new ModelInputString();
    input.setValue("Say hi in one word.");
    request.setInput(input);
    ResponseStreamPumpTest.RecordingListener listener = new ResponseStreamPumpTest.RecordingListener();
    try (ResponseStreamHandle handle = client.generateStreaming(request, listener)) {
      listener.completed.await();
    }
  }
}
