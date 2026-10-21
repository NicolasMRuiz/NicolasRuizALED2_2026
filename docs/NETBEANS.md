# Pendiente: formularios GUI Builder

No se encontró una instalación de NetBeans disponible en el entorno de implementación. Se compiló con Maven y se construyeron vistas Swing funcionales con `BorderLayout`, `GridBagLayout`, `BoxLayout` y `FlowLayout`. No se fabricaron `.form` ni se afirma que estas vistas abran en modo Design.

**No alcanza con abrir las clases existentes en NetBeans:** hay que construir los formularios visuales y conectar sus componentes a los controladores. Este paso académico sigue pendiente.

1. Abrir la carpeta como proyecto Maven en NetBeans, configurar plataforma JDK 21 y compilar.
2. Crear un `JFrame Form` para la ventana principal, con panel de título, `JTabbedPane`, cinco páginas y barra de estado. Usar el layout libre/GroupLayout del diseñador, con componentes redimensionables.
3. Crear `JDialog Form` para casos, planes, bugs y resultados. Ubicar los campos que ya presentan `CaseDialog`, `PlanDialog`, `BugDialog` y `ResultsDialog` en paneles con scroll; conservar Guardar/Cancelar fuera del scroll.
4. Mantener los modelos de tabla y la lógica de eventos fuera de `initComponents()`. Los componentes diseñados deben exponerse a través de métodos de la vista o conservar el contrato que consumen `CatalogController`, `ExecutionController` y `AppController`. Reutilizar `TaskRunner`, validaciones y servicios sin trasladar SQL a la vista.
5. Reemplazar las vistas manuales por las diseñadas y ajustar los constructores de los controladores. No conservar dos interfaces desconectadas para aparentar cumplimiento.
6. Abrir cada pareja `.java`/`.form` en **Design**, mover un componente, guardar, cerrar y reabrir; compilar con `mvn clean package` y comprobar navegación/guardado/ejecución. Solo entonces puede darse el requisito por cumplido.

No edite a mano los bloques protegidos generados por NetBeans. Versione ambos archivos de cada formulario, excluyendo `nbproject/private`.
