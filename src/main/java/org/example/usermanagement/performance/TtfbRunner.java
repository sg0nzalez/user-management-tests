package org.example.usermanagement.performance;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.experimental.UtilityClass;

/** Fires N parallel probes and aggregates TTFB samples. */
@UtilityClass
public class TtfbRunner {

  public TtfbResult run(TtfbProbe probe) {
    return run(TtfbConfig.defaults(), probe.label(), slot -> probe);
  }

  public TtfbResult run(TtfbConfig config, TtfbProbe probe) {
    return run(config, probe.label(), slot -> probe);
  }

  public TtfbResult run(TtfbRequestFactory factory) {
    return run(TtfbConfig.defaults(), "ttfb", factory);
  }

  public TtfbResult run(TtfbConfig config, String label, TtfbRequestFactory factory) {
    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(config.getConnectTimeout())
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    List<CompletableFuture<TtfbSample>> futures = new ArrayList<>();
    for (int slot = 0; slot < config.getConcurrency(); slot++) {
      TtfbProbe probe = factory.create(slot);
      futures.add(send(client, config, probe));
    }

    List<TtfbSample> samples = new ArrayList<>();
    for (CompletableFuture<TtfbSample> future : futures) {
      try {
        samples.add(future.get());
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        samples.add(new TtfbSample(0, -1, "interrupted: " + ex.getMessage()));
      } catch (ExecutionException ex) {
        samples.add(new TtfbSample(0, -1, String.valueOf(ex.getCause())));
      }
    }

    List<Long> sorted =
        samples.stream()
            .filter(sample -> !sample.transportFailed())
            .map(TtfbSample::getTtfbNanos)
            .sorted(Comparator.naturalOrder())
            .toList();
    return new TtfbResult(label, config, List.copyOf(samples), TtfbStats.of(sorted));
  }

  private CompletableFuture<TtfbSample> send(
      HttpClient client, TtfbConfig config, TtfbProbe probe) {
    Instant start = Instant.now();
    return client
        .sendAsync(
            probe.toRequest(config.getRequestTimeout()), HttpResponse.BodyHandlers.ofInputStream())
        .handle(
            (response, error) -> {
              long nanos = java.time.Duration.between(start, Instant.now()).toNanos();
              if (error != null) {
                return new TtfbSample(nanos, -1, error.getMessage());
              }
              try {
                if (response.body() != null) {
                  response.body().close();
                }
              } catch (Exception ignored) {
                // Body drain is best-effort; TTFB already measured at header arrival.
              }
              return new TtfbSample(nanos, response.statusCode(), null);
            });
  }
}
