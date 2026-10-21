package ar.edu.testdesk.view;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EditDialog<T> extends JDialog {
  protected final JPanel fields = new JPanel(new GridBagLayout());
  public final JButton save = new JButton("Guardar"), cancel = new JButton("Cancelar");
  private int row;

  public EditDialog(Window owner, String title) {
    super(owner, title, ModalityType.APPLICATION_MODAL);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    JPanel root = new JPanel(new BorderLayout(8, 8));
    root.setBorder(new EmptyBorder(12, 12, 12, 12));
    fields.setBorder(new EmptyBorder(4, 4, 12, 4));
    root.add(new JScrollPane(fields), BorderLayout.CENTER);
    root.add(MainFrame.bar(save, cancel), BorderLayout.SOUTH);
    setContentPane(root);
    cancel.addActionListener(e -> dispose());
    getRootPane().setDefaultButton(save);
    getRootPane()
        .registerKeyboardAction(
            e -> {
              if (cancel.isEnabled()) dispose();
            },
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  protected void field(String label, JComponent component) {
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = row++;
    g.weightx = 1;
    g.fill = GridBagConstraints.HORIZONTAL;
    g.anchor = GridBagConstraints.NORTHWEST;
    g.insets = new Insets(7, 4, 3, 4);
    fields.add(new JLabel(label), g);
    g.gridy = row++;
    g.insets = new Insets(0, 4, 4, 4);
    fields.add(component, g);
  }

  protected JTextArea area(String text, int rows) {
    JTextArea a = new JTextArea(text, rows, 45);
    a.setLineWrap(true);
    a.setWrapStyleWord(true);
    return a;
  }

  protected void areaField(String label, JTextArea area) {
    field(label, new JScrollPane(area));
  }

  protected void dates(Object created, Object updated) {
    field(
        "Fechas (UTC)",
        new JLabel(
            "Creación: "
                + (created == null ? "al guardar" : created)
                + " · Modificación: "
                + (updated == null ? "al guardar" : updated)));
  }

  protected void submit(Supplier<T> reader, Consumer<T> handler) {
    save.addActionListener(
        e -> {
          try {
            handler.accept(reader.get());
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Revise los datos", JOptionPane.WARNING_MESSAGE);
          }
        });
  }

  public void open() {
    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(Math.min(780, s.width - 80), Math.min(780, s.height - 100));
    setLocationRelativeTo(getOwner());
    setVisible(true);
  }
}
