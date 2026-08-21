package org.example.usermanagement.performance;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/** Fluent builder for a single TTFB HTTP probe. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TtfbProbe {

  private final String method;
  private final URI uri;
  private final String label;
  private final String body;
  private final Map<String, String> headers;

  public static TtfbProbe get(String url) {
    return new TtfbProbe("GET", URI.create(url), url, null, Map.of());
  }

  public static TtfbProbe post(String url) {
    return new TtfbProbe("POST", URI.create(url), url, null, Map.of());
  }

  public static TtfbProbe put(String url) {
    return new TtfbProbe("PUT", URI.create(url), url, null, Map.of());
  }

  public static TtfbProbe delete(String url) {
    return new TtfbProbe("DELETE", URI.create(url), url, null, Map.of());
  }

  public TtfbProbe label(String newLabel) {
    return new TtfbProbe(method, uri, newLabel, body, headers);
  }

  public TtfbProbe jsonBody(String json) {
    Map<String, String> next = new LinkedHashMap<>(headers);
    next.putIfAbsent("Content-Type", "application/json");
    return new TtfbProbe(method, uri, label, json, Map.copyOf(next));
  }

  public TtfbProbe header(String name, String value) {
    Map<String, String> next = new LinkedHashMap<>(headers);
    next.put(name, value);
    return new TtfbProbe(method, uri, label, body, Map.copyOf(next));
  }

  public String label() {
    return label;
  }

  HttpRequest toRequest(Duration timeout) {
    HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(timeout);
    headers.forEach(builder::header);
    HttpRequest.BodyPublisher publisher =
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body);
    return builder.method(method, publisher).build();
  }
}
