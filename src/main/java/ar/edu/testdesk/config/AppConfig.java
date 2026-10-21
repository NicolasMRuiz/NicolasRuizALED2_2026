package ar.edu.testdesk.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record AppConfig(String host, int port, String database, String user, String password) {
  public static AppConfig load() throws IOException {
    Map<String, String> values = new HashMap<>();
    Path file = Path.of(".env");
    if (Files.exists(file)) {
      for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
        line = line.replace("\uFEFF", "").strip();
        if (line.isEmpty() || line.startsWith("#")) continue;
        int equal = line.indexOf('=');
        if (equal < 1)
          throw new IllegalArgumentException("Línea inválida en .env; use CLAVE=valor.");
        String value = line.substring(equal + 1).strip();
        if (value.length() >= 2
            && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))))
          value = value.substring(1, value.length() - 1);
        values.put(line.substring(0, equal).strip(), value);
      }
    }
    values.putAll(System.getenv());
    String host = values.getOrDefault("DB_HOST", "127.0.0.1");
    String database = values.getOrDefault("DB_NAME", "testdesk");
    int port = Integer.parseInt(values.getOrDefault("DB_PORT", "3307"));
    if (!host.matches("[a-zA-Z0-9.-]+")
        || !database.matches("[a-zA-Z0-9_]+")
        || port < 1
        || port > 65535)
      throw new IllegalArgumentException("Revise DB_HOST, DB_NAME y DB_PORT en .env.");
    return new AppConfig(
        host,
        port,
        database,
        values.getOrDefault("DB_USER", "testdesk_app"),
        values.getOrDefault("DB_PASSWORD", "testdesk_local_dev"));
  }
}
