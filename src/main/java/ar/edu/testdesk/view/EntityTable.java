package ar.edu.testdesk.view;

import java.awt.*;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.table.*;

public final class EntityTable<T> extends JPanel {
  private List<T> rows = List.of();
  private final JTable table;
  private final AbstractTableModel model;
  private final JLabel count = new JLabel("Sin datos. Use Actualizar o cree un registro.");

  public EntityTable(String[] columns, Function<T, Object[]> cells) {
    super(new BorderLayout(0, 8));
    model =
        new AbstractTableModel() {
          public int getRowCount() {
            return rows.size();
          }

          public int getColumnCount() {
            return columns.length;
          }

          public String getColumnName(int c) {
            return columns[c];
          }

          public Object getValueAt(int r, int c) {
            return cells.apply(rows.get(r))[c];
          }

          public boolean isCellEditable(int r, int c) {
            return false;
          }

          public Class<?> getColumnClass(int c) {
            for (T row : rows) {
              Object value = cells.apply(row)[c];
              if (value != null) return value.getClass();
            }
            return Object.class;
          }
        };
    table = new JTable(model);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setRowHeight(30);
    table.getTableHeader().setReorderingAllowed(false);
    table.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable t, Object v, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, selected, focus, row, col);
            setToolTipText(null); // No interpretar contenido del usuario como HTML Swing.
            putClientProperty("html", null);
            if (!selected)
              setForeground(
                  switch (String.valueOf(v)) {
                    case "PASS" -> new Color(106, 205, 150);
                    case "FAIL" -> new Color(255, 186, 100);
                    case "ERROR" -> new Color(255, 120, 140);
                    default -> t.getForeground();
                  });
            return this;
          }
        });
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(count, BorderLayout.SOUTH);
  }

  public void setRows(List<T> values) {
    rows = List.copyOf(values);
    model.fireTableStructureChanged();
    count.setText(
        rows.isEmpty() ? "Sin resultados para esta búsqueda." : rows.size() + " registro(s)");
  }

  public T selected() {
    int row = table.getSelectedRow();
    if (row < 0) throw new IllegalArgumentException("Seleccione una fila primero.");
    return rows.get(table.convertRowIndexToModel(row));
  }
}
