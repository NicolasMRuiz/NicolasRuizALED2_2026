package ar.edu.testdesk.http;

import ar.edu.testdesk.model.TestCase;
import ar.edu.testdesk.service.Validation;
import java.net.URI;
import java.util.*;
import java.util.regex.*;

public final class Redactor {
  private static final Pattern SECRET =
      Pattern.compile(
          "(?i)(?<![\\w-])([\"']?(?:password|passwd|authorization|cookie|set-cookie|[\\w-]{0,64}token|[\\w-]{0,64}secret|api[_-]?key)[\"']?\\s*[:=]\\s*)(\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|[^\\s,;&}]+)");
  private final List<String> values = new ArrayList<>();

  public Redactor(TestCase t) {
    Validation.headers(t.headers())
        .forEach(
            (key, value) -> {
              values.add(value);
              if (value.toLowerCase(Locale.ROOT).startsWith("bearer "))
                values.add(value.substring(7));
              if (value.toLowerCase(Locale.ROOT).startsWith("basic "))
                values.add(value.substring(6));
            });
    collect(t.body());
    collect(t.url());
    URI uri = URI.create(t.url());
    if (uri.getRawQuery() != null)
      for (String pair : uri.getRawQuery().split("&")) {
        int pos = pair.indexOf('=');
        if (pos >= 0) {
          values.add(pair.substring(pos + 1));
          values.add(
              java.net.URLDecoder.decode(
                  pair.substring(pos + 1), java.nio.charset.StandardCharsets.UTF_8));
        }
      }
    values.removeIf(String::isBlank);
    values.sort(Comparator.comparingInt(String::length).reversed());
  }

  private void collect(String text) {
    Matcher m = SECRET.matcher(text);
    while (m.find()) {
      String v = m.group(2);
      values.add(v.replaceAll("^[\"']|[\"']$", ""));
    }
  }

  public String clean(String text) {
    String result =
        text.replaceAll(
            "(?im)^(\\s*(?:authorization|proxy-authorization|cookie|set-cookie)\\s*:)\\s*[^\\r"
                + "\\n"
                + "]*",
            "$1 [oculto]");
    for (String value : values) result = result.replace(value, "[oculto]");
    result = SECRET.matcher(result).replaceAll("$1[oculto]");
    return result
        .replaceAll("(?i)Bearer\\s+[^\\s\"',;]+", "Bearer [oculto]")
        .replaceAll("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+", "[token oculto]");
  }

  public String url(String url) {
    int q = url.indexOf('?');
    return limit(
        clean(q < 0 ? url : url.substring(0, q)) + (q < 0 ? "" : "?[parámetros ocultos]"), 2048);
  }

  public static String limit(String value, int max) {
    return value.substring(0, Math.min(max, value.length()));
  }
}
