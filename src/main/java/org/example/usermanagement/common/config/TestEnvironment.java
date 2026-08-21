package org.example.usermanagement.common.config;

import lombok.experimental.UtilityClass;

/**
 * Suite-scoped environment holder. Set once from required TestNG {@code Environment} / {@code
 * -Denv}; all test instances read the same value.
 */
@UtilityClass
public class TestEnvironment {

  private static Environment environment;
  private static ConfigLoader configLoader;
  private static AuthLoader authLoader;

  /** Resolves and locks the active environment for the JVM / suite. */
  public static void set(String environmentName) {
    String resolved = usable(environmentName) ? environmentName : System.getProperty("env");
    environment = Environment.fromProperty(resolved);
    configLoader = new ConfigLoader();
    authLoader = new AuthLoader(environment);
  }

  public static Environment current() {
    requireSet();
    return environment;
  }

  public static ConfigLoader config() {
    requireSet();
    return configLoader;
  }

  public static AuthLoader auth() {
    requireSet();
    return authLoader;
  }

  private static boolean usable(String value) {
    return value != null && !value.isBlank() && !value.contains("${");
  }

  private static void requireSet() {
    if (environment == null || configLoader == null || authLoader == null) {
      throw new IllegalStateException("Environment is not set. Pass -Denv=DEV|PROD.");
    }
  }
}
