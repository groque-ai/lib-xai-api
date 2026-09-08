package com.xai.api.batch.request;

import ai.x.grok.api.completions.Message;
import ai.x.grok.api.completions.ChatRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Round-trip of a first-pass text ChatRequest (system + user + assistant).
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class ChatRequestTest {

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setUpClass() {
    MAPPER = new ObjectMapper();
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Test
  public void textMessagesRoundTrip() throws Exception {
    ChatRequest request = new ChatRequest();
    request.setModel("grok-4-0709");
    request.setTemperature(0.2f);

    List<Message> messages = new ArrayList<>();
    messages.add(message("system", "Classify sentiment."));
    messages.add(message("user", "The product exceeded my expectations!"));
    Message assistant = message("assistant", "positive");
    assistant.setReasoningContent("The user describes a positive outcome.");
    messages.add(assistant);
    request.setMessages(messages);

    String json = MAPPER.writeValueAsString(request);
    JsonNode node = MAPPER.readTree(json);

    Assert.assertEquals("grok-4-0709", node.get("model").asText());
    Assert.assertEquals(3, node.get("messages").size());
    Assert.assertEquals("system", node.get("messages").get(0).get("role").asText());
    Assert.assertEquals("Classify sentiment.", node.get("messages").get(0).get("content").asText());
    Assert.assertFalse("tools must not be serialized in this pass", node.has("tools"));
    Assert.assertFalse("tool_choice must not be serialized in this pass", node.has("tool_choice"));

    ChatRequest recovered = MAPPER.readValue(json, ChatRequest.class);
    Assert.assertEquals("grok-4-0709", recovered.getModel());
    Assert.assertEquals(3, recovered.getMessages().size());
    Assert.assertEquals("assistant", recovered.getMessages().get(2).getRole());
    Assert.assertEquals("The user describes a positive outcome.",
        recovered.getMessages().get(2).getReasoningContent());
  }

  private static Message message(String role, String content) {
    Message m = new Message();
    m.setRole(role);
    m.setContent(content);
    return m;
  }
}
