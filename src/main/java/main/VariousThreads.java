package main;

import device.DialogFlowWebHook;
import device.Fitbit;
import device.Hue;
import device.Sensor;
import device.fitbitdata.HeartRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.Musich;
import support.database.Database;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

public class VariousThreads {

    /**
     * Un Logger per capire meglio quali pezzi vengono eseguiti e quali no.
     */
    public static final Logger LOG = LoggerFactory.getLogger("SeniorAssistant");

    /**
     * Una costante che indica quanti millisecondi ci sono in un minuto (utile per le conversioni)
     */
    public static final int MILLISEC_IN_MINUTE = 60000;


    /**
     * La variabile per far partire della musica da Youtube
     */
    private final Musich musich;

    /**
     * Costruttore
     */
    public VariousThreads() {
        musich = new Musich();
    }

    // TODO aggingere il fitbit per la richiesta dei dati
    /**
     * Fa partire il server Webhook per DialogFlow e continua l'esecuzione
     * @param lights le luci che deve controllare
     */
    public void startWebhook(final Hue lights) {
        DialogFlowWebHook df = new DialogFlowWebHook();

        df.addOnAction("LightsON", (params) -> { lights.turnOn(); return null; });
        df.addOnAction("LightsOFF", (params) -> { lights.turnOff(); return null; });
        df.addOnAction("ColorLoop", (params) -> { lights.colorLoop(); return null; });
        df.addOnAction("ChangeColor", (params) -> {
            lights.changeColor(params.get("color").getAsString());
            return null;
        });
        df.addOnAction("SetLights", (params) -> {
            lights.setBrightness(params.get("intensity").getAsInt());
            return null;
        });
        df.addOnAction("LightsDOWN", (params) -> {
            if(params.get("intensity").getAsString().equals(""))
                lights.decreaseBrightness();
            else
                lights.decreaseBrightness(params.get("intensity").getAsInt());
            return null;
        });
        df.addOnAction("LightsUP", (params) -> {
            if(params.get("intensity").getAsString().equals(""))
                lights.increaseBrightness();
            else
                lights.increaseBrightness(params.get("intensity").getAsInt());
            return null;
        });
        df.addOnAction("SetMusic", (param) -> {
            musich.playRandom(param.get("musicType").getAsString(),10);
            return null;
        });
        df.addOnAction("StopMusic", (params) -> { musich.stop(); return null; });

        //TODO aggiungere una azione che faccia in modo di richiedere dei dati in particolare
        //TODO aggiungere una azione su DialogFlow che riconosca di impostare una playlist (Rilassante, Antica...)

        df.startServer();
        LOG.info("Webhook partito");
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
            LOG.info("Thread per gli aggiornamenti automatici partiti");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO fare auto-brightness
    /**
     * Gestione delle luci in modo che cambiano la luminosita' in base ai dati ricevuti dal sensore<br>
     * Se l'utente pero' cambia il valore delle luci di sua volonta', il processo non modifichera' le luci per almeno un'ora.
     * @param lights le luci da controllare
     * @param sensor i sensori da cui porendere i dati
     */
    public void startHueAutoBrightness(final Hue lights, final Sensor sensor) {
        // controllare la luminosita' arrivata dal sensore
        // trovare un valore di default per ogni ora
        // se troppo bassa alzare la luci di poco
        // se troppo alta abbassare le luci
        // se l'utente modifica la luminosita' delle luci allora non fare nulla per almeno 20/30 minuti o di piu
        /*
        Thread thread = new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                boolean notInterrupted = true;
                Calendar calendar = Calendar.getInstance();
                while(notInterrupted) {
                    int bright = sensor.getBrightnessLevel();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);

                    if(hour >= 21 && hour<7) {
                        lights.setBrightness(5);
                    }
                    else if(hour >= 19 && hour<21) {
                        lights.setBrightness(99);
                    }
                    else if(hour >= 7 && hour<19) {
                        lights.setBrightness(0);
                    }

                    try {
                        wait(120000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notInterrupted = false;
                    }

                    if(hour >= 21 && hour<7)
                        lights.setBrightness(5);
                    else if(bright >= 0 && bright <= 20)
                        lights.setBrightness(90);
                    else if(bright >= 21 && bright <= 40)
                        lights.setBrightness(60);
                    else if(bright >= 41 && bright <= 60)
                        lights.setBrightness(40);
                    else
                        lights.turnOff();

                }
            }
        }, "auto-brightness");

        thread.start();
        */
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
        Runnable runnable = new Runnable() {
            @Override
            public synchronized void run() {
                int sum=0;
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

                //TODO impostare azioni anche di {E}
                double rateNow = fitbit.getHeartRate(minutes);
                if ((rateNow-average) >= delta )
                    lights.decreaseBrightness();
                    //avvisare con una voce registrata?
                else if ((rateNow-average) <= -delta)
                    //alzare le luci?
                    //avvisare con una voce registrata?
                    ;
            }
        };

        getThreadStartingEach(runnable, minutes, "lights-with-heartbeat").start();
        LOG.info("Thread per il controllo delle luci tramite il battito cardiaco partito");
    }

    // TODO Ad una certa ora guarda i passi e se sono pochi dillo
    /**
     * Controlla che ad una certa ora si siano fatti abbastanza passi, ed in caso contrario avvisa tramite un messaggio vocale.<br>
     * E' possibile trasformarlo in controlla ogni X se si sono fatti dei movimenti o meno.
     * @param fitbit da dove vediamo se si sono fatti abbastanza passi
     */
    public void startCheckSteps(final Fitbit fitbit) {
        // trovare un orario (magari inserirlo tramite arg)
        // a quell'ora controllare i passi fatti durante la giornata
        // se pochi mandare un avviso tramite dialogFlow(?)
        // (secondo me si puo' evitare)
    }





    /**
     * Restuisce un thread che se fatto partire, esegue il runnable in un sub-thread ogni X minuti<br>
     * Il sotto thread lanciato avra' lo stesso nome, ma con un trattino e la data di lancio a seguito<br>
     * Se il thread viene interrotto non fa piu' partire il runnable e si ferma
     * @param runnable il runnable da lanciare
     * @param minutes i minuti da aspettare, se negativi o 0 ritorna null
     * @param threadName il nome da dare al thread
     */
    public static Thread getThreadStartingEach(final Runnable runnable, int minutes, String threadName) {
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
