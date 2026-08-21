package org.example.usermanagement.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.example.usermanagement.model.User;
import org.example.usermanagement.support.UserManagementApiTest;
import org.testng.annotations.Test;

@Epic("User Management API")
@Feature("Delete user")
public class DeleteUserTests extends UserManagementApiTest {

  @Test
  @Issue("BUG-003")
  @Description(
      "DELETE /users/{email} with a valid token returns 204 and the user is no longer retrievable")
  public void deleteUserReturns204AndUserIsGone() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient().deleteUser(request.getEmail()).assertNoContent().assertAll();

    usersClient().getUserByEmail(request.getEmail()).assertNotFound().assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("After DELETE, GET /users no longer includes that email")
  public void deleteUserRemovesUserFromList() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient().deleteUser(request.getEmail()).assertNoContent().assertAll();

    usersClient().listUsers().assertOk().assertDoesNotContainEmail(request.getEmail()).assertAll();
  }

  @Test
  @Issue("BUG-004")
  @Description("DELETE /users/{email} without an Authentication header returns 401")
  public void deleteUserWithoutTokenReturns401() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient()
        .deleteUserUnauthenticated(request.getEmail())
        .assertUnauthorized()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-004")
  @Description("DELETE /users/{email} with an invalid Authentication token returns 401")
  public void deleteUserWithInvalidTokenReturns401() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient()
        .deleteUserWithToken(request.getEmail(), "invalid-token")
        .assertUnauthorized()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-004")
  @Description("DELETE /users/{email} with an empty Authentication token returns 401")
  public void deleteUserWithEmptyTokenReturns401() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient()
        .deleteUserWithToken(request.getEmail(), "")
        .assertUnauthorized()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-004")
  @Description("DELETE /users/{email} with Authorization instead of Authentication returns 401")
  public void deleteUserWithWrongHeaderNameReturns401() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient()
        .deleteUserWithHeader(request.getEmail(), "Authorization", "Bearer mysecrettoken")
        .assertUnauthorized()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("DELETE /users/{email} for an unknown email returns 404 and an error message")
  public void deleteUserNotFoundReturns404() {
    String email = uniqueEmail("missing-delete");
    usersClient().deleteUser(email).assertNotFound().assertErrorResponse().assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("DELETE /users/{email} with a SQL-injection-like path returns 404, not 500")
  public void deleteUserSqlInjectionPathReturns404() {
    usersClient().deleteUser("test' OR '1'='1").assertNotFound().assertErrorResponse().assertAll();
  }

  @Test
  @Issue("BUG-006")
  @Description("DELETE /users returns 405 Method Not Allowed")
  public void deleteUserOnCollectionPathReturns405() {
    usersClient().requestUsers("DELETE").assertMethodNotAllowed().assertAll();
  }
}
