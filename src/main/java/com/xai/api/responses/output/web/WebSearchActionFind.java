package com.xai.api.responses.output.web;

public class WebSearchActionFind extends WebSearchAction {

  // The pattern or text to search for within the page
  private String pattern;

  // The source of the page to search in
  private WebSearchSource source;

  // Always "find"
  private String type;
}
