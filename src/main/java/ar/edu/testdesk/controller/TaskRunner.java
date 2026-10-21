package ar.edu.testdesk.controller;

import ar.edu.testdesk.view.MainFrame;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.*;

public final class TaskRunner {
  private final MainFrame frame;
  private boolean busy;

  public TaskRunner(MainFrame frame) {
    this.frame = frame;
  }

  public boolean isBusy() {
    return busy;
  }

  public <T> void run(Window owner, String message, Callable<T> work, Consumer<T> success) {
    if (busy) {
      info(owner, "Espere a que termine la operación en curso.");
      return;
    }
    busy = true;
    frame.status.setText(message);
    List<Component> disabled = new ArrayList<>();
    disable(frame, disabled);
    if (owner != frame) disable(owner, disabled);
    int close = owner instanceof JDialog d ? d.getDefaultCloseOperation() : 0;
    if (owner instanceof JDialog d) d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    new SwingWorker<T, Void>() {
      protected T doInBackground() throws Exception {
        return work.call();
      }

      protected void done() {
        busy = false;
        disabled.forEach(c -> c.setEnabled(true));
        if (owner instanceof JDialog d) d.setDefaultCloseOperation(close);
        frame.status.setText("Listo");
        try {
          success.accept(get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          error(owner, e);
        } catch (ExecutionException e) {
          error(owner, e.getCause());
        } catch (RuntimeException e) {
          error(owner, e);
        }
      }
    }.execute();
  }

  private void disable(Component c, List<Component> disabled) {
    if (c instanceof AbstractButton
        || c instanceof JTextField
        || c instanceof JComboBox<?>
        || c instanceof JTable
        || c instanceof JTabbedPane) {
      if (c.isEnabled()) {
        c.setEnabled(false);
        disabled.add(c);
      }
    }
    if (c instanceof Container container)
      for (Component child : container.getComponents()) disable(child, disabled);
  }

  public void progress(String text) {
    SwingUtilities.invokeLater(() -> frame.status.setText(text));
  }

  public void action(Window owner, Runnable action) {
    if (busy) return;
    try {
      action.run();
    } catch (RuntimeException e) {
      error(owner, e);
    }
  }

  public void error(Component owner, Throwable error) {
    System.err.println("Operación: " + error.getClass().getSimpleName());
    String message =
        error instanceof IllegalArgumentException
                || error instanceof IllegalStateException
                || error instanceof ar.edu.testdesk.database.DataAccessException
            ? error.getMessage()
            : "No se pudo completar la operación ("
                + error.getClass().getSimpleName()
                + "). Revise permisos y configuración.";
    frame.status.setText("Operación no completada");
    JOptionPane.showMessageDialog(owner, message, "TestDesk · Atención", JOptionPane.ERROR_MESSAGE);
  }

  public static void info(Component owner, String message) {
    JOptionPane.showMessageDialog(owner, message, "TestDesk", JOptionPane.INFORMATION_MESSAGE);
  }

  public static boolean confirm(Component owner, String message) {
    return JOptionPane.showConfirmDialog(
            owner, message, "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
        == JOptionPane.YES_OPTION;
  }
}
