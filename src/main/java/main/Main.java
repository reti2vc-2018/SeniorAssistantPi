package main;

import device.*;
import device.fitbitdata.HeartRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.Musich;
import support.database.Database;
import support.database.LocalDB;
import support.database.RemoteDB;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 20015159 on 28/08/2018.
 * Ci si puo' interfacciare con l'assistente tramite Telegram o dal sito di ngrok.
 */
public class Main {

    /**
     * Un Logger per capire meglio quali pezzi vengono eseguiti e quali no.
     */
    private static final Logger LOG = LoggerFactory.getLogger("SeniorAssistant");

    private static Hue lights = null;
    private static Fitbit fitbit = null;
    private static Sensor sensor = null;
    private static Database database = null;
    private static Musich musich = new Musich();

    /**
     * Funzione principale, qui si  creano tutte le classi che verranno utilizzate.<br>
     * Si possono passare dei parametri usando -(nome parametro)::(valore parametro)<br>
     * Ogni parametro deve esser separato da uno o piu spazi<br>
     * Parametri possibili:<br>
     * <ul>
     *     <li>hueAddress</li>
     *     <li>hueUser</li>
     *     <li>sensorNode</li>
     *     <li>sensorLog</li>
     *     <li>remoteDbUser</li>
     * </ul>
     * @param args
     */
    public static void main(String[] args) {
        System.getProperty("sun.arch.data.model");
        Map<String, String> arguments = getArgsMap(args);

        // list of arguments to use in the classes
        String hueAddress = arguments.get("hueAddress");
        String hueUser = arguments.get("hueUser");
        Integer sensorLog = getInt(arguments.get("sensorLog"));
        Integer sensorNode = getInt(arguments.get("sensorNode"));
        String remoteDbUser = arguments.get("remoteDbUser");

        try {
            LOG.info("Connessione alle Philips Hue...");
            lights = new Hue(hueAddress, hueUser);

            try {
                LOG.info("Connessione ai sensori...");
                sensor = new Sensor(sensorNode);

                if(sensorLog>0)
                    startSensorLog(sensorLog);
                startHueAutoBrightness();
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }

            try {
                LOG.info("Connessione al Fitbit, ignorare eventuale errore per setPermissionsToOwnerOnly...");
                fitbit = new Fitbit();

                LOG.info("Connessione al database...");
                database = remoteDbUser==null? new LocalDB():new RemoteDB(remoteDbUser);

                startInsertData();
                startCheckSteps();
                startHueControlledByHeartBeat();
            } catch (Exception e) {
                LOG.warn("Non e' stato possibile collegarsi al fitbit");
                e.printStackTrace();
            }

            startWebhook();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        LOG.info("FINE MAIN");
    }

    /**
     * Fa partire il server Webhook per DialogFlow e continua l'esecuzione
     */
    private static void startWebhook() {
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

        LOG.info("Starting Webhook");
        df.startServer();
    }

    /**
     * Gestione DB in modo che si aggiorni ogni ora
     */
    private static void startInsertData() {
        try {
            Thread hourlyData = Database.insertHourlyDataIn(database, fitbit, 5);
            Thread dailyData = Database.insertDailyData(database, fitbit, 5);

            LOG.info("Starting threads for automatic update");
            hourlyData.start();
            dailyData.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * funzione che logga periodicalmente i valori ricevuti dal sensore
     */
    private static void startSensorLog(int minutes) {
        LOG.info("Starting thread for logging sensor data");

        Runnable runnable = new Runnable() {
            @Override
            public synchronized void run() {
                sensor.update(0);
                LOG.info("Luminosita' rilevata: " + sensor.getBrightnessLevel());
            }
        };

        Database.getThreadStartingEach(runnable, "sensor", minutes).start();
    }

    // TODO AUTO:{B} Gestione luci in modo che la luminosità sia sempre la stessa
    private static void startHueAutoBrightness() {
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

    private static void startHueControlledByHeartBeat() {
        LOG.info("Starting thread lights for heartbeat");
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

        Database.getThreadStartingEach(runnable, "lights-with-heartbeat", minutes).start();
    }

    // TODO AUTO:{D} Ad una certa ora guarda i passi e se sono pochi dillo
    private static void startCheckSteps() {
        // trovare un orario (magari inserirlo tramite arg)
        // a quell'ora controllare i passi fatti durante la giornata
        // se pochi mandare un avviso tramite dialogFlow(?)
        // (secondo me si puo' evitare)
    }

    /*
    TODO AUTOMATIC: {B}, {D}

    XXX Gestione DB in modo che si aggiorni ogni ora
    /B/ Gestione luci in modo che la luminosità sia sempre la stessa
    XXX Gestione luci a seconda del battito cardiaco
    /D/ Ad una certa ora guarda i passi e se sono pochi dillo
    XXX Se i battiti sono troppo bassi/alti avvisare il tizio

    TODO USER-INTERACTION {A}, {C}

    /A/ Dati del sonno/battito/passi che l'utente puo' richiedere
    XXX Gestione luci secondo le esigenze dell'utente ( settare Dialogflow e server + risolvere bug )
    /C/ Gestione musica tramite comando vocale

    // Randomly at night heavy metal start
    */

    /* ------------------------------------------------------------------------------------
            Le funzioni qui sotto servono solamente per gli argomenti passati al main
       ------------------------------------------------------------------------------------ */

    /**
     * Prende gli argomenti nel formato "-(.+)::(.+)" e li inserisce in una mappa.
     * Se l'argomento non e' nel formato giusto lo ignora.
     * @param args un'array di stringhe contente i vari argomenti
     * @return una mappa con key il nome dell'argomento (la parte prima del :: e dopo il meno) e come valore il valore di esso (la parte dopo ::)
     */
    private static Map<String, String> getArgsMap(String[] args) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("-(.+)::(.+)");

        for (String arg: args) {
            Matcher matcher = pattern.matcher(arg);
            if (matcher.find())
                map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    /**
     * Funzione creata per gli argomenti che vengono passati in modo da evitare millemila try and catch
     * @param num la stringa da trasformare in numero
     * @return il numero trasformato, null se fallisce
     */
    private static Integer getInt(String num) {
        Integer returnNum = null;
        try { returnNum = Integer.parseInt(num); } catch (Exception e) {}
        return returnNum;
    }
}
