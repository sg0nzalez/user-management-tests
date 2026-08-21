package org.example.usermanagement.common.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.nio.charset.StandardCharsets;
import org.example.usermanagement.common.config.BaseTest;
import org.testng.annotations.BeforeClass;

/**
 * Shared RestAssured setup for JSON APIs. {@link #setUpApi()} runs before subclass
 * {@code @BeforeClass} methods (TestNG superclass-first order), so clients can rely on {@link
 * #givenBase()} without a manual init guard.
 */
public abstract class BaseApiTest extends BaseTest {

  private RequestSpecification baseSpec;

  @BeforeClass(alwaysRun = true)
  public void setUpApi() {
    String baseUri = config().requireForEnvironment(environment(), "API_BASE_URL");
    baseSpec =
        new RequestSpecBuilder()
            .setBaseUri(baseUri)
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .setConfig(
                RestAssuredConfig.config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .defaultContentCharset(StandardCharsets.UTF_8)
                            .appendDefaultContentCharsetToContentTypeIfUndefined(true)))
            .addFilter(new AllureRestAssured())
            .addFilter(new RequestLoggingFilter())
            .addFilter(new ResponseLoggingFilter())
            .build();
  }

  protected RequestSpecification givenBase() {
    if (baseSpec == null) {
      throw new IllegalStateException("API not initialized; BaseApiTest.setUpApi did not run");
    }
    return RestAssured.given().spec(baseSpec);
  }

  protected RequestSpecification baseSpec() {
    if (baseSpec == null) {
      throw new IllegalStateException("API not initialized; BaseApiTest.setUpApi did not run");
    }
    return baseSpec;
  }
}
