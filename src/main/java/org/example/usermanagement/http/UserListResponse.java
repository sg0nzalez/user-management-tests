package org.example.usermanagement.http;

import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import org.example.usermanagement.common.http.ApiResponse;
import org.example.usermanagement.model.User;

/** Typed wrapper for {@code GET /users} JSON array responses. */
public final class UserListResponse extends ApiResponse<UserListResponse> {

  private UserListResponse(Response response) {
    super(response);
  }

  public static UserListResponse of(Response response) {
    return new UserListResponse(response);
  }

  @Override
  protected UserListResponse self() {
    return this;
  }

  public List<User> asUsers() {
    User[] users = as(User[].class);
    return users == null ? List.of() : Arrays.asList(users);
  }

  public UserListResponse assertContainsEmail(String email) {
    boolean found = asUsers().stream().anyMatch(user -> email.equals(user.getEmail()));
    if (!found) {
      addError("Expected list to contain email " + email);
    }
    return this;
  }

  public UserListResponse assertDoesNotContainEmail(String email) {
    boolean found = asUsers().stream().anyMatch(user -> email.equals(user.getEmail()));
    if (found) {
      addError("Expected list not to contain email " + email);
    }
    return this;
  }
}
