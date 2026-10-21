package ar.edu.testdesk.view;

import ar.edu.testdesk.model.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class MainFrame extends JFrame {
  public final JTabbedPane tabs = new JTabbedPane();
  public final JLabel status = new JLabel("Conectando con MySQL…");
  public final JTextArea dashboard = new JTextArea();
  public final JLabel demoStatus = new JLabel("API demo detenida · 127.0.0.1:8089");
  public final JButton refreshHome = new JButton("Actualizar"),
      startDemo = new JButton("Iniciar API demo"),
      stopDemo = new JButton("Detener API demo"),
      seed = new JButton("Crear casos de ejemplo");
  public final JTextField caseSearch = new JTextField(22),
      planSearch = new JTextField(22),
      bugSearch = new JTextField(22);
  public final JComboBox<String> method =
      new JComboBox<>(new String[] {"Todos los métodos", "GET", "POST", "PUT", "DELETE"});
  public final JComboBox<String> active =
      new JComboBox<>(new String[] {"Todos los estados", "Activo", "Inactivo"});
  public final JComboBox<String> bugStatus =
      new JComboBox<>(new String[] {"Todos los estados", "Abierto", "En progreso", "Resuelto"});
  public final JComboBox<String> priority =
      new JComboBox<>(new String[] {"Todas las prioridades", "Baja", "Media", "Alta", "Crítica"});
  public final JButton findCases = new JButton("Buscar / actualizar"),
      newCase = new JButton("Nuevo"),
      editCase = new JButton("Ver / editar"),
      deleteCase = new JButton("Eliminar"),
      runCase = new JButton("Ejecutar");
  public final JButton findPlans = new JButton("Buscar / actualizar"),
      newPlan = new JButton("Nuevo"),
      editPlan = new JButton("Ver / editar casos"),
      deletePlan = new JButton("Eliminar"),
      runPlan = new JButton("Ejecutar plan");
  public final JButton findBugs = new JButton("Buscar / actualizar"),
      newBug = new JButton("Nuevo"),
      editBug = new JButton("Ver / editar"),
      deleteBug = new JButton("Eliminar");
  public final JButton findRuns = new JButton("Actualizar"),
      detailRun = new JButton("Ver resultados"),
      exportRun = new JButton("Exportar HTML");
  public final EntityTable<TestCase> cases =
      new EntityTable<>(
          new String[] {"ID", "Título", "Método", "HTTP esperado", "Estado", "Modificado (UTC)"},
          t ->
              new Object[] {
                t.id(),
                t.title(),
                t.method(),
                t.expectedStatus(),
                t.active() ? "Activo" : "Inactivo",
                t.updatedAt()
              });
  public final EntityTable<TestPlan> plans =
      new EntityTable<>(
          new String[] {"ID", "Nombre", "Casos", "Modificado (UTC)"},
          p -> new Object[] {p.id(), p.name(), p.caseIds().size(), p.updatedAt()});
  public final EntityTable<Bug> bugs =
      new EntityTable<>(
          new String[] {"ID", "Título", "Prioridad", "Estado", "Caso", "Resultado"},
          b ->
              new Object[] {b.id(), b.title(), b.priority(), b.status(), b.caseId(), b.resultId()});
  public final EntityTable<TestRun> runs =
      new EntityTable<>(
          new String[] {
            "ID",
            "Fecha (UTC)",
            "Origen",
            "Total",
            "PASS",
            "FAIL",
            "ERROR",
            "Omitidos",
            "ms",
            "Estado"
          },
          r ->
              new Object[] {
                r.id(),
                r.startedAt(),
                r.origin(),
                r.total(),
                r.passed(),
                r.failed(),
                r.errors(),
                r.skipped(),
                r.durationMs(),
                r.status()
              });

  public MainFrame() {
    super("TestDesk · Gestión de pruebas");
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    JPanel root = new JPanel(new BorderLayout(12, 12));
    root.setBorder(new EmptyBorder(18, 18, 12, 18));
    JLabel title = new JLabel("TestDesk  /  Laboratorio de pruebas");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
    root.add(title, BorderLayout.NORTH);
    root.add(tabs, BorderLayout.CENTER);
    root.add(status, BorderLayout.SOUTH);
    setContentPane(root);
    dashboard.setEditable(false);
    dashboard.setLineWrap(true);
    dashboard.setWrapStyleWord(true);
    dashboard.setFont(dashboard.getFont().deriveFont(18f));
    JPanel home = new JPanel(new BorderLayout(12, 12));
    home.setBorder(new EmptyBorder(18, 18, 18, 18));
    home.add(new JScrollPane(dashboard), BorderLayout.CENTER);
    JPanel demo = new JPanel(new BorderLayout());
    demo.add(demoStatus, BorderLayout.NORTH);
    demo.add(bar(refreshHome, startDemo, stopDemo, seed), BorderLayout.CENTER);
    home.add(demo, BorderLayout.SOUTH);
    tabs.addTab("Inicio", home);
    tabs.addTab(
        "Casos de prueba",
        section(
            cases,
            bar(new JLabel("Título:"), caseSearch, method, active),
            bar(findCases, newCase, editCase, deleteCase, runCase)));
    tabs.addTab(
        "Planes",
        section(
            plans,
            bar(new JLabel("Nombre:"), planSearch),
            bar(findPlans, newPlan, editPlan, deletePlan, runPlan)));
    tabs.addTab(
        "Bugs",
        section(
            bugs,
            bar(new JLabel("Título:"), bugSearch, bugStatus, priority),
            bar(findBugs, newBug, editBug, deleteBug)));
    tabs.addTab(
        "Historial / Reportes",
        section(
            runs,
            bar(new JLabel("Cada corrida conserva su configuración y resultados. Fechas en UTC.")),
            bar(findRuns, detailRun, exportRun)));
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(Math.min(1180, screen.width - 60), Math.min(780, screen.height - 80));
    setLocationRelativeTo(null);
  }

  public static JPanel bar(Component... components) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    for (Component c : components) p.add(c);
    return p;
  }

  private JPanel section(JComponent table, JPanel filters, JPanel actions) {
    JPanel p = new JPanel(new BorderLayout(8, 8));
    p.setBorder(new EmptyBorder(12, 12, 12, 12));
    JPanel north = new JPanel();
    north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
    north.add(filters);
    north.add(actions);
    p.add(
        new JScrollPane(
            north,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
        BorderLayout.NORTH);
    p.add(table, BorderLayout.CENTER);
    return p;
  }

  public void showDashboard(Dashboard d) {
    TestRun r = d.lastRun();
    dashboard.setText(
        "RESUMEN DEL PROYECTO\n\n"
            + d.cases()
            + " casos de prueba   ·   "
            + d.activeCases()
            + " activos\n"
            + d.plans()
            + " planes   ·   "
            + d.openBugs()
            + " bugs pendientes\n\nÚLTIMA EJECUCIÓN\n\n"
            + (r == null
                ? "Todavía no hay ejecuciones. Cree un caso o use los ejemplos locales."
                : r.origin()
                    + "\n"
                    + r.startedAt()
                    + " UTC · "
                    + r.status()
                    + "\nPASS: "
                    + r.passed()
                    + "   FAIL: "
                    + r.failed()
                    + "   ERROR: "
                    + r.errors()
                    + "\nOmitidos inactivos: "
                    + r.skipped()
                    + "   ·   Duración: "
                    + r.durationMs()
                    + " ms")
            + "\n\n"
            + "DEMOSTRACIÓN LOCAL\n\n"
            + "1. Inicie la API demo.\n"
            + "2. Cree los casos de ejemplo (sin duplicarlos).\n"
            + "3. Ejecute casos o agrúpelos en un plan.\n\n"
            + "/ok → 200   /missing → 404   /error → 500   /echo → POST\n"
            + "/slow demora 15 s y permite demostrar ERROR por timeout.\n\n"
            + "Un 404 o 500 esperado puede ser PASS. El caso FAIL espera 201 de /ok.");
    dashboard.setCaretPosition(0);
  }
}
