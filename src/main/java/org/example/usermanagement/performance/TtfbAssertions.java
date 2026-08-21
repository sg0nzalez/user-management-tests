package org.example.usermanagement.performance;

import io.qameta.allure.Allure;
import lombok.experimental.UtilityClass;
import org.testng.Assert;

/** Assertions and Allure attachment for a TTFB run. */
@UtilityClass
public class TtfbAssertions {

  /** Attach report, assert zero transport failures, and assert p90 under threshold. */
  public void assertSuccessful(TtfbResult result) {
    attachReport(result);
    assertNoTransportFailures(result);
    assertP90(result);
  }

  public void attachReport(TtfbResult result) {
    Allure.addAttachment(
        "TTFB " + result.getLabel(), "text/plain", TtfbReport.render(result), ".txt");
  }

  public void assertNoTransportFailures(TtfbResult result) {
    Assert.assertEquals(
        result.transportFailureCount(),
        0,
        "Expected 0 transport failures for " + result.getLabel());
  }

  public void assertP90(TtfbResult result) {
    double actual = result.getStats().p90Ms();
    double threshold = result.getConfig().getP90ThresholdMs();
    Assert.assertTrue(
        actual < threshold,
        String.format(
            java.util.Locale.ROOT,
            "%s p90 was %.2f ms, expected < %.0f ms",
            result.getLabel(),
            actual,
            threshold));
  }
}
