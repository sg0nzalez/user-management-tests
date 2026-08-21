package org.example.usermanagement.performance;

/** Builds a probe per parallel slot (0-based). */
@FunctionalInterface
public interface TtfbRequestFactory {

  TtfbProbe create(int slotIndex);
}
