package com.llamacpp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Optional metadata describing the underlying GGUF model.
 * <p>
 * Present only when the model is loaded.
 * <p>
 * Mirrors the C++ struct `llama_model_meta`, including: - vocabulary size -
 * context window - embedding dimension - parameter count - file size
 */
public class ModelMeta {

  /**
   * Vocabulary type (GGUF-specific).
   */
  @JsonProperty("vocab_type")
  private Integer vocabType;

  /**
   * Number of vocabulary tokens.
   */
  @JsonProperty("n_vocab")
  private Integer nVocab;

  /**
   * Runtime context window size.
   */
  @JsonProperty("n_ctx")
  private Integer nCtx;

  /**
   * Training-time context window size.
   */
  @JsonProperty("n_ctx_train")
  private Integer nCtxTrain;

  /**
   * Embedding dimension.
   */
  @JsonProperty("n_embd")
  private Integer nEmbd;

  /**
   * Total number of model parameters.
   */
  @JsonProperty("n_params")
  private Long nParams;

  /**
   * Size of the GGUF model file in bytes.
   */
  @JsonProperty("size")
  private Long size;

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public Integer getVocabType() {
    return vocabType;
  }

  public void setVocabType(Integer vocabType) {
    this.vocabType = vocabType;
  }

  public Integer getnCtx() {
    return nCtx;
  }

  public void setnCtx(Integer nCtx) {
    this.nCtx = nCtx;
  }

  public Integer getnCtxTrain() {
    return nCtxTrain;
  }

  public void setnCtxTrain(Integer nCtxTrain) {
    this.nCtxTrain = nCtxTrain;
  }

  public Integer getnEmbd() {
    return nEmbd;
  }

  public void setnEmbd(Integer nEmbd) {
    this.nEmbd = nEmbd;
  }

  public Long getnParams() {
    return nParams;
  }

  public void setnParams(Long nParams) {
    this.nParams = nParams;
  }

  public Integer getnVocab() {
    return nVocab;
  }

  public void setnVocab(Integer nVocab) {
    this.nVocab = nVocab;
  }

}
