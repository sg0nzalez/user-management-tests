package org.example.usermanagement.common.config;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Shared suite setup: TestNG {@code Environment} when the suite is used, otherwise {@code -Denv}.
 * Either way the value is required (no default).
 */
public abstract class BaseTest {

  @Parameters("Environment")
  @BeforeSuite(alwaysRun = true)
  public void beforeSuite(@Optional String environmentName) {
    TestEnvironment.set(environmentName);
  }

  protected Environment environment() {
    return TestEnvironment.current();
  }

  protected ConfigLoader config() {
    return TestEnvironment.config();
  }

  protected AuthLoader auth() {
    return TestEnvironment.auth();
  }
}
