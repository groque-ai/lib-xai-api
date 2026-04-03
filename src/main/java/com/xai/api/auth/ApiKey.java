package com.xai.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ApiKey {

  /**
   * The redacted API key.
   */
  @JsonProperty("redacted_api_key")
  private String redactedApiKey;

  /**
   * User ID the API key belongs to.
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * The name of the API key specified by user.
   */
  private String name;

  /**
   * Creation time of the API key in Unix timestamp.
   */
  @JsonProperty("create_time")
  private String createTime;

  /**
   * Last modification time of the API key in Unix timestamp.
   */
  @JsonProperty("modify_time")
  private String modifyTime;

  /**
   * User ID of the user who last modified the API key.
   */
  @JsonProperty("modified_by")
  private String modifiedBy;

  /**
   * The team ID of the team that owns the API key.
   */
  @JsonProperty("team_id")
  private String teamId;

  /**
   * A list of ACLs authorized with the API key, e.g. `"api-key:endpoint:*"`,
   * `"api-key:model:*"`.
   */
  private List<String> acls;

  /**
   * ID of the API key.
   */
  @JsonProperty("api_key_id")
  private String apiKeyId;

  /**
   * Indicates whether the team that owns the API key.
   */
  @JsonProperty("team_blocked")
  private boolean teamBlocked;

  /**
   * Indicates whether the API key is blocked.
   */
  @JsonProperty("api_key_blocked")
  private boolean apiKeyBlocked;

  /**
   * Indicates whether the API key is disabled.
   */
  @JsonProperty("api_key_disabled")
  private boolean apiKeyDisabled;

  /**
   * Returns A list of ACLs authorized with the API key, e.g.
   * `"api-key:endpoint:*"`, `"api-key:model:*"`.
   *
   * @return the acls
   */
  public List<String> getAcls() {
    return acls;
  }

  /**
   * Sets A list of ACLs authorized with the API key, e.g.
   * `"api-key:endpoint:*"`, `"api-key:model:*"`.
   *
   * @param acls the acls
   */
  public void setAcls(List<String> acls) {
    this.acls = acls;
  }

  public String getApiKeyId() {
    return apiKeyId;
  }

  public void setApiKeyId(String apiKeyId) {
    this.apiKeyId = apiKeyId;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public String getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(String modifyTime) {
    this.modifyTime = modifyTime;
  }

  /**
   * Returns The name of the API key specified by user.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets The name of the API key specified by user.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  public String getRedactedApiKey() {
    return redactedApiKey;
  }

  public void setRedactedApiKey(String redactedApiKey) {
    this.redactedApiKey = redactedApiKey;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public boolean isApiKeyBlocked() {
    return apiKeyBlocked;
  }

  public void setApiKeyBlocked(boolean apiKeyBlocked) {
    this.apiKeyBlocked = apiKeyBlocked;
  }

  public boolean isApiKeyDisabled() {
    return apiKeyDisabled;
  }

  public void setApiKeyDisabled(boolean apiKeyDisabled) {
    this.apiKeyDisabled = apiKeyDisabled;
  }

  public boolean isTeamBlocked() {
    return teamBlocked;
  }

  public void setTeamBlocked(boolean teamBlocked) {
    this.teamBlocked = teamBlocked;
  }
}
