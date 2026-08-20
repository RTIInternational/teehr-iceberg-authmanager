package org.teehr.iceberg.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BrokerTokenClientTest {

  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void doesNotFallbackToDirectSubjectTokenExchangeWhenSessionRequestFails() throws Exception {
    AtomicInteger sessionCalls = new AtomicInteger();
    AtomicInteger directCalls = new AtomicInteger();

    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/auth/polaris-token/session",
        exchange -> {
          sessionCalls.incrementAndGet();
          writeJson(exchange, 401, "Delegated broker session not found");
        });
    server.createContext(
        "/auth/polaris-token",
        exchange -> {
          directCalls.incrementAndGet();
          writeJson(exchange, 200, "{\"access_token\":\"token\",\"expires_at_epoch_seconds\":9999999999}");
        });
    server.start();

    BrokerTokenClient client = new BrokerTokenClient(HttpClient.newHttpClient());
    String brokerUrl =
        "http://127.0.0.1:" + server.getAddress().getPort() + "/auth/polaris-token/session";

    IOException error =
        assertThrows(
            IOException.class,
            () ->
                client.mintToken(
                    brokerUrl,
                    "user-1",
                    "session-1",
                    "realm-1",
                    "iceberg",
                    "account",
                    "broker-session-token",
                    "subject-token",
                    600L,
                    Duration.ofSeconds(2)));

    assertTrue(error.getMessage().contains("401"));
    assertEquals(1, sessionCalls.get());
    assertEquals(0, directCalls.get());
  }

  @Test
  void usesBrokerSessionHeaderWhenProvided() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/auth/polaris-token/session",
        new HeaderAssertingHandler("X-Broker-Session-Token", "broker-session-token"));
    server.start();

    BrokerTokenClient client = new BrokerTokenClient(HttpClient.newHttpClient());
    String brokerUrl =
        "http://127.0.0.1:" + server.getAddress().getPort() + "/auth/polaris-token/session";

    BrokerToken token =
        client.mintToken(
            brokerUrl,
            "user-1",
            "session-1",
            "realm-1",
            "iceberg",
            "account",
            "broker-session-token",
            "subject-token",
            600L,
            Duration.ofSeconds(2));

    assertEquals("token", token.accessToken());
  }

  private static void writeJson(HttpExchange exchange, int status, String responseBody) throws IOException {
    byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, body.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(body);
    }
  }

  private static final class HeaderAssertingHandler implements HttpHandler {

    private final String headerName;
    private final String expectedValue;

    private HeaderAssertingHandler(String headerName, String expectedValue) {
      this.headerName = headerName;
      this.expectedValue = expectedValue;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      assertEquals(expectedValue, exchange.getRequestHeaders().getFirst(headerName));
      writeJson(exchange, 200, "{\"access_token\":\"token\",\"expires_at_epoch_seconds\":9999999999}");
    }
  }
}