package ar.edu.testdesk.report;

import ar.edu.testdesk.model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class HtmlReport {
  public void export(Path path, TestRun run, List<TestResult> results) throws IOException {
    StringBuilder html =
        new StringBuilder(
            """
            <!doctype html><html lang="es"><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>TestDesk · Reporte</title><style>
            body{font:15px system-ui,sans-serif;margin:32px;color:#182235;background:#f4f6fa}
            h1{color:#244c8c}table{border-collapse:collapse;width:100%;background:white}
            td,th{border:1px solid #cdd4df;padding:12px;text-align:left;vertical-align:top}
            th{background:#e4ebf5}pre{white-space:pre-wrap;overflow-wrap:anywhere;margin:5px 0}
            .PASS{color:#146b3a}.FAIL{color:#a33b12}.ERROR{color:#a01936}
            @media print{body{margin:0;background:white}tr{break-inside:avoid}}
            </style><body><h1>TestDesk · Reporte de ejecución</h1>
            """);
    html.append("<h2>")
        .append(escape(run.origin()))
        .append("</h2><p>Corrida #")
        .append(run.id())
        .append(" · Fecha (UTC): ")
        .append(escape(run.startedAt()))
        .append(" · Estado: ")
        .append(escape(run.status()))
        .append("</p><p>Total: ")
        .append(run.total())
        .append(" · PASS: ")
        .append(run.passed())
        .append(" · FAIL: ")
        .append(run.failed())
        .append(" · ERROR: ")
        .append(run.errors())
        .append(" · Inactivos omitidos: ")
        .append(run.skipped())
        .append(" · Duración: ")
        .append(run.durationMs())
        .append(" ms</p>")
        .append(
            "<table><thead><tr><th>Caso / solicitud</th><th>Esperado /"
                + " obtenido</th><th>Resultado</th><th>Detalle</th></tr></thead><tbody>");
    for (TestResult r : results) {
      html.append("<tr><td>")
          .append(escape(r.caseTitle()))
          .append("<pre>")
          .append(escape(r.method() + " " + r.url()))
          .append("</pre>")
          .append(escape(r.startedAt()))
          .append(" UTC</td><td>HTTP ")
          .append(r.expectedStatus())
          .append(" / ")
          .append(r.actualStatus() == null ? "sin respuesta" : r.actualStatus())
          .append("<pre>")
          .append(escape(r.expectedText()))
          .append("</pre></td><td class=\"")
          .append(escape(r.status()))
          .append("\">")
          .append(escape(r.status()))
          .append("<p>")
          .append(r.durationMs())
          .append(" ms</p></td><td>")
          .append(escape(r.message()))
          .append("<pre>")
          .append(escape(r.responseExcerpt()))
          .append("</pre></td></tr>");
    }
    html.append(
        "</tbody></table><p>Extractos de hasta 4000 caracteres; datos sensibles conocidos ocultos."
            + " Los headers y el body de la solicitud no se incluyen.</p></body></html>");
    Files.writeString(path, html, StandardCharsets.UTF_8);
  }

  public static String escape(Object value) {
    if (value == null) return "";
    return value
        .toString()
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
