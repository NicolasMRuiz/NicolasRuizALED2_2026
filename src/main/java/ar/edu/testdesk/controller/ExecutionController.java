package ar.edu.testdesk.controller;

import ar.edu.testdesk.model.*;
import ar.edu.testdesk.service.*;
import ar.edu.testdesk.view.*;
import java.nio.file.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class ExecutionController {
  private final MainFrame view;
  private final TaskRunner tasks;
  private final ExecutionService execution;
  private final HistoryService history;
  private final CatalogService catalog;
  private CatalogController catalogs;

  public ExecutionController(
      MainFrame view,
      TaskRunner tasks,
      ExecutionService execution,
      HistoryService history,
      CatalogService catalog) {
    this.view = view;
    this.tasks = tasks;
    this.execution = execution;
    this.history = history;
    this.catalog = catalog;
    view.runCase.addActionListener(
        e -> tasks.action(view, () -> runCase(view.cases.selected().id())));
    view.runPlan.addActionListener(
        e ->
            tasks.action(
                view,
                () -> {
                  TestPlan p = view.plans.selected();
                  tasks.run(
                      view,
                      "Ejecutando plan…",
                      () -> execution.runPlan(p, tasks::progress),
                      this::completed);
                }));
    view.detailRun.addActionListener(
        e -> tasks.action(view, () -> showResults(view.runs.selected())));
    view.exportRun.addActionListener(e -> tasks.action(view, () -> export(view.runs.selected())));
  }

  public void setCatalogController(CatalogController catalogs) {
    this.catalogs = catalogs;
  }

  public void runCase(long id) {
    tasks.run(
        view, "Ejecutando caso…", () -> execution.runCase(id, tasks::progress), this::completed);
  }

  private record RunDetails(List<TestRun> runs, TestRun run, List<TestResult> results) {}

  private void completed(long id) {
    tasks.run(
        view,
        "Cargando resultados persistidos…",
        () -> {
          List<TestRun> runs = history.runs();
          TestRun run = runs.stream().filter(r -> r.id() == id).findFirst().orElseThrow();
          return new RunDetails(runs, run, history.results(id));
        },
        data -> {
          view.runs.setRows(data.runs());
          view.tabs.setSelectedIndex(4);
          openResults(data.run(), data.results());
        });
  }

  private void showResults(TestRun run) {
    tasks.run(
        view,
        "Cargando resultados…",
        () -> history.results(run.id()),
        rows -> openResults(run, rows));
  }

  private void openResults(TestRun run, List<TestResult> rows) {
    ResultsDialog dialog = new ResultsDialog(view, run, rows);
    dialog.bug.addActionListener(
        e ->
            tasks.action(
                dialog, () -> catalogs.editBug(dialog, catalog.bugFrom(dialog.table.selected()))));
    dialog.setVisible(true);
  }

  private void export(TestRun run) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Exportar reporte HTML");
    chooser.setFileFilter(new FileNameExtensionFilter("Reporte HTML", "html"));
    chooser.setSelectedFile(new java.io.File("testdesk-corrida-" + run.id() + ".html"));
    if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;
    Path chosen = chooser.getSelectedFile().toPath();
    if (!chosen.toString().toLowerCase(java.util.Locale.ROOT).endsWith(".html"))
      chosen = Path.of(chosen + ".html");
    if (Files.exists(chosen) && !TaskRunner.confirm(view, "El archivo ya existe. ¿Reemplazarlo?"))
      return;
    Path output = chosen;
    tasks.run(
        view,
        "Exportando HTML…",
        () -> {
          history.export(output, run);
          return output;
        },
        path ->
            TaskRunner.info(
                view,
                "Reporte guardado en:\n"
                    + path.toAbsolutePath()
                    + "\nPuede abrirlo en cualquier navegador sin conexión."));
  }
}
