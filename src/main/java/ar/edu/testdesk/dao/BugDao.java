package ar.edu.testdesk.dao;

import static ar.edu.testdesk.database.Database.*;

import ar.edu.testdesk.database.Database;
import ar.edu.testdesk.model.Bug;
import java.sql.*;
import java.util.*;

public final class BugDao {
  private final Database db;

  public BugDao(Database db) {
    this.db = db;
  }

  public List<Bug> list(String search, String status, String priority) {
    return db.read(
        c -> {
          try (PreparedStatement p =
                  statement(
                      c,
                      "SELECT * FROM bugs WHERE LOCATE(?,title)>0 AND (? IS NULL OR status=?) AND"
                          + " (? IS NULL OR priority=?) ORDER BY id DESC",
                      search,
                      status,
                      status,
                      priority,
                      priority);
              ResultSet r = p.executeQuery()) {
            List<Bug> rows = new ArrayList<>();
            while (r.next())
              rows.add(
                  new Bug(
                      r.getLong("id"),
                      r.getString("title"),
                      r.getString("description"),
                      r.getString("steps"),
                      r.getString("expected"),
                      r.getString("actual"),
                      r.getString("priority"),
                      r.getString("status"),
                      nullableLong(r, "case_id"),
                      nullableLong(r, "result_id"),
                      r.getObject("created_at", java.time.LocalDateTime.class),
                      r.getObject("updated_at", java.time.LocalDateTime.class)));
            return rows;
          }
        });
  }

  public long save(Bug b) {
    return db.transaction(
        c -> {
          Object[] args = {
            b.title(),
            b.description(),
            b.steps(),
            b.expected(),
            b.actual(),
            b.priority(),
            b.status(),
            b.caseId(),
            b.resultId()
          };
          if (b.id() == null)
            return insert(
                c,
                "INSERT INTO"
                    + " bugs(title,description,steps,expected,actual,priority,status,case_id,result_id)"
                    + " VALUES(?,?,?,?,?,?,?,?,?)",
                args);
          List<Object> updateArgs = new ArrayList<>(Arrays.asList(args));
          updateArgs.add(b.id());
          update(
              c,
              "UPDATE bugs SET"
                  + " title=?,description=?,steps=?,expected=?,actual=?,priority=?,status=?,case_id=?,result_id=?,updated_at=CURRENT_TIMESTAMP(3)"
                  + " WHERE id=?",
              updateArgs.toArray());
          return b.id();
        });
  }

  public void delete(long id) {
    db.transaction(
        c -> {
          update(c, "DELETE FROM bugs WHERE id=?", id);
          return null;
        });
  }
}
