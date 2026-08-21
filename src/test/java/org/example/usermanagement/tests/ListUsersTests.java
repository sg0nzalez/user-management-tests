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
@Feature("List users")
public class ListUsersTests extends UserManagementApiTest {

  @Test
  @Description("GET /users returns 200 and a JSON array matching the User list schema")
  public void listUsersReturns200() {
    usersClient().listUsers().assertOk().assertMatchesSchema(Schemas.USER_LIST).assertAll();
  }

  @Test
  @Description("GET /users includes a user that was just created via POST /users")
  public void listIncludesCreatedUser() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();

    usersClient().listUsers().assertOk().assertContainsEmail(request.getEmail()).assertAll();
  }

  @Test
  @Issue("BUG-003")
  @Description("GET /users does not include a user after it was deleted")
  public void listExcludesDeletedUser() {
    User request = validUser();
    usersClient().createUser(request).assertCreated().assertAll();
    usersClient().deleteUser(request.getEmail(), true).assertNoContent().assertAll();

    usersClient().listUsers().assertOk().assertDoesNotContainEmail(request.getEmail()).assertAll();
  }

  @Test
  @Description("GET /users ignores unexpected query parameters and still returns 200")
  public void listUsersIgnoresExtraQueryParams() {
    usersClient()
        .listUsersWithQuery("admin", "true")
        .assertOk()
        .assertMatchesSchema(Schemas.USER_LIST)
        .assertAll();
  }
}
