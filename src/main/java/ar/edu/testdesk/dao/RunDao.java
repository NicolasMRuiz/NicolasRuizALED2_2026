package ar.edu.testdesk.dao;

import static ar.edu.testdesk.database.Database.*;

import ar.edu.testdesk.database.Database;
import ar.edu.testdesk.model.*;
import java.sql.*;
import java.util.*;

public final class RunDao {
  private final Database db;

  public RunDao(Database db) {
    this.db = db;
  }

  public long start(Long planId, String origin, int skipped) {
    return db.transaction(
        c ->
            insert(
                c,
                "INSERT INTO test_runs(plan_id,origin,skipped) VALUES(?,?,?)",
                planId,
                origin,
                skipped));
  }

  public void append(long runId, TestResult r) {
    db.transaction(
        c -> {
          insert(
              c,
              "INSERT INTO"
                  + " test_results(run_id,case_id,case_title,method,url,expected_status,actual_status,expected_text,started_at,duration_ms,status,message,response_excerpt)"
                  + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
              runId,
              r.caseId(),
              r.caseTitle(),
              r.method(),
              r.url(),
              r.expectedStatus(),
              r.actualStatus(),
              r.expectedText(),
              r.startedAt(),
              r.durationMs(),
              r.status(),
              r.message(),
              r.responseExcerpt());
          update(
              c,
              "UPDATE test_runs SET"
                  + " total=total+1,passed=passed+?,failed=failed+?,errors=errors+?,duration_ms=duration_ms+?"
                  + " WHERE id=?",
              r.status().equals("PASS") ? 1 : 0,
              r.status().equals("FAIL") ? 1 : 0,
              r.status().equals("ERROR") ? 1 : 0,
              r.durationMs(),
              runId);
          return null;
        });
  }

  public void finish(long id, String status, long elapsed) {
    db.transaction(
        c -> {
          update(c, "UPDATE test_runs SET status=?,duration_ms=? WHERE id=?", status, elapsed, id);
          return null;
        });
  }

  public List<TestRun> list() {
    return db.read(
        c -> {
          try (PreparedStatement p = statement(c, "SELECT * FROM test_runs ORDER BY id DESC");
              ResultSet r = p.executeQuery()) {
            List<TestRun> rows = new ArrayList<>();
            while (r.next()) rows.add(mapRun(r));
            return rows;
          }
        });
  }

  public List<TestResult> results(long runId) {
    return db.read(
        c -> {
          try (PreparedStatement p =
                  statement(c, "SELECT * FROM test_results WHERE run_id=? ORDER BY id", runId);
              ResultSet r = p.executeQuery()) {
            return mapResults(r);
          }
        });
  }

  public List<TestResult> allResults() {
    return db.read(
        c -> {
          try (PreparedStatement p = statement(c, "SELECT * FROM test_results ORDER BY id DESC");
              ResultSet r = p.executeQuery()) {
            return mapResults(r);
          }
        });
  }

  private List<TestResult> mapResults(ResultSet r) throws SQLException {
    List<TestResult> rows = new ArrayList<>();
    while (r.next()) {
      int actual = r.getInt("actual_status");
      Integer actualStatus = r.wasNull() ? null : actual;
      rows.add(
          new TestResult(
              r.getLong("id"),
              r.getLong("run_id"),
              nullableLong(r, "case_id"),
              r.getString("case_title"),
              r.getString("method"),
              r.getString("url"),
              r.getInt("expected_status"),
              actualStatus,
              r.getString("expected_text"),
              r.getObject("started_at", java.time.LocalDateTime.class),
              r.getLong("duration_ms"),
              r.getString("status"),
              r.getString("message"),
              r.getString("response_excerpt")));
    }
    return rows;
  }

  public Dashboard dashboard() {
    return db.read(
        c -> {
          try (PreparedStatement p =
                  statement(
                      c,
                      "SELECT (SELECT COUNT(*) FROM test_cases) AS cases_count,(SELECT COUNT(*)"
                          + " FROM test_cases WHERE active=TRUE) AS active_count,(SELECT COUNT(*)"
                          + " FROM test_plans) AS plans_count,(SELECT COUNT(*) FROM bugs WHERE"
                          + " status<>'Resuelto') AS bugs_count");
              ResultSet r = p.executeQuery()) {
            r.next();
            TestRun last = null;
            try (PreparedStatement lp =
                    statement(c, "SELECT * FROM test_runs ORDER BY id DESC LIMIT 1");
                ResultSet lr = lp.executeQuery()) {
              if (lr.next()) last = mapRun(lr);
            }
            return new Dashboard(r.getLong(1), r.getLong(2), r.getLong(3), r.getLong(4), last);
          }
        });
  }

  private TestRun mapRun(ResultSet r) throws SQLException {
    return new TestRun(
        r.getLong("id"),
        nullableLong(r, "plan_id"),
        r.getString("origin"),
        r.getObject("started_at", java.time.LocalDateTime.class),
        r.getLong("duration_ms"),
        r.getInt("total"),
        r.getInt("passed"),
        r.getInt("failed"),
        r.getInt("errors"),
        r.getInt("skipped"),
        r.getString("status"));
  }
}
