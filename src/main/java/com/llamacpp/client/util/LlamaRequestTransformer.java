package com.llamacpp.client.util;

import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.tool.ModelTool;
import com.xai.api.type.ModelToolType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mutates a {@link ModelRequest} so llama.cpp {@code POST /v1/responses} will
 * accept it. llama.cpp converts Responses to chat completions and rejects
 * stored-conversation fields.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public class LlamaRequestTransformer {

  private static final Logger LOG = Logger.getLogger(LlamaRequestTransformer.class.getName());

  private LlamaRequestTransformer() {
  }

  /**
   * Strip llama-unsupported fields in place. No-op if request is null.
   */
  public static void apply(ModelRequest request) {
    if (request == null) {
      return;
    }
    String previous = request.getPreviousResponseId();
    if (previous != null && !previous.isBlank()) {
      skip("previous_response_id");
      request.setPreviousResponseId(null);
    }
    if (Boolean.TRUE.equals(request.getStore())) {
      skip("store");
      request.setStore(null);
    }
    if (request.getSearchParameters() != null) {
      skip("search_parameters");
      request.setSearchParameters(null);
    }
    filterTools(request);
  }

  private static void filterTools(ModelRequest request) {
    Collection<ModelTool> tools = request.getTools();
    if (tools == null || tools.isEmpty()) {
      return;
    }
    List<ModelTool> kept = new ArrayList<>();
    boolean dropped = false;
    for (ModelTool tool : tools) {
      if (tool != null && tool.getType() == ModelToolType.function) {
        kept.add(tool);
      } else {
        dropped = true;
        String type = tool == null ? "null" : String.valueOf(tool.getType());
        skip("tools." + type);
      }
    }
    if (dropped) {
      request.setTools(kept.isEmpty() ? null : kept);
    }
  }

  private static void skip(String field) {
    LOG.log(Level.WARNING, "SANITIZE skip '{'field={0}'}'", field);
  }
}
