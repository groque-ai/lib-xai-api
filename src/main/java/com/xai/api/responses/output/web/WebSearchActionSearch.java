package com.xai.api.responses.output.web;

import java.util.List;

public class WebSearchActionSearch extends WebSearchAction {

  // The search query
  private String query;

  // The sources used in the search
  private List<WebSearchSource> sources;

  // Always "search"
  private String type;
}
