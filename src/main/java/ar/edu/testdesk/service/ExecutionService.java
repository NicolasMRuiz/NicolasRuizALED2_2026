package ar.edu.testdesk.service;

import ar.edu.testdesk.dao.*;
import ar.edu.testdesk.http.HttpExecutor;
import ar.edu.testdesk.model.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ExecutionService {
  private final CaseDao cases;
  private final PlanDao plans;
  private final RunDao runs;
  private final HttpExecutor http;
  private final AtomicBoolean running = new AtomicBoolean();

  public ExecutionService(CaseDao cases, PlanDao plans, RunDao runs, HttpExecutor http) {
    this.cases = cases;
    this.plans = plans;
    this.runs = runs;
    this.http = http;
  }

  public long runCase(long id, Consumer<String> progress) {
    TestCase t = cases.find(id);
    if (!t.active())
      throw new IllegalArgumentException("El caso está inactivo. Actívelo antes de ejecutar.");
    return execute(null, "Caso: " + t.title(), List.of(t), progress);
  }

  public long runPlan(TestPlan p, Consumer<String> progress) {
    return execute(p.id(), "Plan: " + p.name(), plans.cases(p.id()), progress);
  }

  private long execute(Long planId, String origin, List<TestCase> all, Consumer<String> progress) {
    List<TestCase> active = all.stream().filter(TestCase::active).toList();
    if (active.isEmpty())
      throw new IllegalArgumentException(
          all.isEmpty()
              ? "El plan está vacío; agregue casos antes de ejecutarlo."
              : "El plan no contiene casos activos ejecutables.");
    active.forEach(Validation::testCase);
    if (!running.compareAndSet(false, true))
      throw new IllegalStateException("Ya hay una ejecución en curso.");
    Long runId = null;
    long start = System.nanoTime();
    try {
      runId = runs.start(planId, origin, all.size() - active.size());
      for (int i = 0; i < active.size(); i++) {
        progress.accept(
            "Ejecutando "
                + (i + 1)
                + "/"
                + active.size()
                + " · Omitidos inactivos: "
                + (all.size() - active.size()));
        runs.append(runId, http.execute(active.get(i)));
      }
      runs.finish(runId, "COMPLETED", elapsed(start));
      return runId;
    } catch (RuntimeException e) {
      if (runId != null)
        try {
          runs.finish(runId, "INTERRUPTED", elapsed(start));
        } catch (RuntimeException secondary) {
          e.addSuppressed(secondary);
        }
      throw e;
    } finally {
      running.set(false);
    }
  }

  private long elapsed(long start) {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
  }
}
