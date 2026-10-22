# TestDesk

TestDesk es una aplicación de escritorio para organizar y ejecutar pruebas HTTP. Permite gestionar casos de prueba, agruparlos en planes, registrar bugs y consultar los resultados de cada ejecución.

## Funcionalidades

- Crear, editar, buscar y eliminar casos de prueba, planes y bugs.
- Ejecutar solicitudes GET, POST, PUT y DELETE, comparando el código HTTP y el texto esperado.
- Consultar un tablero de resumen y el historial de ejecuciones.
- Exportar reportes HTML y practicar con una API de demostración local.

## Tecnologías y requisitos

Desarrollado con **Java 21, Swing, FlatLaf, JDBC y MySQL 8.4**. Para ejecutarlo necesitás **JDK 21**, **Maven 3.9**, **Git** y **Docker Compose** (incluido en Docker Desktop). Docker se utiliza únicamente para MySQL; la aplicación corre en tu computadora.

## Instalación

```sh
git clone https://github.com/NicolasMRuiz/testdesk.git
cd testdesk
```

Copiá `.env.example` a `.env`:

```powershell
# Windows (PowerShell)
Copy-Item .env.example .env
```

En Linux o macOS, usá `cp .env.example .env`. Con Docker iniciado, ejecutá desde la raíz del proyecto:

```sh
docker compose up -d --wait
mvn clean package
java -jar target/testdesk-1.0.0.jar
```

La primera instalación requiere Internet para descargar las dependencias. Los valores de `.env.example` son para desarrollo local. La aplicación lee `.env` desde el directorio de trabajo; no compartas ese archivo si contiene credenciales propias.

## API de demostración

En **Inicio**, seleccioná **Iniciar API demo** y **Crear casos de ejemplo**. La API funciona en `http://127.0.0.1:8089`, sin conexión a Internet. Podés ejecutar los casos individualmente o agregarlos a un plan.

Los seis ejemplos permiten obtener **4 PASS, 1 FAIL y 1 ERROR** con la API activa. Volver a crearlos no agrega duplicados. Para cerrar la API, usá **Detener API demo**.

Encontrá los detalles de uso, configuración y la alternativa con MySQL sin Docker en la [guía de uso](docs/USO.md).
