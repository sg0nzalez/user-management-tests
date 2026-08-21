package org.example.usermanagement.common.config;

import java.util.Properties;

/** Loads plaintext {@code config.properties} (URLs and non-secret settings). */
public final class ConfigLoader {

  private static final String RESOURCE = "config.properties";

  private final Properties properties;

  public ConfigLoader() {
    this.properties = ClasspathProperties.load(RESOURCE);
  }

  public String require(String key) {
    String value = properties.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required config key: " + key);
    }
    return value.trim();
  }

  public String get(String key, String defaultValue) {
    String value = properties.getProperty(key);
    return value == null || value.isBlank() ? defaultValue : value.trim();
  }

  public String requireForEnvironment(Environment environment, String suffix) {
    return require(environment.name() + "_" + suffix);
  }
}
