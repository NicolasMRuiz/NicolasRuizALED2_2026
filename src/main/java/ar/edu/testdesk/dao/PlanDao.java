package ar.edu.testdesk.dao;

import static ar.edu.testdesk.database.Database.*;

import ar.edu.testdesk.database.Database;
import ar.edu.testdesk.model.*;
import java.sql.*;
import java.util.*;

public final class PlanDao {
  private final Database db;

  public PlanDao(Database db) {
    this.db = db;
  }

  public List<TestPlan> list(String search) {
    return db.read(
        c -> {
          try (PreparedStatement p =
                  statement(
                      c,
                      "SELECT * FROM test_plans WHERE LOCATE(?,name)>0 ORDER BY id DESC",
                      search);
              ResultSet r = p.executeQuery()) {
            List<TestPlan> rows = new ArrayList<>();
            while (r.next())
              rows.add(
                  new TestPlan(
                      r.getLong("id"),
                      r.getString("name"),
                      r.getString("description"),
                      ids(c, r.getLong("id")),
                      r.getObject("created_at", java.time.LocalDateTime.class),
                      r.getObject("updated_at", java.time.LocalDateTime.class)));
            return rows;
          }
        });
  }

  private List<Long> ids(Connection c, long id) throws SQLException {
    try (PreparedStatement p =
            statement(
                c, "SELECT case_id FROM test_plan_cases WHERE plan_id=? ORDER BY case_id", id);
        ResultSet r = p.executeQuery()) {
      List<Long> ids = new ArrayList<>();
      while (r.next()) ids.add(r.getLong(1));
      return ids;
    }
  }

  public List<TestCase> cases(long id) {
    return db.read(
        c -> {
          try (PreparedStatement p =
                  statement(
                      c,
                      "SELECT t.* FROM test_cases t JOIN test_plan_cases pc ON pc.case_id=t.id"
                          + " WHERE pc.plan_id=? ORDER BY t.id",
                      id);
              ResultSet r = p.executeQuery()) {
            List<TestCase> rows = new ArrayList<>();
            while (r.next()) rows.add(CaseDao.map(r));
            return rows;
          }
        });
  }

  public long save(TestPlan plan) {
    return db.transaction(
        c -> {
          long id;
          if (plan.id() == null)
            id =
                insert(
                    c,
                    "INSERT INTO test_plans(name,description) VALUES(?,?)",
                    plan.name(),
                    plan.description());
          else {
            id = plan.id();
            update(
                c,
                "UPDATE test_plans SET name=?,description=?,updated_at=CURRENT_TIMESTAMP(3) WHERE"
                    + " id=?",
                plan.name(),
                plan.description(),
                id);
            update(c, "DELETE FROM test_plan_cases WHERE plan_id=?", id);
          }
          for (long caseId : new LinkedHashSet<>(plan.caseIds()))
            update(c, "INSERT INTO test_plan_cases(plan_id,case_id) VALUES(?,?)", id, caseId);
          return id;
        });
  }

  public void delete(long id) {
    db.transaction(
        c -> {
          update(c, "DELETE FROM test_plans WHERE id=?", id);
          return null;
        });
  }
}
