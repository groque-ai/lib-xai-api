package com.xai.api.responses.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ResponseEventTest {

  @Test
  public void fromNameKnown() {
    assertSame(ResponseEventType.RESPONSE_OUTPUT_TEXT_DELTA,
               ResponseEventType.fromName("response.output_text.delta"));
  }

  @Test
  public void fromNameAlias() {
    assertSame(ResponseEventType.RESPONSE_TEXT_DELTA,
               ResponseEventType.fromName("response.text.delta"));
    assertSame(ResponseEventType.RESPONSE_TEXT_DONE,
               ResponseEventType.fromName("response.text.done"));
  }

  @Test
  public void fromNameCaseInsensitiveAndTrim() {
    assertSame(ResponseEventType.RESPONSE_COMPLETED,
               ResponseEventType.fromName("  Response.Completed  "));
  }

  @Test
  public void fromNameUnknownNeverNull() {
    assertSame(ResponseEventType.UNKNOWN, ResponseEventType.fromName("response.custom.future"));
    assertSame(ResponseEventType.UNKNOWN, ResponseEventType.fromName(null));
    assertSame(ResponseEventType.UNKNOWN, ResponseEventType.fromName(""));
    assertSame(ResponseEventType.UNKNOWN, ResponseEventType.fromName("   "));
  }

  @Test
  public void codeInterpreterWireNames() {
    assertSame(ResponseEventType.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS,
               ResponseEventType.fromName("response.code_interpreter_call.in_progress"));
    assertSame(ResponseEventType.RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA,
               ResponseEventType.fromName("response.code_interpreter_call_code.delta"));
    assertNotEquals("response.code_interpreter.in_progress",
                    ResponseEventType.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS.getName());
  }

  @Test
  public void xaiDocumentedEventsPresent() {
    assertSame(ResponseEventType.RESPONSE_REASONING_TEXT_DELTA,
               ResponseEventType.fromName("response.reasoning_text.delta"));
    assertSame(ResponseEventType.RESPONSE_WEB_SEARCH_CALL_SEARCHING,
               ResponseEventType.fromName("response.web_search_call.searching"));
    assertSame(ResponseEventType.RESPONSE_IMAGE_GENERATION_CALL_GENERATING,
               ResponseEventType.fromName("response.image_generation_call.generating"));
    assertSame(ResponseEventType.RESPONSE_QUEUED,
               ResponseEventType.fromName("response.queued"));
    assertSame(ResponseEventType.RESPONSE_INCOMPLETE,
               ResponseEventType.fromName("response.incomplete"));
    assertSame(ResponseEventType.RESPONSE_OUTPUT_TEXT_ANNOTATION_DOT_ADDED,
               ResponseEventType.fromName("response.output_text.annotation.added"));
  }

  @Test
  public void unknownNameIsUnknown() {
    assertEquals("unknown", ResponseEventType.UNKNOWN.getName());
  }
}
