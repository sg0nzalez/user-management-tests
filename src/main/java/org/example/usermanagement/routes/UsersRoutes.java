package org.example.usermanagement.routes;

import lombok.experimental.UtilityClass;

/** User Management API path fragments (base URI comes from config). */
@UtilityClass
public class UsersRoutes {

  private final String USERS = "/users";

  public String users() {
    return USERS;
  }

  public String userByEmail() {
    return USERS + "/{email}";
  }
}
