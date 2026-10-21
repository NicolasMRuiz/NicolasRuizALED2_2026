package ar.edu.testdesk;

import ar.edu.testdesk.config.AppConfig;
import ar.edu.testdesk.controller.AppController;
import ar.edu.testdesk.dao.*;
import ar.edu.testdesk.database.Database;
import ar.edu.testdesk.http.*;
import ar.edu.testdesk.report.HtmlReport;
import ar.edu.testdesk.service.*;
import ar.edu.testdesk.view.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public final class App {
  private App() {}

  public static void main(String[] args) {
    FlatDarkLaf.setup();
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("TextComponent.arc", 8);
    SwingUtilities.invokeLater(
        () -> {
          try {
            Database db = new Database(AppConfig.load());
            CaseDao cases = new CaseDao(db);
            PlanDao plans = new PlanDao(db);
            BugDao bugs = new BugDao(db);
            RunDao runs = new RunDao(db);
            CatalogService catalog = new CatalogService(cases, plans, bugs);
            HistoryService history = new HistoryService(runs, new HtmlReport());
            HttpExecutor http = new HttpExecutor();
            DemoServer demo = new DemoServer();
            new AppController(
                    new MainFrame(),
                    catalog,
                    history,
                    new ExecutionService(cases, plans, runs, http),
                    demo,
                    http)
                .start();
          } catch (Exception e) {
            System.err.println("Inicio: " + e.getClass().getSimpleName());
            JOptionPane.showMessageDialog(
                null,
                "No se pudo iniciar TestDesk. Revise el formato y los permisos del archivo .env.\n"
                    + e.getClass().getSimpleName(),
                "TestDesk",
                JOptionPane.ERROR_MESSAGE);
          }
        });
  }
}
