package ar.edu.testdesk.model;

import java.time.LocalDateTime;

public record TestRun(
    long id,
    Long planId,
    String origin,
    LocalDateTime startedAt,
    long durationMs,
    int total,
    int passed,
    int failed,
    int errors,
    int skipped,
    String status) {}
