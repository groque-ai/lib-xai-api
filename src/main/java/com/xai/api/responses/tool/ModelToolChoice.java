package com.xai.api.responses.tool;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ModelToolChoiceDeserializer.class)
public abstract class ModelToolChoice {
}
