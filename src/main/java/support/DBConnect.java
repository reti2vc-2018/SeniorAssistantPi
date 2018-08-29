package support;

import java.sql.*;

/**
 * Handle the connection to the SQLite database that stores our tasks.
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.1 (06/05/2018)
 */
public class DBConnect {

    // todo add a db where we put daily (or hourly, but only for heart) updates
    public static final String DB_LOCATION = "jdbc:sqlite:src/main/resources/";
    public static final String DB_NAME = "user_data.db";

    private static DBConnect instance;

    private final Connection conn;

    private DBConnect() throws SQLException {
        conn = DriverManager.getConnection(DB_LOCATION + DB_NAME);
        buildTablesIfNotExisting();
    }

    public static DBConnect getInstance() {
        try {
            instance = instance==null? new DBConnect():instance;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private void buildTablesIfNotExisting() throws SQLException {
        Statement statement = conn.createStatement();
        // todo working, but not quite well
        statement.execute("CREATE TABLE IF NOT EXISTS user (user VARCHAR(16) PRIMARY KEY, name VARCHAR(16), birthday DATE);");
        statement.execute("CREATE TABLE IF NOT EXISTS heart_rate (date DATE, rate DOUBLE, user VARCHAR(16), PRIMARY KEY(date, user));");
    }
}
