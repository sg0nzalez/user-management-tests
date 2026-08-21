package org.example.usermanagement.performance;

import lombok.Value;

/** One completed TTFB sample. */
@Value
public class TtfbSample {

  long ttfbNanos;
  int statusCode;
  String error;

  public boolean transportFailed() {
    return error != null && !error.isBlank();
  }
}
