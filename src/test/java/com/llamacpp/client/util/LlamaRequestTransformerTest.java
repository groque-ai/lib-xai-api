package com.llamacpp.client.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.config.SearchParameters;
import com.xai.api.responses.tool.FunctionTool;
import com.xai.api.responses.tool.ModelTool;
import com.xai.api.responses.tool.WebSearchTool;
import com.xai.api.type.ModelToolType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class LlamaRequestTransformerTest {

  @Test
  public void discardsPreviousResponseId() {
    ModelRequest request = new ModelRequest();
    request.setPreviousResponseId("resp_abc");
    LlamaRequestTransformer.apply(request);
    assertNull(request.getPreviousResponseId());
  }

  @Test
  public void leavesNullPreviousResponseId() {
    ModelRequest request = new ModelRequest();
    LlamaRequestTransformer.apply(request);
    assertNull(request.getPreviousResponseId());
  }

  @Test
  public void discardsStoreTrue() {
    ModelRequest request = new ModelRequest();
    request.setStore(Boolean.TRUE);
    LlamaRequestTransformer.apply(request);
    assertNull(request.getStore());
  }

  @Test
  public void leavesStoreFalse() {
    ModelRequest request = new ModelRequest();
    request.setStore(Boolean.FALSE);
    LlamaRequestTransformer.apply(request);
    assertEquals(Boolean.FALSE, request.getStore());
  }

  @Test
  public void discardsSearchParameters() {
    ModelRequest request = new ModelRequest();
    request.setSearchParameters(new SearchParameters());
    LlamaRequestTransformer.apply(request);
    assertNull(request.getSearchParameters());
  }

  @Test
  public void dropsXaiOnlyToolsKeepsFunction() {
    FunctionTool fn = new FunctionTool();
    fn.setName("lookup");
    List<ModelTool> tools = new ArrayList<>();
    tools.add(fn);
    tools.add(new WebSearchTool());
    ModelRequest request = new ModelRequest();
    request.setTools(tools);
    LlamaRequestTransformer.apply(request);
    assertEquals(1, request.getTools().size());
    ModelTool kept = request.getTools().iterator().next();
    assertSame(fn, kept);
    assertEquals(ModelToolType.function, kept.getType());
  }

  @Test
  public void clearsToolsWhenOnlyXaiToolsPresent() {
    List<ModelTool> tools = new ArrayList<>();
    tools.add(new WebSearchTool());
    ModelRequest request = new ModelRequest();
    request.setTools(tools);
    LlamaRequestTransformer.apply(request);
    assertTrue(request.getTools() == null || request.getTools().isEmpty());
  }
}
