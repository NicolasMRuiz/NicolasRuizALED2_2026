package ar.edu.testdesk.view;

import ar.edu.testdesk.model.*;
import ar.edu.testdesk.service.Validation;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public final class PlanDialog extends EditDialog<TestPlan> {
  public PlanDialog(Window owner, TestPlan p, List<TestCase> cases, Consumer<TestPlan> onSave) {
    super(owner, p.id() == null ? "Nuevo plan" : "Plan #" + p.id());
    JTextField name = new JTextField(p.name());
    JTextArea description = area(p.description(), 3);
    Set<Long> selected = new LinkedHashSet<>(p.caseIds());
    JTable table =
        new JTable(
            new AbstractTableModel() {
              private final String[] columns = {"Incluir", "ID", "Caso", "Método", "Estado"};

              public int getRowCount() {
                return cases.size();
              }

              public int getColumnCount() {
                return 5;
              }

              public String getColumnName(int c) {
                return columns[c];
              }

              public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : c == 1 ? Long.class : String.class;
              }

              public boolean isCellEditable(int r, int c) {
                return c == 0;
              }

              public Object getValueAt(int r, int c) {
                TestCase t = cases.get(r);
                return switch (c) {
                  case 0 -> selected.contains(t.id());
                  case 1 -> t.id();
                  case 2 -> t.title();
                  case 3 -> t.method();
                  default -> t.active() ? "Activo" : "Inactivo";
                };
              }

              public void setValueAt(Object value, int r, int c) {
                if (Boolean.TRUE.equals(value)) selected.add(cases.get(r).id());
                else selected.remove(cases.get(r).id());
                fireTableCellUpdated(r, c);
              }
            });
    table.setRowHeight(28);
    table.setAutoCreateRowSorter(true);
    table.setPreferredScrollableViewportSize(new Dimension(600, 260));
    JTextField filter = new JTextField();
    javax.swing.table.TableRowSorter<?> sorter =
        (javax.swing.table.TableRowSorter<?>) table.getRowSorter();
    filter
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              private void changed() {
                sorter.setRowFilter(
                    javax.swing.RowFilter.regexFilter(
                        "(?i)" + java.util.regex.Pattern.quote(filter.getText()), 2));
              }

              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
              }

              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
              }

              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
              }
            });
    field("Nombre *", name);
    areaField("Descripción", description);
    field("Buscar casos disponibles por título", filter);
    field(
        "Marque para agregar / desmarque para quitar · Inactivos se omiten al ejecutar",
        new JScrollPane(table));
    if (cases.isEmpty())
      field(
          "Sin casos disponibles", new JLabel("Cree casos de prueba antes de agregarlos al plan."));
    dates(p.createdAt(), p.updatedAt());
    submit(
        () -> {
          if (table.isEditing()) table.getCellEditor().stopCellEditing();
          TestPlan value =
              new TestPlan(
                  p.id(),
                  name.getText().strip(),
                  description.getText(),
                  new ArrayList<>(selected),
                  p.createdAt(),
                  p.updatedAt());
          Validation.plan(value);
          return value;
        },
        onSave);
  }
}
