package support;

import device.Fitbit;
import device.fitbitdata.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Calendar;
import java.util.List;

/**
 * Classe che si connette al database e permette di aggiornare i dati in modo automatico tramite dei runnable
 */
public class Database {

    /**
     * Un logger per scrivere a console eventuali errori o informazioni
     */
    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    /**
     * Il percorso di dove trovare il database, strutturato in: &lt;interfaccia&gt;:&lt;implementazione&gt;:&lt;percorso vero e proprio&gt;
     */
    public static final String DB_LOCATION = "jdbc:sqlite:";

    /**
     * Il nome del database (aka il nome del file)
     */
    public static final String DB_NAME = "user_data.db";

    /**
     * Un calendario, visto che ci serve sapere quando inseriamo i dati
     */
    private static final Calendar CALENDAR = Calendar.getInstance();

    /**
     * Una costante che indica quanti millisecondi ci sono in un minuto (utile per le conversioni)
     */
    private static final int MINUTES_TO_MILLISEC = 60000;

    /**
     * L'unica istanza del database (dato che e' una classe Singleton)
     */
    private static Database instance;

    /**
     * La connessione al database
     */
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
        statement.execute("CREATE TABLE IF NOT EXISTS heart (day_hour DATE PRIMARY KEY, rate DOUBLE)");
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
     * <br>
     * L'implementazione e' synchronized (si spera thread safe)
     */
    public synchronized static Database getInstance() {
        try {
            instance = instance==null? new Database():instance;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Prendi il Runnable che automaticamente gestisce l'inserimento dei dati orari (per ora solo il battito cardiaco)<br>
     * <br>
     * Il runnable e' synchronized (si spera thread safe)
     * @param fitbit la classe che contiene i dati aggiornati
     * @return un Runnable
     */
    public Runnable insertHourlyData(Fitbit fitbit) {
        return new Runnable() {
            @Override
            public synchronized void run() {
                boolean notInterrupted = true;
                boolean retry = false;
                double heartRate = 0;
                Timestamp now = null;

                while (notInterrupted) {
                    CALENDAR.setTimeInMillis(System.currentTimeMillis());
                    try {
                        wait((retry ? 1:58-CALENDAR.get(Calendar.MINUTE)) * MINUTES_TO_MILLISEC);
                        CALENDAR.setTimeInMillis(System.currentTimeMillis());
                        if (retry == false) {
                            now = new Timestamp(CALENDAR.getTimeInMillis());
                            heartRate = fitbit.getHeartRate(60);
                        }

                        conn.createStatement().execute("INSERT INTO heart (day_hour, rate) VALUES ( ' " + now + " ', '" + heartRate + "')");
                        LOG.info(CALENDAR.getTime() + " > Ho inserito i dati orari "+now);
                        retry = false;
                        wait(MINUTES_TO_MILLISEC);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notInterrupted = false;
                    } catch (Exception e) {
                        LOG.error(CALENDAR.getTime() + " " + e.getLocalizedMessage() + " > Non e' stato possibile aggingere i dati orari al database, riprovo fra un minuto "+now);
                        retry = true;
                    }
                }
            }
        };
    }

    /**
     * Prendi il Runnable che automaticamente gestisce l'inserimento dei dati giornalieri<br>
     * <br>
     * Il runnable e' synchronized (si spera thread safe)
     * @param fitbit la classe che contiene i dati aggiornati
     * @return un Runnable
     */
    public Runnable insertDailyData(Fitbit fitbit) {
        return new Runnable() {
            @Override
            public synchronized void run() {
                boolean notInterrupted = true;
                boolean retry = false;
                double heartRate = 0;
                long sleepTime = 0;
                long steps = 0;
                List<Sleep.SleepData> sleepDatas = null;
                Timestamp now = null;

                while (notInterrupted) {
                    CALENDAR.setTimeInMillis(System.currentTimeMillis());
                    try {
                        wait((retry? 1:(22-CALENDAR.get(Calendar.HOUR))*60) * MINUTES_TO_MILLISEC);
                        CALENDAR.setTimeInMillis(System.currentTimeMillis());
                        if (retry == false) {
                            heartRate = fitbit.getHeartRate(60 * 24);
                            sleepTime = fitbit.getHoursSleep();
                            sleepDatas = fitbit.getDetailedSleep();
                            steps = fitbit.getSteps();
                            now = new Timestamp(CALENDAR.getTimeInMillis());
                        }

                        conn.createStatement().execute("INSERT INTO total (day, sleep_time, heart_rate, steps) VALUES ( '" + now + "', '" + sleepTime + "', '" + heartRate + "', '" + steps + "' )");
                        for (Sleep.SleepData data : sleepDatas)
                            conn.createStatement().execute("INSERT INTO total (sleep_start, sleep_end, duration) VALUES ( '" + data.start_date + "', '" + data.end_date + "', '" + data.duration + "' )");
                        LOG.info(CALENDAR.getTime() + " > Ho inserito i dati giornalieri");
                        retry = false;
                        wait(MINUTES_TO_MILLISEC);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notInterrupted = false;
                    } catch (Exception e) {
                        LOG.error(CALENDAR.getTime() + " " + e.getLocalizedMessage() + " > Non e' stato possibile aggingere i dati della giornata al database, riprovo fra un minuto");
                        retry = true;
                    }
                }
            }
        };
    }
}
