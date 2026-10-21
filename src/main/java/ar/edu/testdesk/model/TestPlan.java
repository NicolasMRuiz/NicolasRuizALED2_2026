package ar.edu.testdesk.model;

import java.time.LocalDateTime;
import java.util.List;

public record TestPlan(
    Long id,
    String name,
    String description,
    List<Long> caseIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public TestPlan {
    caseIds = List.copyOf(caseIds);
  }

  public static TestPlan empty() {
    return new TestPlan(null, "", "", List.of(), null, null);
  }
}
