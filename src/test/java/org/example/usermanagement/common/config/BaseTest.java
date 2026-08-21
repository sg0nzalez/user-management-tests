package org.example.usermanagement.common.config;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Shared suite setup: TestNG {@code Environment} when the suite is used, otherwise {@code -Denv}.
 * Either way the value is required (no default). Holds a single {@link TestContext} for the suite.
 */
public abstract class BaseTest {

  private static TestContext context;

  @Parameters("Environment")
  @BeforeSuite(alwaysRun = true)
  public void beforeSuite(@Optional String environmentName) {
    context = TestContext.resolve(environmentName);
  }

  protected final TestContext context() {
    if (context == null) {
      throw new IllegalStateException("TestContext is not set. Pass -Denv=DEV|PROD.");
    }
    return context;
  }

  protected Environment environment() {
    return context().environment();
  }

  protected ConfigLoader config() {
    return context().config();
  }

  protected AuthLoader auth() {
    return context().auth();
  }
}
