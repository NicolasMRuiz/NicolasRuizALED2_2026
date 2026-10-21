package ar.edu.testdesk.model;

import java.time.LocalDateTime;

public record Bug(
    Long id,
    String title,
    String description,
    String steps,
    String expected,
    String actual,
    String priority,
    String status,
    Long caseId,
    Long resultId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static Bug empty() {
    return new Bug(null, "", "", "", "", "", "Media", "Abierto", null, null, null, null);
  }
}
