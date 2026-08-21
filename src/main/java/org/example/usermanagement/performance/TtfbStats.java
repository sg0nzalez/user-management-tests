package org.example.usermanagement.performance;

import java.util.List;
import lombok.Value;

/** Percentile summary of successful TTFB samples (nanoseconds). */
@Value
public class TtfbStats {

  long minNanos;
  long meanNanos;
  long p50Nanos;
  long p90Nanos;
  long p95Nanos;
  long p99Nanos;
  long maxNanos;

  public static TtfbStats of(List<Long> sortedNanos) {
    if (sortedNanos.isEmpty()) {
      return new TtfbStats(0, 0, 0, 0, 0, 0, 0);
    }
    long sum = 0;
    for (long value : sortedNanos) {
      sum += value;
    }
    return new TtfbStats(
        sortedNanos.get(0),
        sum / sortedNanos.size(),
        percentile(sortedNanos, 0.50),
        percentile(sortedNanos, 0.90),
        percentile(sortedNanos, 0.95),
        percentile(sortedNanos, 0.99),
        sortedNanos.get(sortedNanos.size() - 1));
  }

  /** Nearest-rank percentile (1-based rank = ceil(p * n)). */
  static long percentile(List<Long> sortedNanos, double percentile) {
    int n = sortedNanos.size();
    int rank = (int) Math.ceil(percentile * n);
    int index = Math.max(0, Math.min(n - 1, rank - 1));
    return sortedNanos.get(index);
  }

  public double p90Ms() {
    return p90Nanos / 1_000_000.0;
  }
}
