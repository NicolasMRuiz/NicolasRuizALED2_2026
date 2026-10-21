package ar.edu.testdesk.database;

import ar.edu.testdesk.config.AppConfig;
import java.sql.*;
import java.util.Properties;

public final class Database {
  private final AppConfig config;

  public Database(AppConfig config) {
    this.config = config;
  }

  public Connection open() throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", config.user());
    properties.setProperty("password", config.password());
    properties.setProperty("connectTimeout", "5000");
    properties.setProperty("socketTimeout", "15000");
    properties.setProperty("connectionTimeZone", "UTC");
    properties.setProperty("forceConnectionTimeZoneToSession", "true");
    // Conexión local de desarrollo, publicada únicamente en loopback por Compose.
    properties.setProperty("sslMode", "DISABLED");
    properties.setProperty("allowPublicKeyRetrieval", "true");
    return DriverManager.getConnection(
        "jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database(),
        properties);
  }

  @FunctionalInterface
  public interface Work<T> {
    T run(Connection connection) throws SQLException;
  }

  public <T> T read(Work<T> work) {
    try (Connection connection = open()) {
      return work.run(connection);
    } catch (SQLException e) {
      throw failure(e);
    }
  }

  public <T> T transaction(Work<T> work) {
    try (Connection connection = open()) {
      connection.setAutoCommit(false);
      try {
        T result = work.run(connection);
        connection.commit();
        return result;
      } catch (SQLException | RuntimeException e) {
        try {
          connection.rollback();
        } catch (SQLException rollback) {
          e.addSuppressed(rollback);
        }
        throw e;
      }
    } catch (SQLException e) {
      throw failure(e);
    }
  }

  private DataAccessException failure(SQLException e) {
    // No imprimir mensajes JDBC: pueden contener valores de las filas o credenciales.
    System.err.println("JDBC: SQLState=" + e.getSQLState() + ", código=" + e.getErrorCode());
    return new DataAccessException(
        "No se pudo completar la operación en MySQL. Verifique .env y ejecute docker compose up -d."
            + " Si eliminó un vínculo desde otra ventana, actualice los datos. Código JDBC: "
            + e.getErrorCode(),
        e);
  }

  public static PreparedStatement statement(Connection c, String sql, Object... args)
      throws SQLException {
    PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    try {
      for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
      return p;
    } catch (SQLException e) {
      p.close();
      throw e;
    }
  }

  public static long insert(Connection c, String sql, Object... args) throws SQLException {
    try (PreparedStatement p = statement(c, sql, args)) {
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (!keys.next()) throw new SQLException("No se obtuvo el ID.");
        return keys.getLong(1);
      }
    }
  }

  public static void update(Connection c, String sql, Object... args) throws SQLException {
    try (PreparedStatement p = statement(c, sql, args)) {
      p.executeUpdate();
    }
  }

  public static Long nullableLong(ResultSet r, String name) throws SQLException {
    long value = r.getLong(name);
    return r.wasNull() ? null : value;
  }
}
