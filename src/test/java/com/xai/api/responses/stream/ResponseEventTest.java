package com.xai.api.responses.stream;

import com.xai.api.type.StreamEventType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ResponseEventTest {

  @Test
  public void fromNameKnown() {
    assertSame(StreamEventType.RESPONSE_OUTPUT_TEXT_DELTA,
               StreamEventType.fromName("response.output_text.delta"));
  }

  @Test
  public void fromNameAlias() {
    assertSame(StreamEventType.RESPONSE_TEXT_DELTA,
               StreamEventType.fromName("response.text.delta"));
    assertSame(StreamEventType.RESPONSE_TEXT_DONE,
               StreamEventType.fromName("response.text.done"));
  }

  @Test
  public void fromNameCaseInsensitiveAndTrim() {
    assertSame(StreamEventType.RESPONSE_COMPLETED,
               StreamEventType.fromName("  Response.Completed  "));
  }

  @Test
  public void fromNameUnknownNeverNull() {
    assertSame(StreamEventType.UNKNOWN, StreamEventType.fromName("response.custom.future"));
    assertSame(StreamEventType.UNKNOWN, StreamEventType.fromName(null));
    assertSame(StreamEventType.UNKNOWN, StreamEventType.fromName(""));
    assertSame(StreamEventType.UNKNOWN, StreamEventType.fromName("   "));
  }

  @Test
  public void codeInterpreterWireNames() {
    assertSame(StreamEventType.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS,
               StreamEventType.fromName("response.code_interpreter_call.in_progress"));
    assertSame(StreamEventType.RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA,
               StreamEventType.fromName("response.code_interpreter_call_code.delta"));
    assertNotEquals("response.code_interpreter.in_progress",
                    StreamEventType.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS.getName());
  }

  @Test
  public void xaiDocumentedEventsPresent() {
    assertSame(StreamEventType.RESPONSE_REASONING_TEXT_DELTA,
               StreamEventType.fromName("response.reasoning_text.delta"));
    assertSame(StreamEventType.RESPONSE_WEB_SEARCH_CALL_SEARCHING,
               StreamEventType.fromName("response.web_search_call.searching"));
    assertSame(StreamEventType.RESPONSE_IMAGE_GENERATION_CALL_GENERATING,
               StreamEventType.fromName("response.image_generation_call.generating"));
    assertSame(StreamEventType.RESPONSE_QUEUED,
               StreamEventType.fromName("response.queued"));
    assertSame(StreamEventType.RESPONSE_INCOMPLETE,
               StreamEventType.fromName("response.incomplete"));
    assertSame(StreamEventType.RESPONSE_OUTPUT_TEXT_ANNOTATION_DOT_ADDED,
               StreamEventType.fromName("response.output_text.annotation.added"));
  }

  @Test
  public void unknownNameIsUnknown() {
    assertEquals("unknown", StreamEventType.UNKNOWN.getName());
  }
}
