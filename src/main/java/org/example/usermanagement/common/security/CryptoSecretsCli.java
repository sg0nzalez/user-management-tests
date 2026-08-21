package org.example.usermanagement.common.security;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * One-off CLI for {@link CryptoSecrets}: {@code java ... CryptoSecretsCli encrypt|decrypt <text>}
 * with {@code ENCRYPTION_MASTER_KEY} set.
 */
@Slf4j
@UtilityClass
public class CryptoSecretsCli {

  public void main(String[] args) {
    if (args.length != 2) {
      log.error("Usage: CryptoSecretsCli encrypt|decrypt <text>");
      System.exit(2);
    }
    String mode = args[0];
    String text = args[1];
    if ("encrypt".equalsIgnoreCase(mode)) {
      log.info(CryptoSecrets.encryptString(text));
      return;
    }
    if ("decrypt".equalsIgnoreCase(mode)) {
      log.info(CryptoSecrets.decryptString(text));
      return;
    }
    log.error("Unknown mode: {}", mode);
    System.exit(2);
  }
}
