# Guía de uso

Para instalar y abrir TestDesk, seguí los pasos del [README](../README.md).

## Casos de prueba

En **Casos de prueba**, usá **Nuevo** para definir el título, método, URL y código HTTP esperado. También podés agregar descripción, headers, body y un fragmento de texto que deba aparecer en la respuesta.

- Escribí los headers como `Nombre: valor`, uno por línea, sin duplicados. No agregues `Host` ni `Content-Length`.
- GET no envía body. El fragmento esperado distingue mayúsculas y minúsculas.
- Guardá los cambios antes de ejecutar. Los casos inactivos no pueden ejecutarse.
- Seleccioná una fila para editarla, eliminarla o ejecutarla. **Buscar / actualizar** aplica los filtros de título, método y estado.

## Planes

Creá un plan y marcá los casos que querés incluir. Para quitar uno, desmarcalo y guardá. Al ejecutar el plan, los casos activos se procesan uno por uno; los inactivos se omiten. Un plan vacío o sin casos activos no puede ejecutarse.

## Resultados y bugs

- **PASS:** coinciden el código HTTP y, si se configuró, el fragmento esperado. Un 404 o 500 esperado puede ser PASS.
- **FAIL:** hubo respuesta, pero alguna comparación no coincide.
- **ERROR:** ocurrió un problema técnico, como una conexión fallida o un timeout.

En **Historial / Reportes**, seleccioná una corrida y abrí **Ver resultados** para consultar los detalles. Desde un FAIL o ERROR podés crear un bug con información precargada y editarla antes de guardar. La sección **Bugs** permite administrar los registros y filtrarlos por estado y prioridad.

**Exportar HTML** guarda un reporte que se puede abrir en cualquier navegador sin conexión. Las fechas se muestran en UTC. El historial se conserva al cerrar la aplicación y al eliminar casos o planes.

## Demo local

Iniciá la API desde **Inicio** y creá los ejemplos. Estos usan el puerto `8089`:

| Ruta | Respuesta |
| --- | --- |
| `/ok` | HTTP 200 |
| `/missing` | HTTP 404 |
| `/error` | HTTP 500 |
| `/echo` | Devuelve el body enviado por POST |
| `/slow` | Demora 15 segundos y provoca un timeout |

El ejemplo FAIL espera un 201 de `/ok`, que devuelve 200. Si el puerto está ocupado, cerrá el proceso que lo utiliza antes de iniciar la demo.

## Configuración y base de datos

Ejecutá TestDesk desde la raíz del proyecto para que encuentre `.env`. Las variables de entorno tienen prioridad sobre ese archivo. MySQL usa por defecto `127.0.0.1:3307`, la base `testdesk` y el usuario `testdesk_app`.

- Detener MySQL sin borrar datos: `docker compose stop`.
- Volver a iniciarlo: `docker compose start`.
- Consultar su estado: `docker compose ps`.
- Consultar errores: `docker compose logs mysql`.

Si el puerto `3307` está ocupado, cambiá `DB_PORT` en `.env` y ejecutá `docker compose up -d`. Si la aplicación no logra conectarse, revisá MySQL y la configuración, y volvé a intentar con **Actualizar**.

El esquema se inicializa automáticamente en un volumen vacío. No uses `docker compose down -v` si querés conservar tus datos. Cambiar una contraseña en `.env` no cambia la de un volumen existente.

### MySQL sin Docker

Con MySQL 8.x instalado, entrá como administrador con `mysql -u root -p` y ejecutá:

```sql
SOURCE /ruta/al/proyecto/database/init.sql;
CREATE USER IF NOT EXISTS 'testdesk_app'@'localhost' IDENTIFIED BY 'testdesk_local_dev';
GRANT SELECT, INSERT, UPDATE, DELETE ON testdesk.* TO 'testdesk_app'@'localhost';
```

Reemplazá la ruta por la real, usando `/` también en Windows. Ajustá `DB_PORT` en `.env` al puerto de tu servidor (normalmente `3306`) y configurá las mismas credenciales del usuario creado. Luego compilá y ejecutá la aplicación con los comandos del README, sin los pasos de Docker.

## Durante la ejecución

Usá una sola instancia de TestDesk por base y esperá a que termine la operación antes de cerrar. Las solicitudes tienen un límite de 12 segundos y las respuestas, de 1 MiB. El historial guarda un extracto de hasta 4000 caracteres. No se siguen redirecciones ni se reintentan peticiones automáticamente.

Usá datos de prueba: la configuración original de los casos se guarda en MySQL. Si cerrás el proceso de forma forzada, una corrida puede quedar como `RUNNING`, aunque sus resultados ya guardados se pueden consultar.
