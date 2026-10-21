package ar.edu.testdesk.model;

import java.time.LocalDateTime;

public record TestCase(
    Long id,
    String title,
    String description,
    String method,
    String url,
    String headers,
    String body,
    int expectedStatus,
    String expectedText,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static TestCase empty() {
    return new TestCase(
        null, "", "", "GET", "http://127.0.0.1:8089/ok", "", "", 200, "", true, null, null);
  }

  @Override
  public String toString() {
    return id + " · " + title + (active ? "" : " (inactivo)");
  }
}
