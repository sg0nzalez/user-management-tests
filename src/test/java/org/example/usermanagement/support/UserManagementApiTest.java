package org.example.usermanagement.support;

import org.example.usermanagement.clients.UsersClient;
import org.example.usermanagement.common.api.BaseApiTest;
import org.example.usermanagement.model.User;
import org.testng.annotations.BeforeClass;

/** Shared API test support: RestAssured base spec and {@link UsersClient}. */
public abstract class UserManagementApiTest extends BaseApiTest {

  private UsersClient usersClient;

  @BeforeClass(alwaysRun = true)
  public void setUpUsersClient() {
    usersClient = new UsersClient(this::givenBase, auth());
  }

  protected UsersClient usersClient() {
    return usersClient;
  }

  protected String uniqueEmail() {
    return UsersFixtures.uniqueEmail();
  }

  protected String uniqueEmail(String prefix) {
    return UsersFixtures.uniqueEmail(prefix);
  }

  protected User validUser() {
    return UsersFixtures.validUser();
  }

  protected User validUser(String email) {
    return UsersFixtures.validUser(email);
  }
}
