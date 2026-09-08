package com.xai.api.responses.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ResponseEventTest {

  @Test
  public void fromNameKnown() {
    assertSame(ResponseEvent.RESPONSE_OUTPUT_TEXT_DELTA,
               ResponseEvent.fromName("response.output_text.delta"));
  }

  @Test
  public void fromNameAlias() {
    assertSame(ResponseEvent.RESPONSE_TEXT_DELTA,
               ResponseEvent.fromName("response.text.delta"));
    assertSame(ResponseEvent.RESPONSE_TEXT_DONE,
               ResponseEvent.fromName("response.text.done"));
  }

  @Test
  public void fromNameCaseInsensitiveAndTrim() {
    assertSame(ResponseEvent.RESPONSE_COMPLETED,
               ResponseEvent.fromName("  Response.Completed  "));
  }

  @Test
  public void fromNameUnknownNeverNull() {
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName("response.custom.future"));
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName(null));
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName(""));
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName("   "));
  }

  @Test
  public void codeInterpreterWireNames() {
    assertSame(ResponseEvent.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS,
               ResponseEvent.fromName("response.code_interpreter_call.in_progress"));
    assertSame(ResponseEvent.RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA,
               ResponseEvent.fromName("response.code_interpreter_call_code.delta"));
    assertNotEquals("response.code_interpreter.in_progress",
                    ResponseEvent.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS.getName());
  }

  @Test
  public void xaiDocumentedEventsPresent() {
    assertSame(ResponseEvent.RESPONSE_REASONING_TEXT_DELTA,
               ResponseEvent.fromName("response.reasoning_text.delta"));
    assertSame(ResponseEvent.RESPONSE_WEB_SEARCH_CALL_SEARCHING,
               ResponseEvent.fromName("response.web_search_call.searching"));
    assertSame(ResponseEvent.RESPONSE_IMAGE_GENERATION_CALL_GENERATING,
               ResponseEvent.fromName("response.image_generation_call.generating"));
    assertSame(ResponseEvent.RESPONSE_QUEUED,
               ResponseEvent.fromName("response.queued"));
    assertSame(ResponseEvent.RESPONSE_INCOMPLETE,
               ResponseEvent.fromName("response.incomplete"));
    assertSame(ResponseEvent.RESPONSE_OUTPUT_TEXT_ANNOTATION_DOT_ADDED,
               ResponseEvent.fromName("response.output_text.annotation.added"));
  }

  @Test
  public void unknownNameIsUnknown() {
    assertEquals("unknown", ResponseEvent.UNKNOWN.getName());
  }
}
