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
@Feature("Update user")
public class UpdateUserTests extends UserManagementApiTest {

  @Test
  @Description("PUT /users/{email} updates an existing user and returns 200")
  public void updateUserReturns200() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    User update = User.builder().name("Updated Name").email(created.getEmail()).age(31).build();

    usersClient()
        .updateUser(created.getEmail(), update)
        .assertOk()
        .assertMatchesSchema(Schemas.USER)
        .assertUserEquals(update.getName(), update.getEmail(), update.getAge())
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} with age at minimum (1) returns 200")
  public void updateUserAgeMinimumReturns200() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    User update = User.builder().name("Jane Doe").email(created.getEmail()).age(1).build();

    usersClient()
        .updateUser(created.getEmail(), update)
        .assertOk()
        .assertUserEquals(update.getName(), update.getEmail(), 1)
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} with age at maximum (150) returns 200")
  public void updateUserAgeMaximumReturns200() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    User update = User.builder().name("Jane Doe").email(created.getEmail()).age(150).build();

    usersClient()
        .updateUser(created.getEmail(), update)
        .assertOk()
        .assertUserEquals(update.getName(), update.getEmail(), 150)
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} for an unknown email returns 404 and an error message")
  public void updateUserNotFoundReturns404() {
    User update =
        User.builder().name("Jane Doe").email(uniqueEmail("missing-update")).age(30).build();

    usersClient()
        .updateUser(update.getEmail(), update)
        .assertNotFound()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} with invalid email format returns 400 and an error message")
  public void updateUserInvalidEmailReturns400() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    usersClient()
        .updateUserRaw(
            created.getEmail(), "{\"name\":\"Jane Doe\",\"email\":\"bad-email\",\"age\":30}")
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} with age below minimum returns 400 and an error message")
  public void updateUserAgeBelowMinimumReturns400() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    User update = User.builder().name("Jane Doe").email(created.getEmail()).age(0).build();

    usersClient()
        .updateUser(created.getEmail(), update)
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} with age above maximum returns 400 and an error message")
  public void updateUserAgeAboveMaximumReturns400() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    User update = User.builder().name("Jane Doe").email(created.getEmail()).age(151).build();

    usersClient()
        .updateUser(created.getEmail(), update)
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} with empty body object returns 400 and an error message")
  public void updateUserMissingFieldsReturns400() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    usersClient()
        .updateUserRaw(created.getEmail(), "{}")
        .assertBadRequest()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Description("PUT /users/{email} changing email to an existing user's email returns 409")
  public void updateUserDuplicateEmailReturns409() {
    String firstEmail = uniqueEmail("update-first");
    String secondEmail = uniqueEmail("update-second");
    usersClient().createUser(validUser(firstEmail)).assertCreated().assertAll();
    usersClient().createUser(validUser(secondEmail)).assertCreated().assertAll();

    User conflict = User.builder().name("Jane Doe").email(firstEmail).age(30).build();

    usersClient()
        .updateUser(secondEmail, conflict)
        .assertConflict()
        .assertErrorResponse()
        .assertAll();
  }

  @Test
  @Issue("BUG-007")
  @Description("After a successful PUT, GET returns the updated user fields")
  public void updateUserIsVisibleOnSubsequentGet() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    User update =
        User.builder().name("Visible After Update").email(created.getEmail()).age(42).build();
    usersClient().updateUser(created.getEmail(), update).assertOk().assertAll();

    usersClient()
        .getUserByEmail(created.getEmail())
        .assertOk()
        .assertUserEquals(update.getName(), update.getEmail(), update.getAge())
        .assertAll();
  }

  @Test
  @Description(
      "PUT /users/{email} with unknown JSON fields still returns only the User schema fields")
  public void updateUserExtraFieldsAreNotReturned() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    String json =
        "{\"name\":\"Jane Doe\",\"email\":\""
            + created.getEmail()
            + "\",\"age\":30,\"role\":\"admin\",\"password\":\"secret\"}";

    usersClient()
        .updateUserRaw(created.getEmail(), json)
        .assertOk()
        .assertMatchesSchema(Schemas.USER)
        .assertAll();
  }

  @Test
  @Issue("BUG-006")
  @Description("PUT /users returns 405 Method Not Allowed")
  public void updateUserOnCollectionPathReturns405() {
    usersClient().requestUsers("PUT").assertMethodNotAllowed().assertAll();
  }

  @Test
  @Issue("BUG-006")
  @Description("PATCH /users returns 405 Method Not Allowed")
  public void patchUserOnCollectionPathReturns405() {
    usersClient().requestUsers("PATCH").assertMethodNotAllowed().assertAll();
  }

  @Test
  @Issue("BUG-006")
  @Description("PATCH /users/{email} returns 405 Method Not Allowed")
  public void patchUserOnResourcePathReturns405() {
    usersClient()
        .requestUserByEmail("PATCH", uniqueEmail("method-patch"))
        .assertMethodNotAllowed()
        .assertAll();
  }
}
