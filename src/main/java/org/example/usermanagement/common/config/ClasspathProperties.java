package org.example.usermanagement.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.experimental.UtilityClass;

/** Loads {@code .properties} files from the classpath. */
@UtilityClass
public class ClasspathProperties {

  public Properties load(String resourceName) {
    Properties properties = new Properties();
    try (InputStream input =
        ClasspathProperties.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (input == null) {
        throw new IllegalStateException("Classpath resource not found: " + resourceName);
      }
      properties.load(input);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to load " + resourceName, ex);
    }
    return properties;
  }
}
