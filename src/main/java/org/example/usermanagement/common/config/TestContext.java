package org.example.usermanagement.common.config;

/**
 * Immutable suite context: active environment plus config/auth loaders. Created once per JVM suite
 * (see {@code BaseTest}); prefer this over scattered static loaders when scaling or paralleling.
 */
public final class TestContext {

  private final Environment environment;
  private final ConfigLoader config;
  private final AuthLoader auth;

  private TestContext(Environment environment, ConfigLoader config, AuthLoader auth) {
    this.environment = environment;
    this.config = config;
    this.auth = auth;
  }

  /**
   * Resolves {@code environmentName} when usable; otherwise {@code -Denv}. Either way the value is
   * required (no default).
   */
  public static TestContext resolve(String environmentName) {
    String resolved = usable(environmentName) ? environmentName : System.getProperty("env");
    Environment environment = Environment.fromProperty(resolved);
    return new TestContext(environment, new ConfigLoader(), new AuthLoader(environment));
  }

  public Environment environment() {
    return environment;
  }

  public ConfigLoader config() {
    return config;
  }

  public AuthLoader auth() {
    return auth;
  }

  private static boolean usable(String value) {
    return value != null && !value.isBlank() && !value.contains("${");
  }
}
