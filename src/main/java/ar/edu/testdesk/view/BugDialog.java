package ar.edu.testdesk.view;

import ar.edu.testdesk.model.*;
import ar.edu.testdesk.service.Validation;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public final class BugDialog extends EditDialog<Bug> {
  private record Link(Long id, String label) {
    @Override
    public String toString() {
      return label;
    }
  }

  public BugDialog(
      Window owner, Bug b, List<TestCase> cases, List<TestResult> results, Consumer<Bug> onSave) {
    super(owner, b.id() == null ? "Nuevo bug" : "Bug #" + b.id());
    JTextField title = new JTextField(b.title());
    JTextArea description = area(b.description(), 3),
        steps = area(b.steps(), 4),
        expected = area(b.expected(), 3),
        actual = area(b.actual(), 4);
    JComboBox<String> priority = new JComboBox<>(new String[] {"Baja", "Media", "Alta", "Crítica"}),
        status = new JComboBox<>(new String[] {"Abierto", "En progreso", "Resuelto"});
    priority.setSelectedItem(b.priority());
    status.setSelectedItem(b.status());
    JComboBox<Link> caseLink = new JComboBox<>(), resultLink = new JComboBox<>();
    caseLink.addItem(new Link(null, "Sin vínculo"));
    resultLink.addItem(new Link(null, "Sin vínculo"));
    for (TestCase t : cases) {
      Link link = new Link(t.id(), t.toString());
      caseLink.addItem(link);
      if (t.id().equals(b.caseId())) caseLink.setSelectedItem(link);
    }
    for (TestResult r : results) {
      Link link = new Link(r.id(), r.toString());
      resultLink.addItem(link);
      if (r.id().equals(b.resultId())) resultLink.setSelectedItem(link);
    }
    caseLink.setPrototypeDisplayValue(new Link(null, "#0000 · Caso de prueba relacionado"));
    resultLink.setPrototypeDisplayValue(
        new Link(null, "#0000 · Resultado de ejecución relacionado"));
    field("Título *", title);
    areaField("Descripción", description);
    areaField("Pasos para reproducir", steps);
    areaField("Resultado esperado", expected);
    areaField("Resultado obtenido", actual);
    field("Prioridad", priority);
    field("Estado", status);
    field("Caso relacionado (opcional)", caseLink);
    field("Resultado relacionado (opcional)", resultLink);
    dates(b.createdAt(), b.updatedAt());
    submit(
        () -> {
          Bug value =
              new Bug(
                  b.id(),
                  title.getText().strip(),
                  description.getText(),
                  steps.getText(),
                  expected.getText(),
                  actual.getText(),
                  (String) priority.getSelectedItem(),
                  (String) status.getSelectedItem(),
                  ((Link) caseLink.getSelectedItem()).id(),
                  ((Link) resultLink.getSelectedItem()).id(),
                  b.createdAt(),
                  b.updatedAt());
          Validation.bug(value);
          return value;
        },
        onSave);
  }
}
