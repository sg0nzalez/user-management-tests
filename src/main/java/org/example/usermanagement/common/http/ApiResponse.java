package org.example.usermanagement.common.http;

import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.example.usermanagement.model.ErrorResponse;

/**
 * Fluent HTTP response wrapper with soft status asserts until {@link #assertAll()}. Body and schema
 * checks are delegated to {@link SoftResponseBodyChecks}.
 *
 * @param <S> concrete self type for fluent chaining
 */
public abstract class ApiResponse<S extends ApiResponse<S>> {

  private final Response response;
  private final List<String> errors = new ArrayList<>();
  private final SoftResponseBodyChecks bodyChecks;

  protected ApiResponse(Response response) {
    if (response == null) {
      throw new IllegalArgumentException("Response must not be null");
    }
    this.response = response;
    this.bodyChecks = new SoftResponseBodyChecks(response, this::addError);
  }

  protected abstract S self();

  protected void addError(String message) {
    errors.add(message);
  }

  public Response raw() {
    return response;
  }

  public int statusCode() {
    return response.statusCode();
  }

  public String asString() {
    return response.asString();
  }

  public <T> T as(Class<T> type) {
    return response.as(type);
  }

  public S assertOk() {
    return assertStatus(200);
  }

  public S assertCreated() {
    return assertStatus(201);
  }

  public S assertNoContent() {
    return assertStatus(204);
  }

  public S assertBadRequest() {
    return assertStatus(400);
  }

  public S assertUnauthorized() {
    return assertStatus(401);
  }

  public S assertNotFound() {
    return assertStatus(404);
  }

  public S assertConflict() {
    return assertStatus(409);
  }

  public S assertMethodNotAllowed() {
    return assertStatus(405);
  }

  public S assertUnsupportedMediaType() {
    return assertStatus(415);
  }

  public S assertStatus(int expected) {
    int actual = statusCode();
    if (actual != expected) {
      addError("Expected HTTP " + expected + " but was " + actual);
    }
    return self();
  }

  public S assertStatusOneOf(int... expected) {
    int actual = statusCode();
    for (int code : expected) {
      if (actual == code) {
        return self();
      }
    }
    String wanted =
        Arrays.stream(expected).mapToObj(String::valueOf).collect(Collectors.joining("/"));
    addError("Expected HTTP " + wanted + " but was " + actual);
    return self();
  }

  public S assertBodyNotBlank() {
    bodyChecks.assertBodyNotBlank();
    return self();
  }

  public S assertBodyContains(String... fragments) {
    bodyChecks.assertBodyContains(fragments);
    return self();
  }

  public S assertBodyContainsIgnoringCase(String... fragments) {
    bodyChecks.assertBodyContainsIgnoringCase(fragments);
    return self();
  }

  public S assertMatchesSchema(String schemaClasspath) {
    bodyChecks.assertMatchesSchema(schemaClasspath);
    return self();
  }

  public ErrorResponse asErrorResponse() {
    return as(ErrorResponse.class);
  }

  /** Deserializes the body as {@link ErrorResponse} and asserts the error schema. */
  public S assertErrorResponse() {
    bodyChecks.assertErrorResponse();
    return self();
  }

  public void assertAll() {
    if (errors.isEmpty()) {
      return;
    }
    String message = String.join("; ", errors);
    errors.clear();
    throw new AssertionError(message);
  }
}
