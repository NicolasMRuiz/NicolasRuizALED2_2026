package ar.edu.testdesk.view;

import ar.edu.testdesk.model.TestCase;
import ar.edu.testdesk.service.Validation;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public final class CaseDialog extends EditDialog<TestCase> {
  public final JButton execute = new JButton("Ejecutar versión guardada");

  public CaseDialog(Window owner, TestCase t, Consumer<TestCase> onSave) {
    super(owner, t.id() == null ? "Nuevo caso de prueba" : "Caso #" + t.id());
    JTextField title = new JTextField(t.title()), url = new JTextField(t.url());
    JComboBox<String> method = new JComboBox<>(new String[] {"GET", "POST", "PUT", "DELETE"});
    method.setSelectedItem(t.method());
    JTextArea description = area(t.description(), 3),
        headers = area(t.headers(), 4),
        body = area(t.body(), 5),
        expectedText = area(t.expectedText(), 3);
    JSpinner code = new JSpinner(new SpinnerNumberModel(t.expectedStatus(), 100, 599, 1));
    JCheckBox active = new JCheckBox("Caso activo", t.active());
    field("Título * (hasta 160 caracteres)", title);
    areaField("Descripción", description);
    field("Método HTTP *", method);
    field("URL HTTP/HTTPS *", url);
    areaField("Headers opcionales · Nombre: valor, uno por línea (sin duplicados)", headers);
    areaField("Body opcional · GET nunca envía body", body);
    field("Código HTTP esperado * (100–599)", code);
    areaField("Fragmento esperado · coincidencia literal, sensible a mayúsculas", expectedText);
    field("Estado", active);
    dates(t.createdAt(), t.updatedAt());
    if (t.id() != null)
      field("La ejecución usa los datos persistidos. Guarde primero los cambios.", execute);
    method.addActionListener(e -> body.setEnabled(!"GET".equals(method.getSelectedItem())));
    body.setEnabled(!t.method().equals("GET"));
    submit(
        () -> {
          try {
            code.commitEdit();
          } catch (java.text.ParseException e) {
            throw new IllegalArgumentException("Código esperado inválido.");
          }
          TestCase value =
              new TestCase(
                  t.id(),
                  title.getText().strip(),
                  description.getText(),
                  (String) method.getSelectedItem(),
                  url.getText().strip(),
                  headers.getText(),
                  body.getText(),
                  ((Number) code.getValue()).intValue(),
                  expectedText.getText(),
                  active.isSelected(),
                  t.createdAt(),
                  t.updatedAt());
          Validation.testCase(value);
          return value;
        },
        onSave);
  }
}
