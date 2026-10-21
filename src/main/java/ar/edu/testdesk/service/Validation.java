package ar.edu.testdesk.service;

import ar.edu.testdesk.model.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.*;

public final class Validation {
  private Validation() {}

  public static void text(String label, String value, int max, boolean required) {
    if (value == null || (required && value.isBlank()))
      throw new IllegalArgumentException(label + ": es obligatorio.");
    if (value.length() > max)
      throw new IllegalArgumentException(label + ": máximo " + max + " caracteres.");
  }

  public static Map<String, String> headers(String text) {
    text("Headers", text, 8000, false);
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (String line : text.split("\\R")) {
      if (line.isBlank()) continue;
      int colon = line.indexOf(':');
      if (colon < 1)
        throw new IllegalArgumentException("Headers: use Nombre: valor, uno por línea.");
      String name = line.substring(0, colon).strip(), value = line.substring(colon + 1).strip();
      if (!name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+") || value.isBlank())
        throw new IllegalArgumentException("Header inválido: se requieren nombre y valor.");
      try {
        HttpRequest.newBuilder(URI.create("http://127.0.0.1")).header(name, value);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Header no permitido o con caracteres inválidos: " + name);
      }
      if (headers.put(name, value) != null)
        throw new IllegalArgumentException("Header repetido: " + name);
    }
    return headers;
  }

  public static void testCase(TestCase t) {
    text("Título", t.title(), 160, true);
    text("Descripción", t.description(), 8000, false);
    text("URL", t.url(), 2048, true);
    text("Body", t.body(), 64000, false);
    text("Texto esperado", t.expectedText(), 4000, false);
    if (!List.of("GET", "POST", "PUT", "DELETE").contains(t.method()))
      throw new IllegalArgumentException("Método HTTP no soportado.");
    try {
      URI uri = URI.create(t.url());
      if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
          || uri.getHost() == null
          || uri.getUserInfo() != null
          || uri.getFragment() != null
          || uri.getPort() > 65535
          || uri.getPort() == 0) throw new IllegalArgumentException();
      HttpRequest.newBuilder(uri);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "URL: use HTTP/HTTPS con host válido, sin usuario, contraseña ni fragmento.");
    }
    if (t.expectedStatus() < 100 || t.expectedStatus() > 599)
      throw new IllegalArgumentException("Código esperado: debe estar entre 100 y 599.");
    headers(t.headers());
  }

  public static void plan(TestPlan p) {
    text("Nombre", p.name(), 160, true);
    text("Descripción", p.description(), 8000, false);
  }

  public static void bug(Bug b) {
    text("Título", b.title(), 160, true);
    text("Descripción", b.description(), 8000, false);
    text("Pasos", b.steps(), 8000, false);
    text("Esperado", b.expected(), 8000, false);
    text("Obtenido", b.actual(), 8000, false);
    if (!List.of("Baja", "Media", "Alta", "Crítica").contains(b.priority()))
      throw new IllegalArgumentException("Prioridad inválida.");
    if (!List.of("Abierto", "En progreso", "Resuelto").contains(b.status()))
      throw new IllegalArgumentException("Estado inválido.");
  }
}
