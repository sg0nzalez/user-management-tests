package org.example.usermanagement.common.config;

import java.util.Properties;
import lombok.Getter;
import org.example.usermanagement.common.security.CryptoSecrets;

/**
 * Loads {@code auth.properties} and decrypts {@code ENC(...)} values for the active environment.
 */
public final class AuthLoader {

  private static final String RESOURCE = "auth.properties";

  private final Properties properties;
  @Getter private final Environment environment;

  public AuthLoader(Environment environment) {
    this.environment = environment;
    this.properties = ClasspathProperties.load(RESOURCE);
  }

  public String require(String suffix) {
    String key = environment.name() + "_" + suffix;
    String raw = properties.getProperty(key);
    if (raw == null || raw.isBlank()) {
      throw new IllegalStateException("Missing required auth key: " + key);
    }
    return CryptoSecrets.maybeDecrypt(raw.trim());
  }
}
