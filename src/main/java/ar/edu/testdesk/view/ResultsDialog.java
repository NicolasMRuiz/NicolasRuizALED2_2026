package ar.edu.testdesk.view;

import ar.edu.testdesk.model.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class ResultsDialog extends JDialog {
  public final JButton bug = new JButton("Crear bug desde FAIL / ERROR"),
      detail = new JButton("Ver detalle");
  public final EntityTable<TestResult> table =
      new EntityTable<>(
          new String[] {"ID", "Caso", "Estado", "Esperado", "Obtenido", "ms", "Observación"},
          r ->
              new Object[] {
                r.id(),
                r.caseTitle(),
                r.status(),
                r.expectedStatus(),
                r.actualStatus(),
                r.durationMs(),
                r.message()
              });

  public ResultsDialog(Window owner, TestRun run, List<TestResult> results) {
    super(owner, "Corrida #" + run.id(), ModalityType.APPLICATION_MODAL);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    JPanel root = new JPanel(new BorderLayout(8, 8));
    root.setBorder(new EmptyBorder(12, 12, 12, 12));
    setContentPane(root);
    JTextArea summary =
        new JTextArea(
            run.origin()
                + "\nPASS: "
                + run.passed()
                + " · FAIL: "
                + run.failed()
                + " · ERROR: "
                + run.errors()
                + " · Inactivos omitidos: "
                + run.skipped()
                + " · "
                + run.status());
    summary.setEditable(false);
    root.add(summary, BorderLayout.NORTH);
    root.add(table, BorderLayout.CENTER);
    root.add(MainFrame.bar(detail, bug), BorderLayout.SOUTH);
    table.setRows(results);
    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(Math.min(1100, s.width - 80), Math.min(650, s.height - 100));
    setLocationRelativeTo(owner);
    detail.addActionListener(
        e -> {
          try {
            showDetail(table.selected());
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
          }
        });
  }

  private void showDetail(TestResult r) {
    JTextArea text =
        new JTextArea(
            "Caso: "
                + r.caseTitle()
                + "\n"
                + r.method()
                + " "
                + r.url()
                + "\nFecha (UTC): "
                + r.startedAt()
                + "\nEstado: "
                + r.status()
                + "\nHTTP esperado: "
                + r.expectedStatus()
                + "\nHTTP obtenido: "
                + r.actualStatus()
                + "\nTexto esperado: "
                + r.expectedText()
                + "\nDuración: "
                + r.durationMs()
                + " ms\n\n"
                + r.message()
                + "\n\nExtracto de respuesta:\n"
                + r.responseExcerpt(),
            22,
            65);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setCaretPosition(0);
    JOptionPane.showMessageDialog(
        this, new JScrollPane(text), "Resultado #" + r.id(), JOptionPane.INFORMATION_MESSAGE);
  }
}
