package org.example.usermanagement.performance;

import java.util.List;
import lombok.Value;

/** Aggregated TTFB run for one labeled probe. */
@Value
public class TtfbResult {

  String label;
  TtfbConfig config;
  List<TtfbSample> samples;
  TtfbStats stats;

  public long transportFailureCount() {
    return samples.stream().filter(TtfbSample::transportFailed).count();
  }
}
