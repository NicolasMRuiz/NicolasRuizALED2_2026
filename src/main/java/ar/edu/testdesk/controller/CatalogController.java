package ar.edu.testdesk.controller;

import ar.edu.testdesk.model.*;
import ar.edu.testdesk.service.*;
import ar.edu.testdesk.view.*;
import java.awt.Window;
import java.util.List;
import java.util.function.LongConsumer;

public final class CatalogController {
  private final MainFrame view;
  private final TaskRunner tasks;
  private final CatalogService catalog;
  private final HistoryService history;
  private final Runnable refresh;
  private final LongConsumer executeCase;

  public CatalogController(
      MainFrame view,
      TaskRunner tasks,
      CatalogService catalog,
      HistoryService history,
      Runnable refresh,
      LongConsumer executeCase) {
    this.view = view;
    this.tasks = tasks;
    this.catalog = catalog;
    this.history = history;
    this.refresh = refresh;
    this.executeCase = executeCase;
    view.newCase.addActionListener(e -> editCase(TestCase.empty()));
    view.editCase.addActionListener(e -> tasks.action(view, () -> editCase(view.cases.selected())));
    view.deleteCase.addActionListener(
        e ->
            tasks.action(
                view,
                () -> {
                  TestCase t = view.cases.selected();
                  if (TaskRunner.confirm(
                      view,
                      "¿Eliminar físicamente el caso #"
                          + t.id()
                          + "?\nSe quitará de los planes. El historial y los bugs se conservan."))
                    tasks.run(
                        view,
                        "Eliminando caso…",
                        () -> {
                          catalog.deleteCase(t.id());
                          return null;
                        },
                        v -> refresh.run());
                }));
    view.newPlan.addActionListener(e -> editPlan(TestPlan.empty()));
    view.editPlan.addActionListener(e -> tasks.action(view, () -> editPlan(view.plans.selected())));
    view.deletePlan.addActionListener(
        e ->
            tasks.action(
                view,
                () -> {
                  TestPlan p = view.plans.selected();
                  if (TaskRunner.confirm(
                      view,
                      "¿Eliminar el plan #" + p.id() + "? Los casos y el historial se conservan."))
                    tasks.run(
                        view,
                        "Eliminando plan…",
                        () -> {
                          catalog.deletePlan(p.id());
                          return null;
                        },
                        v -> refresh.run());
                }));
    view.newBug.addActionListener(e -> editBug(view, Bug.empty()));
    view.editBug.addActionListener(
        e -> tasks.action(view, () -> editBug(view, view.bugs.selected())));
    view.deleteBug.addActionListener(
        e ->
            tasks.action(
                view,
                () -> {
                  Bug b = view.bugs.selected();
                  if (TaskRunner.confirm(view, "¿Eliminar físicamente el bug #" + b.id() + "?"))
                    tasks.run(
                        view,
                        "Eliminando bug…",
                        () -> {
                          catalog.deleteBug(b.id());
                          return null;
                        },
                        v -> refresh.run());
                }));
  }

  private void editCase(TestCase t) {
    CaseDialog[] dialog = new CaseDialog[1];
    dialog[0] =
        new CaseDialog(
            view,
            t,
            value ->
                tasks.run(
                    dialog[0],
                    "Guardando caso…",
                    () -> catalog.saveCase(value),
                    id -> {
                      dialog[0].dispose();
                      refresh.run();
                    }));
    dialog[0].execute.addActionListener(
        e -> {
          dialog[0].dispose();
          executeCase.accept(t.id());
        });
    dialog[0].open();
  }

  private void editPlan(TestPlan p) {
    tasks.run(
        view,
        "Cargando selector de casos…",
        () -> catalog.cases("", null, null),
        cases -> {
          PlanDialog[] dialog = new PlanDialog[1];
          dialog[0] =
              new PlanDialog(
                  view,
                  p,
                  cases,
                  value ->
                      tasks.run(
                          dialog[0],
                          "Guardando plan y relaciones…",
                          () -> catalog.savePlan(value),
                          id -> {
                            dialog[0].dispose();
                            refresh.run();
                          }));
          dialog[0].open();
        });
  }

  private record Links(List<TestCase> cases, List<TestResult> results) {}

  public void editBug(Window owner, Bug b) {
    tasks.run(
        owner,
        "Cargando vínculos…",
        () -> new Links(catalog.cases("", null, null), history.allResults()),
        links -> {
          BugDialog[] dialog = new BugDialog[1];
          dialog[0] =
              new BugDialog(
                  owner,
                  b,
                  links.cases(),
                  links.results(),
                  value ->
                      tasks.run(
                          dialog[0],
                          "Guardando bug…",
                          () -> catalog.saveBug(value),
                          id -> {
                            dialog[0].dispose();
                            if (owner == view) refresh.run();
                            else
                              TaskRunner.info(
                                  owner, "Bug guardado. Puede consultarlo en la sección Bugs.");
                          }));
          dialog[0].open();
        });
  }
}
