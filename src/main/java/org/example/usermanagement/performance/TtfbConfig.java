package org.example.usermanagement.performance;

import java.time.Duration;
import lombok.Builder;
import lombok.Value;

/** Immutable TTFB probe settings. */
@Value
@Builder
public class TtfbConfig {

  int concurrency;
  double p90ThresholdMs;
  Duration connectTimeout;
  Duration requestTimeout;

  public static TtfbConfig defaults() {
    return TtfbConfig.builder()
        .concurrency(20)
        .p90ThresholdMs(250)
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(15))
        .build();
  }
}
