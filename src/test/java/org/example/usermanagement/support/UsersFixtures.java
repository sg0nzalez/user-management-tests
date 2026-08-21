package org.example.usermanagement.support;

import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.example.usermanagement.model.User;

/** Shared fixture helpers for user payloads (composition, not inheritance). */
@UtilityClass
public class UsersFixtures {

  public String uniqueEmail() {
    return "test-" + UUID.randomUUID() + "@example.com";
  }

  public String uniqueEmail(String prefix) {
    return prefix + "-" + UUID.randomUUID() + "@example.com";
  }

  public User validUser() {
    return User.builder().name("Jane Doe").email(uniqueEmail()).age(30).build();
  }

  public User validUser(String email) {
    return User.builder().name("Jane Doe").email(email).age(30).build();
  }
}
