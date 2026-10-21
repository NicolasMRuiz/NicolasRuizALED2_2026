# TestDesk

Aplicación de escritorio para gestionar casos de prueba HTTP, planes, bugs, historial y reportes HTML. **Java 21, Swing, FlatLaf Dark, JDBC y MySQL 8.4**, con arquitectura `view → controller → service → dao → database`.

## Instalar y ejecutar

Instale **JDK 21**, **Maven 3.9** y **Docker Desktop** (contenedores Linux). Compruebe `java -version`, `mvn -version` y que Docker esté iniciado.

```sh
git clone https://github.com/NicolasMRuiz/testdesk.git
cd testdesk
```

Copie `.env.example` a `.env`: en PowerShell use `Copy-Item .env.example .env`; en Linux/macOS, `cp .env.example .env`. Luego:

```sh
docker compose up -d --wait
mvn clean package
java -jar target/testdesk-1.0.0.jar
```

La primera descarga de dependencias e imagen MySQL requiere Internet. La aplicación y la demo local luego funcionan sin APIs externas. En Windows también puede usar `./scripts/build.ps1` y `./scripts/run.ps1`; requieren las herramientas instaladas y no descargan software.

La UI corre en el anfitrión; Docker contiene únicamente MySQL. Java carga `.env` explícitamente desde la raíz del proyecto; las variables de entorno tienen prioridad. Los valores incluidos son **ejemplos de desarrollo local**, no secretos de producción. No publique su `.env`.

## Base de datos

MySQL escucha en `127.0.0.1:3307`, base `testdesk`, con usuario de aplicación separado de root. `database/init.sql` crea las tablas y relaciones al inicializar un volumen vacío; la app no recrea tablas al arrancar.

- Detener conservando los datos: `docker compose stop`.
- Volver a iniciar: `docker compose start`.
- Diagnosticar: `docker compose ps` y `docker compose logs mysql`.
- Puerto ocupado: cambie `DB_PORT` en `.env` y ejecute `docker compose up -d`.

No use `docker compose down -v` si desea conservar los datos. Cambiar contraseñas en `.env` no modifica las de un volumen existente. Para **MySQL instalado sin Docker**, consulte las instrucciones SQL de [USO.md](docs/USO.md#mysql-instalado-manualmente).

## Funcionalidades y demo

- CRUD de casos, planes y bugs, con validaciones, búsqueda y filtros.
- Relación muchos-a-muchos entre planes y casos; ejecución secuencial de casos activos.
- HTTP GET, POST, PUT y DELETE; evaluación de código y texto esperado como PASS, FAIL o ERROR.
- Dashboard, historial persistente y reportes HTML locales. Borrar casos o planes conserva el historial.

En **Inicio**, pulse **Iniciar API demo** y **Crear casos de ejemplo**. La demo escucha exclusivamente en `127.0.0.1:8089`; no duplica los ejemplos existentes. Sus seis casos permiten obtener **4 PASS, 1 FAIL y 1 ERROR**. Puede agruparlos en un plan desde Planes. Un HTTP 404 o 500 esperado puede ser PASS.

Más detalles sobre headers, límites, ejecución y estructura: [guía de uso](docs/USO.md). Comprobaciones realizadas: [VERIFICACION.md](docs/VERIFICACION.md). No se incluyen JUnit ni suites de pruebas automatizadas.

## NetBeans

Abra la raíz como proyecto Maven y seleccione JDK 21. **Esta versión usa vistas Swing escritas manualmente y no contiene formularios `.form` editables en GUI Builder.** Ese requisito académico sigue pendiente, tal como se documentó al entregar la implementación. Consulte [NETBEANS.md](docs/NETBEANS.md); no se atribuye al proyecto una verificación en modo Design que no se realizó.

## Archivos publicados

Se incluyen fuentes, `pom.xml`, scripts de arranque, `compose.yaml`, `database/init.sql`, configuración de ejemplo y documentación. `.env`, datos locales, reportes, herramientas portables, temporales y `target/` están excluidos. El JAR ejecutable se genera al compilar: `target/testdesk-1.0.0.jar`.
