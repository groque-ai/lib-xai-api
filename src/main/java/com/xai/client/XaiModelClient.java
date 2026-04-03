package com.xai.client;

import com.xai.api.models.ListImageGenerationModelsResponse;
import com.xai.api.models.ListLanguageModelsResponse;
import com.xai.api.models.ListModelsResponse;
import java.net.http.HttpRequest;

/**
 * Client for interacting with XAI model APIs.
 * <p>
 * This class provides methods to retrieve lists of available models,
 * including general models, language models, and image generation models.
 * <p>
 * It extends {@link XaiAbstractClient} to handle HTTP requests and responses.
 * <p>
 * All methods are thread-safe as they do not maintain any mutable state.
 */
public class XaiModelClient extends XaiAbstractClient {

  /** Path for the models endpoint. */
  private static final String MODELS_PATH = "/models";

  /** Path for the language models endpoint. */
  private static final String LANGUAGE_MODELS_PATH = "/language-models";

  /** Path for the image generation models endpoint. */
  private static final String IMAGE_MODELS_PATH = "/image-generation-models";

  /**
   * Constructs a new XaiModelClient with default configuration.
   * <p>
   * The default configuration uses an empty base URL, which should be set appropriately
   * by the extending class or through configuration.
   */
  public XaiModelClient() {
    super("");
  }

  /**
   * Retrieves a list of all available models.
   * <p>
   * This method sends a GET request to the models endpoint and parses the response
   * into a {@link ListModelsResponse} object.
   *
   * @return a {@link ListModelsResponse} containing the list of models
   */
  public ListModelsResponse getModels() {
    HttpRequest request = doGet(MODELS_PATH);
    return sendRequest(request, ListModelsResponse.class);
  }

  /**
   * Retrieves a list of all available language models.
   * <p>
   * This method sends a GET request to the language models endpoint and parses the response
   * into a {@link ListLanguageModelsResponse} object.
   *
   * @return a {@link ListLanguageModelsResponse} containing the list of language models
   */
  public ListLanguageModelsResponse getLanguageModels() {
    HttpRequest request = doGet(LANGUAGE_MODELS_PATH);
    return sendRequest(request, ListLanguageModelsResponse.class);
  }

  /**
   * Retrieves a list of all available image generation models.
   * <p>
   * This method sends a GET request to the image generation models endpoint and parses the response
   * into a {@link ListImageGenerationModelsResponse} object.
   *
   * @return a {@link ListImageGenerationModelsResponse} containing the list of image generation models
   */
  public ListImageGenerationModelsResponse getImageGenerationModels() {
    HttpRequest request = doGet(IMAGE_MODELS_PATH);
    return sendRequest(request, ListImageGenerationModelsResponse.class);
  }

}