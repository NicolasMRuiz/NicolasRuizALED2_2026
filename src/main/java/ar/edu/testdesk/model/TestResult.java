package ar.edu.testdesk.model;

import java.time.LocalDateTime;

public record TestResult(
    Long id,
    long runId,
    Long caseId,
    String caseTitle,
    String method,
    String url,
    int expectedStatus,
    Integer actualStatus,
    String expectedText,
    LocalDateTime startedAt,
    long durationMs,
    String status,
    String message,
    String responseExcerpt) {
  @Override
  public String toString() {
    return "#" + id + " · " + caseTitle + " · " + status;
  }
}
