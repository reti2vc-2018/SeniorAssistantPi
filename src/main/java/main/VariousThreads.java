package main;

import device.DialogFlowWebHook;
import device.Fitbit;
import device.Hue;
import device.Sensor;
import device.fitbitdata.HeartRate;
import device.fitbitdata.Steps;
import support.audio.Audio;
import support.audio.AudioFile;
import support.database.Database;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe che contiene tutti i thread che servono al programma per funzionare
 */
public class VariousThreads {

    /**
     * Una costante che indica quanti millisecondi ci sono in un minuto (utile per le conversioni)
     */
    public static final int MILLISEC_IN_MINUTE = 60000;

    /**
     * Quanti minuti di cooldown da impostare dopo che l'utente ha chiesto di modificare le luci
     */
    private static final int COOLDOWN_IN_MINUTES = 20;

    /**
     * Variabile che serve ad impostare un cooldown per la luminosita' automatica
     */
    private final AtomicInteger cooldown = new AtomicInteger(0);

    /**
     * La variabile per far partire della musica
     */
    private final Audio audio;

    /**
     * Costruttore
     */
    public VariousThreads() {
        audio = new AudioFile(); // se si vuole solamente far partire una traccia tra quelle fornite e non Youtube
        // audio = System.getProperty("os.name").startsWith("Windows")? new Musich():new AudioFile();
    }

    /**
     * Fa partire il server Webhook per DialogFlow e continua l'esecuzione
     * @param lights le luci che deve controllare
     */
    public void startWebhook(final Hue lights, final Fitbit fitbit) {
        DialogFlowWebHook df = new DialogFlowWebHook();

        df.addOnAction("LightsON", (params) -> { lights.turnOn(); cooldown.set(COOLDOWN_IN_MINUTES); return null; });
        df.addOnAction("LightsOFF", (params) -> { lights.turnOff(); cooldown.set(COOLDOWN_IN_MINUTES); return null; });
        df.addOnAction("ColorLoop", (params) -> { lights.colorLoop(); return null; });
        df.addOnAction("ChangeColor", (params) -> {
            lights.changeColor(params.get("color").getAsString());
            return null;
        });
        df.addOnAction("SetLights", (params) -> {
            lights.setBrightness(params.get("intensity").getAsInt());
            cooldown.set(COOLDOWN_IN_MINUTES);
            return null;
        });
        df.addOnAction("LightsDOWN", (params) -> {
            if(params.get("intensity").getAsString().equals(""))
                lights.decreaseBrightness();
            else
                lights.decreaseBrightness(params.get("intensity").getAsInt());
            cooldown.set(COOLDOWN_IN_MINUTES);
            return null;
        });
        df.addOnAction("LightsUP", (params) -> {
            if(params.get("intensity").getAsString().equals(""))
                lights.increaseBrightness();
            else
                lights.increaseBrightness(params.get("intensity").getAsInt());
            cooldown.set(COOLDOWN_IN_MINUTES);
            return null;
        });
        df.addOnAction("SetMusic", (param) -> {
            audio.playRandom(param.get("musicType").getAsString());
            return null;
        });
        df.addOnAction("StopMusic", (params) -> { audio.stop(); return null; });
        df.addOnAction("ReqHearthbeat", (params) -> {
            double rate = fitbit.getHeartRate(60);
            return "Il battito medio dell'ultima ora e' di "+rate;
        });
        df.addOnAction("ReqSteps", (params) -> {
            int steps = fitbit.getSteps();
            return "I passi fatti oggi sono "+steps;
        });
        df.addOnAction("ReqDistance", (params) -> {
            double steps = fitbit.getSteps();
            return String.format("Oggi hai percorso circa %.2f kilometri", steps/2000);
        });
        df.addOnAction("ReqSleep", (params) -> {
            double sleep = fitbit.getHoursSleep();
            return String.format("Oggi hai dormito per %.2f ore", sleep);
        });

        df.startServer();
        SeniorAssistant.LOG.info("Webhook partito");
    }

    /**
     * Gestione DB in modo che si aggiorni ogni ora e ogni giorno.<br>
     * Da quando viene fatto partire aspetta un giorno e poi aggiorna i dati sul DB; ripete.<br>
     * Da quando viene fatto partire aspetta un'ora e poi aggiorna i dati sul DB; ripete.
     * @param database il database a cui inviare i dati
     * @param fitbit la sorgente di dati
     */
    public void startInsertData(final Database database, final Fitbit fitbit) {
        try {
            Thread hourlyData = Database.insertHourlyDataIn(database, fitbit, 5);
            Thread dailyData = Database.insertDailyData(database, fitbit, 5);

            hourlyData.start();
            dailyData.start();
            SeniorAssistant.LOG.info("Thread per gli aggiornamenti automatici partiti");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestione delle luci in modo che cambino la luminosita' in base ai dati ricevuti dal sensore<br>
     * Se l'utente pero' cambia il valore delle luci di sua volonta', il processo non modifichera' le luci per almeno un'ora.
     * @param lights le luci da controllare
     * @param sensor i sensori da cui prendere i dati
     */
    public void startHueAutoBrightness(final Hue lights, final Sensor sensor) {
        final int minute = 1;
        final int minBrightness = 20; // valore che va da 0 a 100
        Calendar calendar = Calendar.getInstance();

        Thread thread = getThreadStartingEach(() -> {
            if(cooldown.addAndGet(-minute) <= 0) {
                calendar.setTimeInMillis(System.currentTimeMillis());

                // puo' avere un valore compreso tra -1 e 1
                final double brightFactor =
                    calculateBrightFactor(
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        sensor.getBrightnessLevel(),
                        minBrightness
                    );

                lights.addBrightness(brightFactor*100);
            }
        }, minute, "auto-brightness");

        thread.start();
        SeniorAssistant.LOG.info("Thread per l'impostazione automatica della luminosita' partito");
    }

    /**
     * Permette di far partire un thread che controlla le Hue in base al battito cardiaco.<br>
     * Il battito e' preso sia in tempo reale dal fitbit, che dal databse, che permette di fare una analisi statistica.
     * @param lights le luci da controllare
     * @param fitbit da dove ricevo i dati in tempo reale
     * @param database da dove posso analizzare i vecchi dati
     */
    public void startHueControlledByHeartBeat(final Hue lights, final Fitbit fitbit, final Database database) {
        final int minutes = 30;
        final int delta = 15;
        Thread thread = getThreadStartingEach(new Runnable() {
            @Override
            public synchronized void run() {
                double sum=0;
                int count=0;
                double average;

                List<HeartRate> heartRate = database.getHeartDataOfLast(15);
                Calendar now = Calendar.getInstance();
                Calendar past = Calendar.getInstance();
                now.setTimeInMillis(System.currentTimeMillis());

                for(HeartRate rate: heartRate) {
                    past.setTimeInMillis(rate.getDate());
                    if(past.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY)) {
                        sum += rate.getAverage();
                        count++;
                    }
                }
                average = count!=0? sum/count:0;

                double rateNow = fitbit.getHeartRate(minutes);
                if (Math.abs(rateNow-average) > delta ) { // ALTO
                    lights.decreaseBrightness();
                    audio.playRandom("relax");
                    new AudioFile("molti battiti.wav");
                }
                else if (Math.abs(rateNow-average) < delta) { // BASSO avvisa ma niente musica
                    lights.increaseBrightness();
                    new AudioFile("pochi battiti.wav");
                }
            }
        }, minutes, "lights-with-heartbeat");

        thread.start();
        SeniorAssistant.LOG.info("Thread per il controllo delle luci tramite il battito cardiaco partito");
    }

    /**
     * Controlla che ad una certa ora si siano fatti abbastanza passi, ed in caso contrario avvisa tramite un messaggio vocale.<br>
     *
     * @param database da dove vediamo se si sono fatti abbastanza passi
     */
    public void startCheckSteps(final Database database) {
        final int norm = 4500;
        final int delta = 1500; // average steps for 60 year old -> 3.500-5.500 or so they say

        Thread thread = getThreadStartingAt(new Runnable() {
            @Override
            public synchronized void run() {
                List<Steps> list = database.getStepDataOfLast(1);

                double sum=0;
                int size = list.size();
                for(Steps steps: list)
                    sum += steps.getSteps();
                final long average = size!=0? (long)(sum/size):0;

                if (Math.abs(norm-average) > delta )
                    new AudioFile("molti passi.wav");
                else if (Math.abs(norm-average) < delta)
                    new AudioFile("pochi passi.wav");

            }
        }, 20, "checking-steps");

        thread.start();
    }




    /**
     * Calcola un numero compreso fra -1 e 1 che indica se c'e' bisogno o meno di luminosita'<br>
     * Se i valori inseriti sono maggiori o minori di quelli consentiti, allora verranno limitati<br>
     * ovvero se sono minori del minimo esso diventera' il minimo, stessa cosa con il massimo.
     * @param hour l'ora corrente (valore da 0 a 23)
     * @param minutes i minuti correnti (valore da 0 a 59)
     * @param sensorBright la luminosita' segnata dal sensore (valore da 0 a 100)
     * @param minBrightness la luminosita' minima che si vuole avere (valore da 0 a 100)
     * @return un valore indicante quanta luminosita' si ha bisogno nell'ora indicata e con la luminosita' indicata
     */
    public static double calculateBrightFactor(int hour, int minutes, double sensorBright, double minBrightness) {
        hour = hour<0? 0:hour>23? 23:hour;
        minutes = minutes<0? 0:minutes>59? 59:minutes;
        minBrightness = minBrightness<0? 0:minBrightness>100? 100:minBrightness;
        sensorBright = sensorBright<0? 0:sensorBright>100? 100:sensorBright;

        // Valore compreso tra -1(poca luminosita') e 1(molta luminosita')
        sensorBright = sensorBright/100;
        minBrightness = 0.5*Math.abs(1-minBrightness/100);

        // Puo' avere un valore compreso tra 1(mezzanotte) e 0(mezzogiorno) => il valore minimo(0) puo' aumentare grazie a minBrightness)
        final double maxIntensity = minBrightness*Math.cos((2*Math.PI*(hour + (minutes/60.0)) /24)) + (1-minBrightness);

        return maxIntensity-sensorBright;
    }

    /**
     * Restuisce un thread che se fatto partire, esegue il runnable in un sub-thread all'ora indicata ogni giorno<br>
     * Il sotto thread lanciato avra' lo stesso nome, ma con un trattino e la data di lancio a seguito<br>
     * Se il thread viene interrotto non fa piu' partire il runnable e si ferma
     * @param runnable il runnable da lanciare
     * @param hour l'ora a cui far partire il thread, se negativa o maggiore di 23 ritorna null
     * @param threadName il nome da dare al thread
     */
    public static Thread getThreadStartingAt(final Runnable runnable, final int hour, String threadName) {
        if(hour<0 || hour>23)
            return null;

        return new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                boolean notInterrupted = true;
                Calendar calendar = Calendar.getInstance();
                do {
                    try {
                        double hourCalculated = calendar.get(Calendar.HOUR_OF_DAY)+((double)calendar.get(Calendar.MINUTE)/60);
                        double hourToWait = hour-hourCalculated;
                        if(hourToWait<0)
                            hourToWait += 24;

                        wait((long)(hourToWait * 60 * MILLISEC_IN_MINUTE));
                        Thread thread = new Thread(runnable, threadName + "-" + new Timestamp(System.currentTimeMillis()));
                        thread.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        notInterrupted = false;
                    }
                } while (notInterrupted);
            }
        });
    }

    /**
     * Restuisce un thread che se fatto partire, esegue il runnable in un sub-thread ogni X minuti<br>
     * Il sotto thread lanciato avra' lo stesso nome, ma con un trattino e la data di lancio a seguito<br>
     * Se il thread viene interrotto non fa piu' partire il runnable e si ferma
     * @param runnable il runnable da lanciare
     * @param minutes i minuti da aspettare, se negativi o 0 ritorna null
     * @param threadName il nome da dare al thread
     */
    public static Thread getThreadStartingEach(final Runnable runnable, final int minutes, String threadName) {
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
