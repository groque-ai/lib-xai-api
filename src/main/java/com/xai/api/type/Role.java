package com.xai.api.type;
// com/xai/api/model/Role.java

public enum Role {
  system,
  user,
  assistant,
  tool,
  /**
   * app-level instructions
   * <p>
   * used in ModelInputPart (Message) only
   */
  developer;
}
