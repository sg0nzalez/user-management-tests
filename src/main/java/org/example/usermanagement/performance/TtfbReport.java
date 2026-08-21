package org.example.usermanagement.performance;

import java.util.Locale;
import lombok.experimental.UtilityClass;

/** Formats a TTFB result for Allure. */
@UtilityClass
public class TtfbReport {

  public String render(TtfbResult result) {
    TtfbStats stats = result.getStats();
    return String.format(
        Locale.ROOT,
        """
        label: %s
        concurrency: %d
        p90 threshold: %.0f ms
        samples: %d
        transport failures: %d
        min: %.2f ms
        mean: %.2f ms
        p50: %.2f ms
        p90: %.2f ms
        p95: %.2f ms
        p99: %.2f ms
        max: %.2f ms
        """,
        result.getLabel(),
        result.getConfig().getConcurrency(),
        result.getConfig().getP90ThresholdMs(),
        result.getSamples().size(),
        result.transportFailureCount(),
        ms(stats.getMinNanos()),
        ms(stats.getMeanNanos()),
        ms(stats.getP50Nanos()),
        ms(stats.getP90Nanos()),
        ms(stats.getP95Nanos()),
        ms(stats.getP99Nanos()),
        ms(stats.getMaxNanos()));
  }

  private double ms(long nanos) {
    return nanos / 1_000_000.0;
  }
}
