package ar.edu.testdesk.service;

import ar.edu.testdesk.dao.*;
import ar.edu.testdesk.model.*;
import java.util.List;

public final class CatalogService {
  private final CaseDao cases;
  private final PlanDao plans;
  private final BugDao bugs;

  public CatalogService(CaseDao cases, PlanDao plans, BugDao bugs) {
    this.cases = cases;
    this.plans = plans;
    this.bugs = bugs;
  }

  public List<TestCase> cases(String search, String method, Boolean active) {
    return cases.list(search, method, active);
  }

  public TestCase findCase(long id) {
    return cases.find(id);
  }

  public long saveCase(TestCase t) {
    Validation.testCase(t);
    return cases.save(t);
  }

  public void deleteCase(long id) {
    cases.delete(id);
  }

  public List<TestPlan> plans(String search) {
    return plans.list(search);
  }

  public long savePlan(TestPlan p) {
    Validation.plan(p);
    return plans.save(p);
  }

  public void deletePlan(long id) {
    plans.delete(id);
  }

  public List<Bug> bugs(String search, String status, String priority) {
    return bugs.list(search, status, priority);
  }

  public long saveBug(Bug b) {
    Validation.bug(b);
    return bugs.save(b);
  }

  public void deleteBug(long id) {
    bugs.delete(id);
  }

  public int createExamples() {
    List<TestCase> examples =
        List.of(
            example("Demo · 200 correcto", "GET", "/ok", 200, "TestDesk", ""),
            example("Demo · 404 esperado", "GET", "/missing", 404, "no encontrado", ""),
            example("Demo · 500 esperado", "GET", "/error", 500, "error demo", ""),
            example("Demo · POST con body", "POST", "/echo", 200, "hola", "hola TestDesk"),
            example("Demo · FAIL controlado", "GET", "/ok", 201, "", ""),
            example("Demo · ERROR por timeout", "GET", "/slow", 200, "", ""));
    examples.forEach(Validation::testCase);
    return cases.seed(examples);
  }

  private TestCase example(
      String title, String method, String path, int status, String text, String body) {
    return new TestCase(
        null,
        title,
        "Ejemplo local. Inicie la API demo en Inicio.",
        method,
        "http://127.0.0.1:8089" + path,
        "Content-Type: text/plain; charset=UTF-8",
        body,
        status,
        text,
        true,
        null,
        null);
  }

  public Bug bugFrom(TestResult r) {
    if (r.status().equals("PASS"))
      throw new IllegalArgumentException("Seleccione un resultado FAIL o ERROR.");
    String title = "Fallo: " + r.caseTitle();
    return new Bug(
        null,
        title.substring(0, Math.min(160, title.length())),
        r.message(),
        "Ejecutar " + r.method() + " " + r.url() + "\nCorrida #" + r.runId(),
        "HTTP " + r.expectedStatus() + "\n" + r.expectedText(),
        "HTTP " + r.actualStatus() + "\n" + r.responseExcerpt(),
        "Media",
        "Abierto",
        r.caseId(),
        r.id(),
        null,
        null);
  }
}
