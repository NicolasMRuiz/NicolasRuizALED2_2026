package ar.edu.testdesk.controller;

import ar.edu.testdesk.http.*;
import ar.edu.testdesk.service.*;
import ar.edu.testdesk.view.MainFrame;
import java.awt.event.*;
import javax.swing.*;

public final class AppController {
  private final MainFrame view;
  private final TaskRunner tasks;
  private final CatalogService catalog;
  private final HistoryService history;

  public AppController(
      MainFrame view,
      CatalogService catalog,
      HistoryService history,
      ExecutionService execution,
      DemoServer demo,
      HttpExecutor http) {
    this.view = view;
    this.catalog = catalog;
    this.history = history;
    tasks = new TaskRunner(view);
    ExecutionController executions =
        new ExecutionController(view, tasks, execution, history, catalog);
    CatalogController catalogs =
        new CatalogController(view, tasks, catalog, history, this::refresh, executions::runCase);
    executions.setCatalogController(catalogs);
    for (JButton button :
        new JButton[] {
          view.refreshHome, view.findCases, view.findPlans, view.findBugs, view.findRuns
        }) button.addActionListener(e -> refresh());
    view.caseSearch.addActionListener(e -> refresh());
    view.planSearch.addActionListener(e -> refresh());
    view.bugSearch.addActionListener(e -> refresh());
    view.tabs.addChangeListener(
        e -> {
          if (!tasks.isBusy()) refresh();
        });
    view.startDemo.addActionListener(
        e ->
            tasks.run(
                view,
                "Iniciando API demo…",
                () -> {
                  demo.start();
                  return null;
                },
                v -> view.demoStatus.setText("API demo activa · http://127.0.0.1:8089")));
    view.stopDemo.addActionListener(
        e ->
            tasks.run(
                view,
                "Deteniendo API demo…",
                () -> {
                  demo.close();
                  return null;
                },
                v -> view.demoStatus.setText("API demo detenida · 127.0.0.1:8089")));
    view.seed.addActionListener(
        e ->
            tasks.run(
                view,
                "Creando ejemplos…",
                catalog::createExamples,
                count -> {
                  TaskRunner.info(
                      view,
                      "Se crearon "
                          + count
                          + " casos de ejemplo. Los existentes no se modificaron.");
                  refresh();
                }));
    view.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent event) {
            if (tasks.isBusy()) {
              TaskRunner.info(
                  view, "Hay una operación en curso. Espere a que termine antes de cerrar.");
              return;
            }
            demo.close();
            http.close();
            view.dispose();
          }
        });
  }

  public void start() {
    view.setVisible(true);
    refresh();
  }

  public void refresh() {
    switch (view.tabs.getSelectedIndex()) {
      case 0 -> tasks.run(view, "Actualizando tablero…", history::dashboard, view::showDashboard);
      case 1 -> {
        String search = view.caseSearch.getText().strip();
        String method =
            view.method.getSelectedIndex() == 0 ? null : (String) view.method.getSelectedItem();
        Boolean active =
            view.active.getSelectedIndex() == 0 ? null : view.active.getSelectedIndex() == 1;
        tasks.run(
            view,
            "Cargando casos…",
            () -> catalog.cases(search, method, active),
            view.cases::setRows);
      }
      case 2 -> {
        String search = view.planSearch.getText().strip();
        tasks.run(view, "Cargando planes…", () -> catalog.plans(search), view.plans::setRows);
      }
      case 3 -> {
        String search = view.bugSearch.getText().strip(),
            status =
                view.bugStatus.getSelectedIndex() == 0
                    ? null
                    : (String) view.bugStatus.getSelectedItem(),
            priority =
                view.priority.getSelectedIndex() == 0
                    ? null
                    : (String) view.priority.getSelectedItem();
        tasks.run(
            view,
            "Cargando bugs…",
            () -> catalog.bugs(search, status, priority),
            view.bugs::setRows);
      }
      case 4 -> tasks.run(view, "Cargando historial…", history::runs, view.runs::setRows);
      default -> throw new IllegalStateException("Sección desconocida");
    }
  }
}
