package org.teehr.iceberg.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.apache.iceberg.rest.auth.AuthSession;
import org.junit.jupiter.api.Test;

class TeehrBrokerAuthManagerTest {

  @Test
  void acceptsTrustedInClusterHttpBrokerUrl() {
    TeehrBrokerAuthManager manager = new TeehrBrokerAuthManager("test");

    AuthSession session =
        manager.catalogSession(null, properties("http://teehr-api:8000/auth/polaris-token/session"));

    assertNotNull(session);
    manager.close();
  }

  @Test
  void acceptsTrustedLocalHttpsBrokerUrl() {
    TeehrBrokerAuthManager manager = new TeehrBrokerAuthManager("test");

    AuthSession session =
        manager.catalogSession(
            null, properties("https://api.teehr.local.app.garden/auth/polaris-token/session"));

    assertNotNull(session);
    manager.close();
  }

  @Test
  void rejectsUntrustedBrokerUrl() {
    TeehrBrokerAuthManager manager = new TeehrBrokerAuthManager("test");

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                manager.catalogSession(
                    null, properties("https://evil.example.com/auth/polaris-token/session")));

    assertNotNull(error.getMessage());
  }

  private static Map<String, String> properties(String brokerUrl) {
    Map<String, String> properties = new HashMap<>();
    properties.put(TeehrAuthProperties.BROKER_URL, brokerUrl);
    properties.put(TeehrAuthProperties.USER_ID, "user-1");
    properties.put(TeehrAuthProperties.SESSION_ID, "session-1");
    properties.put(TeehrAuthProperties.REALM, "realm-1");
    return properties;
  }
}