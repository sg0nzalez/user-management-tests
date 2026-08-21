package org.example.usermanagement.common.http;

import lombok.experimental.UtilityClass;

/** HTTP header names used by the User Management API. */
@UtilityClass
public class AuthHeaders {

  /** Spec name for DELETE authentication (not {@code Authorization}). */
  public final String AUTHENTICATION = "Authentication";
}
