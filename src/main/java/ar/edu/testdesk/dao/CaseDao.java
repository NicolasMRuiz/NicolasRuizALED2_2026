package ar.edu.testdesk.dao;

import static ar.edu.testdesk.database.Database.*;

import ar.edu.testdesk.database.Database;
import ar.edu.testdesk.model.TestCase;
import java.sql.*;
import java.util.*;

public final class CaseDao {
  private final Database db;

  public CaseDao(Database db) {
    this.db = db;
  }

  public List<TestCase> list(String search, String method, Boolean active) {
    return db.read(
        c -> {
          try (PreparedStatement p =
                  statement(
                      c,
                      "SELECT * FROM test_cases WHERE LOCATE(?,title)>0 AND (? IS NULL OR method=?)"
                          + " AND (? IS NULL OR active=?) ORDER BY id DESC",
                      search,
                      method,
                      method,
                      active,
                      active);
              ResultSet r = p.executeQuery()) {
            List<TestCase> rows = new ArrayList<>();
            while (r.next()) rows.add(map(r));
            return rows;
          }
        });
  }

  public TestCase find(long id) {
    return db.read(
        c -> {
          try (PreparedStatement p = statement(c, "SELECT * FROM test_cases WHERE id=?", id);
              ResultSet r = p.executeQuery()) {
            if (!r.next())
              throw new IllegalArgumentException("El caso ya no existe. Actualice la lista.");
            return map(r);
          }
        });
  }

  public long save(TestCase t) {
    return db.transaction(c -> save(c, t, null));
  }

  private long save(Connection c, TestCase t, String demoKey) throws SQLException {
    Object[] values = {
      t.title(),
      t.description(),
      t.method(),
      t.url(),
      t.headers(),
      t.body(),
      t.expectedStatus(),
      t.expectedText(),
      t.active()
    };
    if (t.id() == null) {
      List<Object> args = new ArrayList<>(Arrays.asList(values));
      args.add(demoKey);
      return insert(
          c,
          "INSERT INTO"
              + " test_cases(title,description,method,url,headers,body,expected_status,expected_text,active,demo_key)"
              + " VALUES(?,?,?,?,?,?,?,?,?,?)",
          args.toArray());
    }
    List<Object> args = new ArrayList<>(Arrays.asList(values));
    args.add(t.id());
    update(
        c,
        "UPDATE test_cases SET"
            + " title=?,description=?,method=?,url=?,headers=?,body=?,expected_status=?,expected_text=?,active=?,updated_at=CURRENT_TIMESTAMP(3)"
            + " WHERE id=?",
        args.toArray());
    return t.id();
  }

  public int seed(List<TestCase> examples) {
    return db.transaction(
        c -> {
          int added = 0;
          for (int i = 0; i < examples.size(); i++) {
            String key = "demo-v1-" + i;
            try (PreparedStatement p =
                    statement(c, "SELECT id FROM test_cases WHERE demo_key=?", key);
                ResultSet r = p.executeQuery()) {
              if (!r.next()) {
                save(c, examples.get(i), key);
                added++;
              }
            }
          }
          return added;
        });
  }

  public void delete(long id) {
    db.transaction(
        c -> {
          update(c, "DELETE FROM test_cases WHERE id=?", id);
          return null;
        });
  }

  public static TestCase map(ResultSet r) throws SQLException {
    return new TestCase(
        r.getLong("id"),
        r.getString("title"),
        r.getString("description"),
        r.getString("method"),
        r.getString("url"),
        r.getString("headers"),
        r.getString("body"),
        r.getInt("expected_status"),
        r.getString("expected_text"),
        r.getBoolean("active"),
        r.getObject("created_at", java.time.LocalDateTime.class),
        r.getObject("updated_at", java.time.LocalDateTime.class));
  }
}
