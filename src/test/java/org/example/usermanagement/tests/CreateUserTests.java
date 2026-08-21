package org.example.usermanagement.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.example.usermanagement.common.http.Schemas;
import org.example.usermanagement.model.User;
import org.example.usermanagement.support.UserManagementApiTest;
import org.testng.annotations.Test;

@Epic("User Management API")
@Feature("Create user")
public class CreateUserTests extends UserManagementApiTest {

  @Test
  @Description("POST /users with valid payload returns 201 and the created User body")
  public void createUserReturns201() {
    User request = validUser();
    usersClient()
        .createUser(request)
        .assertCreated()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals(request.getName(), request.getEmail(), request.getAge())
        .assertAll();
  }

  @Test
  @Description("POST /users with age at minimum (1) returns 201")
  public void createUserAgeMinimumReturns201() {
    User request = User.builder().name("Jane Doe").email(uniqueEmail("age-min")).age(1).build();
    usersClient()
        .createUser(request)
        .assertCreated()
        .assertUserEquals(request.getName(), request.getEmail(), 1)
        .assertAll();
  }

  @Test
  @Description("POST /users with age at maximum (150) returns 201")
  public void createUserAgeMaximumReturns201() {
    User request = User.builder().name("Jane Doe").email(uniqueEmail("age-max")).age(150).build();
    usersClient()
        .createUser(request)
        .assertCreated()
        .assertUserEquals(request.getName(), request.getEmail(), 150)
        .assertAll();
  }

  @Test
  @Description("POST /users with empty body object returns 400 and an error message")
  public void createUserMissingFieldsReturns400() {
    usersClient().createUserRaw("{}").assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with missing name returns 400 and an error message")
  public void createUserMissingNameReturns400() {
    String json = "{\"email\":\"" + uniqueEmail("missing-name") + "\",\"age\":30}";
    usersClient().createUserRaw(json).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with missing email returns 400 and an error message")
  public void createUserMissingEmailReturns400() {
    usersClient()
        .createUserRaw("{\"name\":\"Jane Doe\",\"age\":30}")
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("POST /users with missing age returns 400 and an error message")
  public void createUserMissingAgeReturns400() {
    String json = "{\"name\":\"Jane Doe\",\"email\":\"" + uniqueEmail("missing-age") + "\"}";
    usersClient().createUserRaw(json).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with empty name returns 400 and an error message")
  public void createUserEmptyNameReturns400() {
    User request = User.builder().name("").email(uniqueEmail("empty-name")).age(30).build();
    usersClient().createUser(request).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Issue("BUG-002")
  @Description("POST /users with an invalid email format returns 400 and an error message")
  public void createUserInvalidEmailReturns400() {
    User request = User.builder().name("Jane Doe").email("not-an-email").age(30).build();
    usersClient().createUser(request).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with malformed JSON returns 400 and an error message")
  public void createUserMalformedJsonReturns400() {
    usersClient().createUserRaw("{bad").assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with non-integer age returns 400 and an error message")
  public void createUserAgeWrongTypeReturns400() {
    String json =
        "{\"name\":\"Jane Doe\",\"email\":\"" + uniqueEmail("age-type") + "\",\"age\":\"thirty\"}";
    usersClient().createUserRaw(json).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with age below minimum returns 400 and an error message")
  public void createUserAgeBelowMinimumReturns400() {
    usersClient()
        .createUser(User.builder().name("Jane Doe").email(uniqueEmail("age-low")).age(0).build())
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("POST /users with age above maximum returns 400 and an error message")
  public void createUserAgeAboveMaximumReturns400() {
    usersClient()
        .createUser(User.builder().name("Jane Doe").email(uniqueEmail("age-high")).age(151).build())
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-001")
  @Description("POST /users with a duplicate email returns 409 Conflict")
  public void createUserDuplicateEmailReturns409() {
    String email = uniqueEmail("duplicate");
    usersClient().createUser(validUser(email)).assertCreated().assertAll();

    usersClient().createUser(validUser(email)).assertConflict().assertErrorResponse().assertAll();
  }

  @Test
  @Issue("BUG-002")
  @Description("POST /users rejects SQL-injection-like email values with 400")
  public void createUserSqlInjectionEmailReturns400() {
    User request =
        User.builder().name("Jane Doe").email("test' OR '1'='1@example.com").age(30).build();
    usersClient().createUser(request).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Issue("BUG-002")
  @Description("POST /users rejects comment-style email values with 400")
  public void createUserSqlCommentEmailReturns400() {
    User request = User.builder().name("Jane Doe").email("admin'--@example.com").age(30).build();
    usersClient().createUser(request).assertBadRequest().assertErrorResponse().assertAll();
  }

  @Test
  @Description("POST /users with unknown JSON fields still returns only the User schema fields")
  public void createUserExtraFieldsAreNotReturned() {
    String email = uniqueEmail("extra-field");
    String json =
        "{\"name\":\"Jane Doe\",\"email\":\""
            + email
            + "\",\"age\":30,\"role\":\"admin\",\"id\":999,\"isAdmin\":true}";

    usersClient()
        .createUserRaw(json)
        .assertCreated()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals("Jane Doe", email, 30)
        .assertAll();

    usersClient()
        .getUserByEmail(email)
        .assertOk()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals("Jane Doe", email, 30)
        .assertAll();
  }

  @Test
  @Issue("BUG-008")
  @Description("POST /users with a non-JSON Content-Type returns 415 Unsupported Media Type")
  public void createUserWrongContentTypeReturns415() {
    String email = uniqueEmail("wrong-ct");
    String json = "{\"name\":\"Jane Doe\",\"email\":\"" + email + "\",\"age\":30}";

    usersClient()
        .createUserRawWithContentType(json, "text/plain")
        .assertUnsupportedMediaType()
        .assertAll();
  }

  @Test
  @Description("POST /users stores a script-like name as opaque text and returns it unchanged")
  public void createUserScriptLikeNameRoundTrips() {
    String email = uniqueEmail("xss-name");
    String name = "<script>alert(1)</script>";
    User request = User.builder().name(name).email(email).age(30).build();

    usersClient()
        .createUser(request)
        .assertCreated()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals(name, email, 30)
        .assertAll();

    usersClient().getUserByEmail(email).assertOk().assertUserEquals(name, email, 30).assertAll();
  }

  @Test
  @Issue("BUG-006")
  @Description("POST /users/{email} returns 405 Method Not Allowed")
  public void createUserOnResourcePathReturns405() {
    usersClient()
        .requestUserByEmail("POST", uniqueEmail("method-post"))
        .assertMethodNotAllowed()
        .assertAll();
  }
}
