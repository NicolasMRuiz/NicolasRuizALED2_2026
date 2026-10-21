package ar.edu.testdesk.service;

import ar.edu.testdesk.dao.RunDao;
import ar.edu.testdesk.model.*;
import ar.edu.testdesk.report.HtmlReport;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class HistoryService {
  private final RunDao runs;
  private final HtmlReport report;

  public HistoryService(RunDao runs, HtmlReport report) {
    this.runs = runs;
    this.report = report;
  }

  public List<TestRun> runs() {
    return runs.list();
  }

  public List<TestResult> results(long id) {
    return runs.results(id);
  }

  public List<TestResult> allResults() {
    return runs.allResults();
  }

  public Dashboard dashboard() {
    return runs.dashboard();
  }

  public void export(Path path, TestRun run) throws IOException {
    report.export(path, run, runs.results(run.id()));
  }
}
