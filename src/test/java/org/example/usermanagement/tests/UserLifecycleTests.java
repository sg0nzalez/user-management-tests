package org.example.usermanagement.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.example.usermanagement.model.User;
import org.example.usermanagement.support.UserManagementApiTest;
import org.testng.annotations.Test;

@Epic("User Management API")
@Feature("User lifecycle")
public class UserLifecycleTests extends UserManagementApiTest {

  @Test
  @Issue("BUG-003")
  @Issue("BUG-007")
  @Description(
      "Create → get → update → get → delete → get/list confirms the user moves through each state")
  public void createUpdateDeleteLifecycle() {
    User created = validUser();
    usersClient().createUser(created).assertCreated().assertAll();

    usersClient()
        .getUserByEmail(created.getEmail())
        .assertOk()
        .assertUserEquals(created.getName(), created.getEmail(), created.getAge())
        .assertAll();

    User update =
        User.builder().name("Lifecycle Updated").email(created.getEmail()).age(45).build();
    usersClient().updateUser(created.getEmail(), update).assertOk().assertAll();

    usersClient()
        .getUserByEmail(created.getEmail())
        .assertOk()
        .assertUserEquals(update.getName(), update.getEmail(), update.getAge())
        .assertAll();

    usersClient().listUsers().assertOk().assertContainsEmail(created.getEmail()).assertAll();

    usersClient().deleteUser(created.getEmail()).assertNoContent().assertAll();

    usersClient().getUserByEmail(created.getEmail()).assertNotFound().assertAll();
    usersClient().listUsers().assertOk().assertDoesNotContainEmail(created.getEmail()).assertAll();
  }
}
