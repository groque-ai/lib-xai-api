package com.xai.api.responses.output.filesearch;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.type.OutputType;
import java.util.List;

@JsonTypeName("file_search_call")
public class FileSearchCall extends ModelOutput {

  /**
   * The queries used to search for files.
   */
  private List<String> queries;

  /**
   * The results of the file search tool call.
   */
  private List<FileSearchResult> results;

  public FileSearchCall() {
    super(OutputType.file_search_call);
  }

  /**
   * Returns The queries used to search for files.
   *
   * @return the queries
   */
  public List<String> getQueries() {
    return queries;
  }

  /**
   * Sets The queries used to search for files.
   *
   * @param queries the queries
   */
  public void setQueries(List<String> queries) {
    this.queries = queries;
  }

  /**
   * Returns The results of the file search tool call.
   *
   * @return the results
   */
  public List<FileSearchResult> getResults() {
    return results;
  }

  /**
   * Sets The results of the file search tool call.
   *
   * @param results the results
   */
  public void setResults(List<FileSearchResult> results) {
    this.results = results;
  }
}
