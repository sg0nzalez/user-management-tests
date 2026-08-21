package org.example.usermanagement.common.config;

import java.util.Locale;

/** Supported runtime environments selected with {@code -Denv} (required; no default). */
public enum Environment {
  DEV,
  PROD;

  public static Environment fromProperty(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required environment (pass -Denv=DEV|PROD)");
    }
    String trimmed = value.trim();
    if (trimmed.contains("${")) {
      throw new IllegalStateException(
          "Environment property was not resolved (pass -Denv=DEV|PROD); got: " + trimmed);
    }
    return Environment.valueOf(trimmed.toUpperCase(Locale.ROOT));
  }
}
