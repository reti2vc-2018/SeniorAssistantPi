package support.database;

import device.Fitbit;
import device.fitbitdata.HeartRate;
import device.fitbitdata.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

public interface Database {

    /**
     * Un logger per scrivere a console eventuali errori o informazioni
     */
    Logger LOG = LoggerFactory.getLogger("DB");

    /**
     * Una costante che indica quanti millisecondi ci sono in un minuto (utile per le conversioni)
     */
    int MILLISEC_IN_MINUTE = 60000;

    /**
     * Dice solamente se e' possibile collegarsi al database e se si possono fare delle query
     * @return vero se si pu' falso in altri casi.
     */
    boolean isReachable();

    /**
     * Inserisce nuovi dati del paziente nel database.
     * @param dateMilliSec la data che si vuole inserire in millisec
     * @param heartRate il battito cardiaco
     * @return vero se ha inserito o i dati esistevano gia' falso se non ce l'ha fatta
     */
    boolean updateHeart(long dateMilliSec, double heartRate);
    /**
     * Inserisce nuovi dati del paziente nel database.
     * @param dateStartSleep la data che si vuole inserire in millisec
     * @param duration per quanto e' durato il sonno
     * @return vero se ha inserito o i dati esistevano gia' falso se non ce l'ha fatta
     */
    boolean updateSleep(long dateStartSleep, long duration);
    /**
     * Inserisce nuovi dati del paziente nel database.
     * @param dateMilliSec la data che si vuole inserire in millisec
     * @param steps i passi fatti
     * @return vero se ha inserito o i dati esistevano gia' falso se non ce l'ha fatta
     */
    boolean updateSteps(long dateMilliSec, long steps);

    /**
     * Riceve i dati del cuore dal giorno selezionato fino ad oggi
     * @param days quanti giorni devono esser considerati
     * @return una lista dei battiti cardiaci degli ultimi X giorni (ordinati da oggi al giorno X)
     */
    List<HeartRate> getHeartDataOfLast(int days);

    /**
     * Prendi il Thread che automaticamente gestisce l'inserimento dei dati orari (per ora solo il battito cardiaco)<br>
     * Se per caso c'e' un fallimento riprova ad inserire i dati ogni x minuti, indicati dal terzo parametro<br>
     * @param database il database in cui inserirlo
     * @param fitbit la classe che contiene i dati aggiornati
     * @param retryMinutes ogni quanti minuti deve riprovare ad inviare la richiesta
     * @return un Thread
     */
    static Thread insertHourlyDataIn(Database database, Fitbit fitbit, int retryMinutes) {
        Runnable runnable = new Runnable() {
            @Override
            public synchronized void run() {
                LOG.info("Aggiornamento orario iniziato");
                try {
                    boolean retry;
                    long now = System.currentTimeMillis();
                    double heartRate = 30;//fitbit.getHeartRate(60);
                    do {
                        retry = !database.updateHeart(now, heartRate);
                        LOG.info("Aggiornamento " + (!retry ? "riuscito" : "fallito, riprovo fra " + retryMinutes + " minuti"));
                        if (retry)
                            wait(retryMinutes * MILLISEC_IN_MINUTE);
                    } while(retry);
                } catch (Exception e) {
                    LOG.warn("Aggiornamento interrotto");
                }
            }
        };

        return getThreadStartingEach(runnable, "update-hourly-data", 60);
    }

    /**
     * Prendi il Thread che automaticamente gestisce l'inserimento dei dati giornalieri<br>
     * Se per caso c'e' un fallimento riprova ad inserire i dati ogni x minuti, indicati dal terzo parametro<br>
     * @param database il database in cui inserirlo
     * @param fitbit la classe che contiene i dati aggiornati
     * @param retryMinutes ogni quanti minuti deve riprovare ad inviare la richiesta
     * @return un Thread da far partire
     */
    static Thread insertDailyData(Database database, Fitbit fitbit, int retryMinutes) {
        Runnable runnable = new Runnable() {
            @Override
            public synchronized void run() {
                LOG.info("Aggiornamento giornaliero iniziato");
                try {
                    boolean retry;
                    long steps = fitbit.getSteps();
                    List<Sleep.SleepData> sleepDatas = fitbit.getDetailedSleep();
                    long now = System.currentTimeMillis();
                    do {
                        retry = !database.updateSteps(now, steps);
                        for (Sleep.SleepData data : sleepDatas)
                            retry = retry && !database.updateSleep(data.start_date, data.duration);

                        LOG.info("Aggiornamento " + (!retry ? "riuscito" : "fallito, riprovo fra " + retryMinutes + " minuti"));
                        if (retry)
                            wait(retryMinutes * MILLISEC_IN_MINUTE);
                    } while (retry);
                } catch (Exception e) {
                    LOG.warn("Aggiornamento interrotto");
                }
            }
        };

        return getThreadStartingEach(runnable, "update-daily-data", 24*60);
    }

    /**
     * Restuisce un thread che se fatto partire, esegue il runnable in un sub-thread ogni X minuti<br>
     * Il sotto thread lanciato avra' lo stesso nome, ma con un trattino e la data di lancio a seguito<br>
     * Se il thread viene interrotto non fa piu' partire il runnable e si ferma
     * @param runnable il runnable da lanciare
     * @param threadName il nome da dare al thread
     * @param minutes i minuti da aspettare, se negativi o 0 ritorna null
     */
    static Thread getThreadStartingEach(final Runnable runnable, String threadName, int minutes) {
        if(minutes<1)
            return null;

        return new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                boolean notInterrupted = true;
                do {
                    try {
                        wait(minutes * MILLISEC_IN_MINUTE);
                        Thread thread = new Thread(runnable, threadName + "-" + new Timestamp(System.currentTimeMillis()));
                        thread.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        notInterrupted = false;
                    }
                } while (notInterrupted);
            }
        }, threadName);
    }
}
