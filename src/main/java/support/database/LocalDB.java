package support.database;

import device.fitbitdata.HeartRate;
import device.fitbitdata.Steps;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe che utilizza un database sqlite che contiene le seguenti tabelle:<br>
 * <ul>
 *     <li>'heart' battito + orario</li>
 *     <li>'sleep' inizio + durata</li>
 *     <li>'steps' data + passi.</li>
 * </ul>
 */
public class LocalDB implements Database {

    /**
     * Il percorso dove trovare il database, strutturato in: &lt;interfaccia&gt;:&lt;implementazione&gt;:&lt;percorso vero e proprio&gt;
     */
    public static final String DB_LOCATION = "jdbc:sqlite:";

    /**
     * Il nome del database (aka il nome del file)
     */
    public static final String DB_NAME = "user_data.db";

    /**
     * La connessione al database
     */
    private final Connection conn;

    /**
     * Crea una connessione al Database specificato in DB_LOCATION e con il nome DB_NAME.
     * Se il Database non esiste lo crea e inizializza anche delle tabelle:
     * <ul>
     *     <li>'heart' battito + orario</li>
     *     <li>'sleep' inizio + durata</li>
     *     <li>'step' data + passi.</li>
     * </ul>
     * @throws SQLException se qualcosa e' andato storto
     */
    public LocalDB() throws SQLException {
        conn = DriverManager.getConnection(DB_LOCATION + DB_NAME);
        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS heart (day DATE PRIMARY KEY, rate DOUBLE)");
        statement.execute("CREATE TABLE IF NOT EXISTS sleep (sleep_start DATE PRIMARY KEY, duration INTEGER)");
        statement.execute("CREATE TABLE IF NOT EXISTS steps (day DATE PRIMARY KEY, steps INTEGER)");
    }

    @Override
    public boolean isReachable() { return conn!=null; }

    @Override
    public boolean updateHeart(long dateMilliSec, double heartRate) {
        Timestamp time = new Timestamp(dateMilliSec);
        return query("IF NOT EXISTS (" +
                "SELECT * " +
                "FROM heart " +
                "WHERE day = '" + time + "') " +
                "BEGIN INSERT INTO heart (day, rate) VALUES ( ' " + time + " ', '" + heartRate + "') " +
                "END;");
    }

    @Override
    public boolean updateSleep(long dateStartSleep, long duration) {
        Timestamp time = new Timestamp(dateStartSleep);
        return query("IF NOT EXISTS (" +
                "SELECT * " +
                "FROM sleep " +
                "WHERE sleep_start = '" + time + "') " +
                "BEGIN INSERT INTO sleep (sleep_start, duration) VALUES ( ' " + time + " ', '" + duration + "') " +
                "END;");
    }

    @Override
    public boolean updateSteps(long dateMilliSec, long steps) {
        Timestamp time = new Timestamp(dateMilliSec);
        return query("IF NOT EXISTS (" +
                "SELECT * " +
                "FROM steps " +
                "WHERE day = '" + time + "') " +
                "INSERT INTO steps (day, steps) VALUES ( ' " + time + " ', '" + steps + "') " +
                "END;");
    }

    @Override
    public List<HeartRate> getHeartDataOfLast(int days) {
        try {
            int dayToSubtract = 15;
            long time = System.currentTimeMillis() - (dayToSubtract * 24 * 60 * 1000); // meno 24 ore per 60 secondi per 100 millisec

            ResultSet result = conn.createStatement().executeQuery("SELECT * FROM heart WHERE day>='" + new Timestamp(time) + "'");
            List<HeartRate> list = new LinkedList<>();

            while(result.next()) {
                HeartRate rate = new HeartRate();
                rate.setAverage(result.getDouble("rate"));
                rate.setDate(result.getDate("day").getTime());

                list.add(rate);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Steps> getStepDataOfLast(int days) {
        try {
            int dayToSubtract = 15;
            long time = System.currentTimeMillis() - (dayToSubtract * 24 * 60 * 1000);

            ResultSet result = conn.createStatement().executeQuery("SELECT * FROM steps WHERE day>='" + new Timestamp(time) + "'");
            List<Steps> list = new LinkedList<>();

            while(result.next()) {
                Steps steps = new Steps();
                steps.setSteps(result.getInt("rate"));
                steps.setDate(result.getDate("day").getTime());

                list.add(steps);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean query(String sql) {
        try {
            conn.createStatement().execute(sql);
            return true;
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }
}
