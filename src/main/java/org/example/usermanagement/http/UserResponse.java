package org.example.usermanagement.http;

import io.restassured.response.Response;
import org.example.usermanagement.common.http.ApiResponse;
import org.example.usermanagement.model.User;

/** Typed wrapper for single-user JSON responses. */
public final class UserResponse extends ApiResponse<UserResponse> {

  private UserResponse(Response response) {
    super(response);
  }

  public static UserResponse of(Response response) {
    return new UserResponse(response);
  }

  @Override
  protected UserResponse self() {
    return this;
  }

  public User asUser() {
    return as(User.class);
  }

  public UserResponse assertUserEquals(String name, String email, int age) {
    User user = asUser();
    if (user == null) {
      addError("Expected a User JSON body");
      return this;
    }
    if (!name.equals(user.getName())) {
      addError("Expected name '" + name + "' but was '" + user.getName() + "'");
    }
    if (!email.equals(user.getEmail())) {
      addError("Expected email '" + email + "' but was '" + user.getEmail() + "'");
    }
    if (user.getAge() == null || user.getAge() != age) {
      addError("Expected age " + age + " but was " + user.getAge());
    }
    return this;
  }
}
