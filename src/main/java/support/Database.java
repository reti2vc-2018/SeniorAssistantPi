package support;

import device.Fitbit;
import device.fitbitdata.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// todo add docs
/**
 * Handle the connection to the SQLite database that stores our tasks.
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.1 (06/05/2018)
 */
public class Database {

    public static final String DB_LOCATION = "jdbc:sqlite:src/main/resources/";
    public static final String DB_NAME = "user_data.db";

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private static Database instance;

    private final Connection conn;

    /**
     * Crea una connessione al Database specificato in DB_LOCATION e con il nome DB_NAME.
     * Se il Database non esiste lo crea e inizializza anche delle tabelle:
     * <ul>
     *     <li>'total' contiene i dati utente giorno per giorno.</li>
     *     <li>'heart' battito + orario</li>
     *     <li>'sleep' inizio + fine + durata</li>
     * </ul>
     * @throws SQLException se qualcosa e' andato storto
     */
    private Database() throws SQLException {
        CALENDAR.setTimeInMillis(System.currentTimeMillis());
        conn = DriverManager.getConnection(DB_LOCATION + DB_NAME);
        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS total (day DATE PRIMARY KEY, sleep_time INTEGER, heart_rate DOUBLE, steps INTEGER)");
        statement.execute("CREATE TABLE IF NOT EXISTS heart (day_hour DATE PRIMARY KEY, heart_rate DOUBLE)");
        statement.execute("CREATE TABLE IF NOT EXISTS sleep (sleep_start DATE PRIMARY KEY, sleep_end DATE, duration INTEGER)");
    }

    /**
     * Crea una connessione al Database specificato in DB_LOCATION e con il nome DB_NAME.
     * Se il Database non esiste lo crea e inizializza anche delle tabelle:
     * <ul>
     *     <li>'total' contiene i dati utente giorno per giorno.</li>
     *     <li>'heart' battito + orario</li>
     *     <li>'sleep' inizio + fine + durata</li>
     * </ul>
     */
    public static Database getInstance() {
        try {
            instance = instance==null? new Database():instance;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Prendi il Runnable che automaticamente gestisce l'inserimento dei dati orari (per ora solo il battito cardiaco)
     * @param fitbit la classe che contiene i dati aggiornati
     * @return un Runnable
     */
    public Runnable insertHourlyData(Fitbit fitbit) {
        Runnable runnable = () -> {
            boolean notInterrupted = true;
            boolean retry = false;
            double heartRate = 0;
            Date now = null;

            while(notInterrupted) {
                try {
                    wait((retry? 1:59-CALENDAR.get(Calendar.MINUTE)) * 60000);
                    if (retry == false) {
                        heartRate = fitbit.getHeartRate(60);
                        now = CALENDAR.getTime();
                    }

                    conn.createStatement().execute("INSERT INTO heart (day_hour, rate) VALUE ( ' " + now + " ', '" + heartRate + "')");
                    retry = false;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    notInterrupted = false;
                } catch (Exception e) {
                    LOG.error("Non e' stato possibile aggingere i dati orari al database, riprovo fra un minuto");
                    retry = true;
                }
            }
        };
        return runnable;
    }

    /**
     * Prendi il Runnable che automaticamente gestisce l'inserimento dei dati giornalieri
     * @param fitbit la classe che contiene i dati aggiornati
     * @return un Runnable
     */
    public Runnable insertDailyData(Fitbit fitbit) {
        Runnable runnable = () -> {
            boolean notInterrupted = true;
            boolean retry = false;
            double heartRate = 0;
            long sleepTime = 0;
            long steps = 0;
            List<Sleep.SleepData> sleepDatas = null;
            Date now = null;

            while (notInterrupted) {
                int hourToWait = 23 - CALENDAR.get(Calendar.HOUR);

                try {
                    wait(hourToWait * 3600000);
                    if (retry == false) {
                        heartRate = fitbit.getHeartRate(60 * 24);
                        sleepTime = fitbit.getHoursSleep();
                        sleepDatas = fitbit.getDetailedSleep();
                        steps = fitbit.getSteps();
                        now = CALENDAR.getTime();
                    }

                    conn.createStatement().execute("INSERT INTO total (day, sleep_time, heart_rate, steps) VALUE ( '" + now + "', '" + sleepTime + "', '" + heartRate + "', '" + steps + "' )");
                    for (Sleep.SleepData data : sleepDatas)
                        conn.createStatement().execute("INSERT INTO total (sleep_start, sleep_end, duration) VALUE ( '" + data.start_date + "', '" + data.end_date + "', '" + data.duration + "' )");
                    retry = false;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    notInterrupted = false;
                } catch (Exception e) {
                    LOG.error("Non e' stato possibile aggingere i dati orari al database, riprovo fra un minuto");
                    retry = true;
                }
            }
        };
        return runnable;
    }
}
