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
@Feature("Get user")
public class GetUserTests extends UserManagementApiTest {

  @Test
  @Description("GET /users/{email} for an existing user returns 200 and the User body")
  public void getUserReturns200() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient()
        .getUserByEmail(request.getEmail())
        .assertOk()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals(request.getName(), request.getEmail(), request.getAge())
        .assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("GET /users/{email} for an unknown email returns 404 and an error message")
  public void getUserNotFoundReturns404() {
    String email = uniqueEmail("missing");
    usersClient().getUserByEmail(email).assertNotFound().assertErrorResponse().assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("GET /users/{email} with a SQL-injection-like path returns 404, not 500")
  public void getUserSqlInjectionPathReturns404() {
    usersClient()
        .getUserByEmail("test' OR '1'='1")
        .assertNotFound()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("GET /users/{email} with a path-traversal-like email returns 404, not 500")
  public void getUserPathTraversalEmailReturns404() {
    usersClient()
        .getUserByEmail("../prod/users")
        .assertNotFound()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("GET /users/{email} ignores unexpected query parameters and still returns the user")
  public void getUserIgnoresExtraQueryParams() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    usersClient()
        .getUserByEmailWithQuery(created.getEmail(), "role", "admin")
        .assertOk()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals(created.getName(), created.getEmail(), created.getAge())
        .assertAll();
  }
}
