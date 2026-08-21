package org.example.usermanagement.common.http;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import io.restassured.response.Response;
import java.util.Locale;
import java.util.function.Consumer;
import org.example.usermanagement.model.ErrorResponse;

/**
 * Soft body and schema checks for {@link ApiResponse}. Status and {@code assertAll} stay on the
 * response wrapper; body/schema logic lives here (SRP).
 */
final class SoftResponseBodyChecks {

  private final Response response;
  private final Consumer<String> addError;

  SoftResponseBodyChecks(Response response, Consumer<String> addError) {
    this.response = response;
    this.addError = addError;
  }

  void assertBodyNotBlank() {
    String body = asString();
    if (body == null || body.isBlank()) {
      addError.accept("Expected a non-blank response body");
    }
  }

  void assertBodyContains(String... fragments) {
    String body = bodyOrEmpty();
    for (String fragment : fragments) {
      if (!body.contains(fragment)) {
        addError.accept(
            "Expected body to contain '" + fragment + "', got: " + truncateForAssert(body));
      }
    }
  }

  void assertBodyContainsIgnoringCase(String... fragments) {
    String body = bodyOrEmpty();
    String lower = body.toLowerCase(Locale.ROOT);
    for (String fragment : fragments) {
      if (!lower.contains(fragment.toLowerCase(Locale.ROOT))) {
        addError.accept(
            "Expected body to contain '"
                + fragment
                + "' (ignore case), got: "
                + truncateForAssert(body));
      }
    }
  }

  void assertMatchesSchema(String schemaClasspath) {
    try {
      response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaClasspath));
    } catch (AssertionError | RuntimeException ex) {
      addError.accept("JSON Schema '" + schemaClasspath + "': " + ex.getMessage());
    }
  }

  void assertErrorResponse() {
    assertMatchesSchema(Schemas.ERROR);
    try {
      ErrorResponse body = response.as(ErrorResponse.class);
      if (body == null || body.getError() == null || body.getError().isBlank()) {
        addError.accept(
            "Expected ErrorResponse.error with a message, got: "
                + truncateForAssert(bodyOrEmpty()));
      }
    } catch (RuntimeException ex) {
      addError.accept(
          "Expected JSON ErrorResponse, got status "
              + response.statusCode()
              + " body: "
              + truncateForAssert(bodyOrEmpty()));
    }
  }

  private String asString() {
    return response.asString();
  }

  private String bodyOrEmpty() {
    String body = asString();
    return body == null ? "" : body;
  }

  private static String truncateForAssert(String body) {
    if (body.length() <= 200) {
      return body;
    }
    return body.substring(0, 200) + "...";
  }
}
