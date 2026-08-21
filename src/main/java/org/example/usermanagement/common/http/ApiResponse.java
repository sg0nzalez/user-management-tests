package org.example.usermanagement.common.http;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.example.usermanagement.model.ErrorResponse;

/**
 * Fluent HTTP response wrapper with soft status/body asserts until {@link #assertAll()}.
 *
 * @param <S> concrete self type for fluent chaining
 */
@SuppressWarnings("PMD.GodClass")
public abstract class ApiResponse<S extends ApiResponse<S>> {

  private final Response response;
  private final List<String> errors = new ArrayList<>();

  protected ApiResponse(Response response) {
    if (response == null) {
      throw new IllegalArgumentException("Response must not be null");
    }
    this.response = response;
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
    if (asString() == null || asString().isBlank()) {
      addError("Expected a non-blank response body");
    }
    return self();
  }

  public S assertBodyContains(String... fragments) {
    String body = bodyOrEmpty();
    for (String fragment : fragments) {
      if (!body.contains(fragment)) {
        addError("Expected body to contain '" + fragment + "', got: " + truncateForAssert(body));
      }
    }
    return self();
  }

  public S assertBodyContainsIgnoringCase(String... fragments) {
    String body = bodyOrEmpty();
    String lower = body.toLowerCase(Locale.ROOT);
    for (String fragment : fragments) {
      if (!lower.contains(fragment.toLowerCase(Locale.ROOT))) {
        addError(
            "Expected body to contain '"
                + fragment
                + "' (ignore case), got: "
                + truncateForAssert(body));
      }
    }
    return self();
  }

  public S assertMatchesSchema(String schemaClasspath) {
    try {
      response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaClasspath));
    } catch (AssertionError ex) {
      addError("JSON Schema '" + schemaClasspath + "': " + ex.getMessage());
    }
    return self();
  }

  public ErrorResponse asErrorResponse() {
    return as(ErrorResponse.class);
  }

  /** Deserializes the body as {@link ErrorResponse} and asserts the error schema. */
  public S assertErrorResponse() {
    assertMatchesSchema(Schemas.ERROR);
    try {
      ErrorResponse body = asErrorResponse();
      if (body == null || body.getError() == null || body.getError().isBlank()) {
        addError(
            "Expected ErrorResponse.error with a message, got: "
                + truncateForAssert(bodyOrEmpty()));
      }
    } catch (RuntimeException ex) {
      addError(
          "Expected JSON ErrorResponse, got status "
              + statusCode()
              + " body: "
              + truncateForAssert(bodyOrEmpty()));
    }
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

  private String bodyOrEmpty() {
    return asString() == null ? "" : asString();
  }

  private static String truncateForAssert(String body) {
    if (body.length() <= 200) {
      return body;
    }
    return body.substring(0, 200) + "...";
  }
}
