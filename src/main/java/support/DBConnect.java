package support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handle the connection to the SQLite database that stores our tasks.
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.1 (06/05/2018)
 */
public class DBConnect {

    // todo add a db where we put daily (or hourly, but only for heart) updates
    static private final String DB_LOCATION = "jdbc:sqlite:src/main/resources/tasks.db";
    static private DBConnect instance = null;

    private DBConnect() {
        instance = this;
    }

    public static DBConnect getInstance() {
        return (instance == null ? new DBConnect() : instance);
    }

    public Connection getConnection() throws SQLException {
        try {
            /* todo this might work for create the database
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/");
            Statement s = Conn.createStatement();
            int result = s.executeUpdate("CREATE DATABASE databasename");
            st.close();
            */
            return DriverManager.getConnection(DB_LOCATION);
        } catch (SQLException e) {
            throw new SQLException("Cannot get connection to " + DB_LOCATION, e);
        }
    }
}
