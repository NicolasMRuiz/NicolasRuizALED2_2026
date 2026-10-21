# TestDesk

Aplicación de escritorio para el trabajo práctico de Java: casos HTTP, planes, bugs, historial y reportes HTML. Java 21, Swing con FlatLaf Dark, JDBC y MySQL 8.4. Sin login ni servicios externos.

**Pendiente académico:** las vistas actuales están escritas con layouts Swing. No se generaron archivos `.form` ni se verificó edición en NetBeans Design. Esto NO cumple todavía el requisito específico de construir los formularios con GUI Builder. Ver [pasos pendientes](NETBEANS.md). El proyecto Maven y la aplicación funcionan independientemente de ese paso.

## Arranque

Requisitos: JDK 21, Maven 3.9 y Docker Desktop iniciado (contenedores Linux). Desde la raíz del proyecto:

```powershell
Copy-Item .env.example .env
docker compose up -d --wait
mvn clean package
java -jar target/testdesk-1.0.0.jar
```

En Linux/macOS, el primer comando es `cp .env.example .env`. No reemplace un `.env` que ya configuró. La primera compilación y descarga de MySQL necesitan Internet; la aplicación y demo luego pueden funcionar sin Internet.

En este workspace también puede ejecutar `./scripts/build.ps1` y `./scripts/run.ps1` desde PowerShell: detectan las herramientas portables ya descargadas en `.tools`. En otra máquina usan Maven/JDK instalados. Los scripts no descargan ni instalan herramientas.

Java carga explícitamente `.env` **desde el directorio de trabajo** mediante `AppConfig`; las variables del proceso tienen prioridad. En NetBeans, establezca la raíz del proyecto como directorio de trabajo y seleccione JDK 21. Las contraseñas de `.env.example` son únicamente de desarrollo local. No coloque credenciales reales en el repositorio.

MySQL escucha en `127.0.0.1:3307`, base `testdesk`, usuario separado `testdesk_app`. La UI corre en el anfitrión. `database/init.sql` se ejecuta automáticamente solo al crear un volumen vacío. `docker compose stop` detiene MySQL sin borrar datos; `docker compose start` lo vuelve a iniciar. No use `down -v` si quiere conservarlos. Cambiar la contraseña en `.env` no cambia la de un volumen ya inicializado.

Si 3307 está ocupado, cambie `DB_PORT` en `.env` y ejecute `docker compose up -d`. Si MySQL no está disponible, la ventana muestra el error y permite reintentar con **Actualizar**. Consulte `docker compose ps` y `docker compose logs mysql` para diagnosticar el contenedor.

### MySQL instalado manualmente

Con MySQL 8.x, entre con un administrador mediante `mysql -u root -p` y ejecute:

```sql
SOURCE /ruta/al/proyecto/database/init.sql;
CREATE USER IF NOT EXISTS 'testdesk_app'@'localhost' IDENTIFIED BY 'testdesk_local_dev';
GRANT SELECT, INSERT, UPDATE, DELETE ON testdesk.* TO 'testdesk_app'@'localhost';
```

Reemplace la ruta por la real, usando `/` incluso en Windows. Ajuste `DB_PORT=3306` y las credenciales locales en `.env`. La app no requiere root ni crea/reinicia tablas al arrancar.

## Uso

- **Casos:** Nuevo, Ver/editar, Eliminar y Ejecutar. Buscar/actualizar aplica título, método y estado; las columnas pueden ordenarse. GET ignora el body. Headers: `Nombre: valor`, uno por línea. No se permiten duplicados ni headers reservados por HttpClient como `Host` y `Content-Length`.
- **Planes:** marque/desmarque los casos del selector para agregarlos/quitarlos. Puede guardar planes vacíos, pero no ejecutarlos. Se ejecutan por ID ascendente, uno a la vez; los inactivos se omiten. Un plan sin casos activos no se ejecuta.
- **Bugs:** vínculos opcionales mediante selectores. Desde Historial → Ver resultados puede precargar un bug de un FAIL/ERROR, editarlo y confirmar su guardado.
- **Historial:** resultados reales persistidos, detalle y exportación mediante selector de archivo. El HTML es autónomo, con texto escapado y sin recursos externos. Fechas en UTC para evitar diferencias entre MySQL y distintas máquinas.
- **Inicio:** totales actuales y resumen de la última corrida. Iniciar/detener API demo y crear ejemplos sin duplicarlos. El puerto demo es `127.0.0.1:8089`; si está ocupado, cierre el proceso que lo usa. No se enlaza a interfaces públicas.

La demo incluye `/ok` (200), `/missing` (404), `/error` (500), `/echo` (POST) y `/slow` (15 s). Los seis ejemplos producen, con la demo activa, **4 PASS, 1 FAIL y 1 ERROR**. Para ejecutarlos juntos, cree un plan y márquelos. Una segunda carga de ejemplos no modifica los existentes; un ejemplo borrado puede volver a crearse.

PASS requiere código y fragmento esperado coincidentes; un 404/500 esperado puede pasar. FAIL tiene respuesta pero no cumple alguna aserción. ERROR indica fallo técnico, timeout o respuesta mayor al límite. No se siguen redirecciones automáticamente: se evalúa el código recibido. Conexión: 5 s; solicitud completa: 12 s; respuesta máxima: 1 MiB; extracto: 4000 caracteres.

Las tareas JDBC, HTTP y de exportación se ejecutan en `SwingWorker`. Durante una operación se bloquean las acciones y el cierre; espere a que finalice. La exclusión es por instancia: use una ventana de TestDesk por base durante la demostración. Si el proceso termina abruptamente, la corrida puede quedar `RUNNING`; los resultados ya guardados siguen consultables. No se reintenta automáticamente una petición, para evitar repetir POST/PUT/DELETE.

Los casos guardan su configuración original en MySQL para poder ejecutarla. Los resultados no guardan headers ni body enviados: conservan título, método, URL sin query, aserciones, estado y extracto sanitizado. Se ocultan valores de headers, parámetros y campos reconocidos como contraseña/token/secreto. Evite usar datos reales sensibles en este trabajo; la detección no interpreta todos los posibles formatos de respuesta. Los mensajes JDBC solo registran SQLState/código, sin volcar valores.

## Recorrido del código

`view → controller → service → dao → database`

| Paquete/archivo | Responsabilidad |
| --- | --- |
| `model` | Records inmutables de casos, planes, bugs, corridas y resultados |
| `view` | Ventana, tablas y diálogos; sin SQL ni solicitudes HTTP |
| `controller` | Eventos, coordinación y tareas fuera del EDT |
| `service` | Validaciones, reglas, ejecución secuencial e historial |
| `dao` | PreparedStatement, mapeo de filas y transacciones |
| `database`, `config` | Conexiones JDBC y carga explícita de `.env` |
| `http` | HttpClient, límite de respuesta, sanitizado y servidor demo |
| `report` | HTML local con escape de contenido |
| `database/init.sql` | PK/FK, índices y reglas de borrado |

El plan y sus relaciones se guardan en una transacción. La relación muchos-a-muchos tiene PK compuesta. El borrado de casos es físico: sus asociaciones con planes se eliminan en cascada; resultados y bugs quedan con `case_id=NULL`. Borrar un plan deja `plan_id=NULL` en sus corridas. Cada resultado y los contadores de su corrida se guardan en una misma transacción. Las copias históricas permiten consultar ejecuciones aun sin sus entidades originales.

No se incluyeron JUnit, clases de tests ni suites automatizadas: las pruebas de aceptación manuales quedan a cargo del alumno. Consulte [VERIFICACION.md](VERIFICACION.md) para las comprobaciones técnicas efectivamente realizadas.

## Repositorio

`target/`, `.tools/`, `.env`, logs y `reports/` están ignorados. `.tools/` contiene únicamente herramientas portables descargadas durante esta implementación; no es necesario distribuirlas. El proyecto no contiene rutas absolutas de esta máquina. El repositorio público distribuye únicamente fuentes, configuración de ejemplo y documentación.

Dependencias declaradas: [FlatLaf](https://www.formdev.com/flatlaf/) 3.6 y [MySQL Connector/J](https://dev.mysql.com/doc/relnotes/connector-j/en/news-9-2-0.html) 9.2.0, versiones estables. Maven genera `target/testdesk-1.0.0.jar` con sus dependencias; el archivo `original-...jar` no es el ejecutable distribuible.
