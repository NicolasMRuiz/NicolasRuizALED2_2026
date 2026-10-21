# Verificación de la implementación

Fecha: 21 de octubre de 2026. Estas son comprobaciones técnicas puntuales; no constituyen una suite de aceptación ni reemplazan las pruebas manuales del alumno.

## Ejecutado realmente

- Maven 3.9.9: `clean package`, primero con el JDK instalado y luego con Temurin **21.0.12.1** portable. Compilación final de 34 fuentes con `release 21`: **BUILD SUCCESS**. JAR con dependencias: `target/testdesk-1.0.0.jar`. No hay clases de tests ni dependencia JUnit.
- `docker compose config --quiet`: configuración válida. `docker compose up -d --wait`: descargó MySQL 8.4, creó el volumen específico `testdesk_testdesk_data`, inicializó el esquema y dejó el servicio **Healthy**. No se usaron bases de otros proyectos.
- Se invocaron servicios reales mediante JShell, conectados a MySQL. Primera carga de ejemplos: **6**; segunda: **0**. Se guardó el plan **Demostración local** con los seis casos.
- Se inició `DemoServer` en loopback y se ejecutó ese plan con `ExecutionService`. Corrida #1: **6 resultados, 4 PASS, 1 FAIL, 1 ERROR**, estado `COMPLETED`. Los HTTP 200, 404, 500 y POST esperado pasaron; el caso que esperaba 201 falló; `/slow` produjo ERROR por timeout. Se cerraron servidor demo y cliente HTTP de esa comprobación.
- Exportación real a `reports/demo.html`, generado a partir de la corrida persistida. El reporte y los datos de ejemplo quedan locales, ignorados por Git.
- Tras cerrar el proceso de comprobación, otra JVM consultó MySQL y recuperó **1 corrida y 6 resultados** con los mismos contadores. La ventana Swing también mostró esos datos. No dependen de colecciones en memoria.
- Se abrió el JAR con Java 21 y se inspeccionaron visualmente el tablero oscuro, la JTable de casos, la navegación a Casos y el formulario Nuevo. Se canceló el formulario sin guardar y se cerró la app normalmente para recompilar.
- Comprobación puntual de sanitizado con credenciales ficticias, ocultamiento de query y escape HTML; revisión de exclusiones de `.env`, `.tools`, `target` y `reports`.
- La compilación emite el aviso habitual de manifiestos superpuestos de Shade; se usa el transformador de manifiesto para seleccionar `ar.edu.testdesk.App`. No hubo errores de compilación.

## No se afirma haber verificado

- No se recorrieron manualmente todas las combinaciones de CRUD, filtros, validaciones, relaciones y fallos de conexión de la interfaz. La aceptación manual queda pendiente para el alumno.
- No se verificaron otros sistemas operativos, escalas HiDPI ni todos los tamaños de pantalla.
- No se abrió el reporte en un navegador durante esta comprobación; se generó y revisó su contenido HTML.
- **GUI Builder de NetBeans está pendiente de implementación y verificación**, no solo de abrir el proyecto. Las vistas actuales son Swing manual, sin `.form`. El detalle exacto está en [NETBEANS.md](NETBEANS.md).
- Durante la implementación inicial no se creó un repositorio remoto ni se hizo push. La publicación posterior fue solicitada por el usuario, sin cambios funcionales.

## Estado local entregado

MySQL conserva seis ejemplos, un plan y una corrida con seis resultados. `.env` contiene solo las credenciales locales de `.env.example`. El JDK y Maven portables están en `.tools` (ignorado); puede usar `scripts/build.ps1` y `scripts/run.ps1` en este workspace. No se añadieron herramientas como dependencias de la aplicación.
