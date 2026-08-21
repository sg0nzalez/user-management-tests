package org.example.usermanagement.clients;

import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.example.usermanagement.common.config.AuthLoader;
import org.example.usermanagement.common.http.AuthHeaders;
import org.example.usermanagement.http.UserListResponse;
import org.example.usermanagement.http.UserResponse;
import org.example.usermanagement.model.User;
import org.example.usermanagement.routes.UsersRoutes;

/** User Management CRUD client. RestAssured stays here; tests call {@code @Step} methods. */
@RequiredArgsConstructor
public final class UsersClient {

  private final Supplier<RequestSpecification> spec;
  private final AuthLoader auth;

  @Step("List users")
  public UserListResponse listUsers() {
    return UserListResponse.of(
        spec.get().when().get(UsersRoutes.users()).then().extract().response());
  }

  @Step("List users with query {name}={value}")
  public UserListResponse listUsersWithQuery(String name, String value) {
    return UserListResponse.of(
        spec.get()
            .queryParam(name, value)
            .when()
            .get(UsersRoutes.users())
            .then()
            .extract()
            .response());
  }

  @Step("Create user {request.email}")
  public UserResponse createUser(User request) {
    return UserResponse.of(
        spec.get().body(request).when().post(UsersRoutes.users()).then().extract().response());
  }

  @Step("Create user with raw JSON body")
  public UserResponse createUserRaw(String jsonBody) {
    return UserResponse.of(
        spec.get().body(jsonBody).when().post(UsersRoutes.users()).then().extract().response());
  }

  @Step("Create user with Content-Type={contentType}")
  public UserResponse createUserRawWithContentType(String jsonBody, String contentType) {
    return UserResponse.of(
        spec.get()
            .contentType(contentType)
            .body(jsonBody)
            .when()
            .post(UsersRoutes.users())
            .then()
            .extract()
            .response());
  }

  @Step("Get user {email}")
  public UserResponse getUserByEmail(String email) {
    return UserResponse.of(
        spec.get().when().get(UsersRoutes.userByEmail(), email).then().extract().response());
  }

  @Step("Get user {email} with query {name}={value}")
  public UserResponse getUserByEmailWithQuery(String email, String name, String value) {
    return UserResponse.of(
        spec.get()
            .queryParam(name, value)
            .when()
            .get(UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }

  @Step("Update user {email}")
  public UserResponse updateUser(String email, User request) {
    return UserResponse.of(
        spec.get()
            .body(request)
            .when()
            .put(UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }

  @Step("Update user {email} with raw JSON body")
  public UserResponse updateUserRaw(String email, String jsonBody) {
    return UserResponse.of(
        spec.get()
            .body(jsonBody)
            .when()
            .put(UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }

  @Step("Delete user {email}")
  public UserResponse deleteUser(String email) {
    return UserResponse.of(
        spec.get()
            .header(AuthHeaders.AUTHENTICATION, auth.require("AUTH_TOKEN"))
            .when()
            .delete(UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }

  @Step("Delete user {email} without Authentication")
  public UserResponse deleteUserUnauthenticated(String email) {
    return UserResponse.of(
        spec.get().when().delete(UsersRoutes.userByEmail(), email).then().extract().response());
  }

  @Step("Delete user {email} with Authentication={token}")
  public UserResponse deleteUserWithToken(String email, String token) {
    return UserResponse.of(
        spec.get()
            .header(AuthHeaders.AUTHENTICATION, token)
            .when()
            .delete(UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }

  @Step("Delete user {email} with header {headerName}")
  public UserResponse deleteUserWithHeader(String email, String headerName, String headerValue) {
    return UserResponse.of(
        spec.get()
            .header(headerName, headerValue)
            .when()
            .delete(UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }

  @Step("{method} /users")
  public UserResponse requestUsers(String method) {
    return UserResponse.of(
        spec.get().when().request(method, UsersRoutes.users()).then().extract().response());
  }

  @Step("{method} /users/{email}")
  public UserResponse requestUserByEmail(String method, String email) {
    return UserResponse.of(
        spec.get()
            .when()
            .request(method, UsersRoutes.userByEmail(), email)
            .then()
            .extract()
            .response());
  }
}
