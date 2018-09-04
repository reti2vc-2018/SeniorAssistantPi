package main;

import device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.Database;

/**
 * Created by 20015159 on 28/08/2018.
 */
public class Main {

    /**
     * Un Logger per capire meglio quali pezzi vengono eseguiti e quali no.
     */
    private static final Logger LOG = LoggerFactory.getLogger("SeniorAssistant");

    private static Hue lights;
    private static Fitbit fitbit;
    private static Sensor sensor;

    /**
     * Funzione principale, qui si  creano tutte le classi che verranno utilizzate.
     * @param args per ora nulla, ma forse in futuro si potrebbe mettere roba
     */
    public static void main(String[] args) {

        LOG.info("Connecting to hue lights");
        lights = new Hue();

        LOG.info("Connecting to the sensors");
        sensor = new Sensor(2);

        try {
            LOG.info("Connecting to Fitbit");
            fitbit = new Fitbit();

            startInsertData();
            // add here functions associated with fitbit
        } catch (Exception e) { // in this way the program will continue even without fitbit
            e.printStackTrace();
        }

        startSensorLog(10);
        startWebhook();
    }

    /**
     * Fa partire il server Webhook per DialogFlow e continua l'esecuzione
     */
    private static void startWebhook() {
        DialogFlowWebHook df = new DialogFlowWebHook();

        df.addOnAction("LightsON", (params) -> {lights.turnOn(); return null;});
        df.addOnAction("LightsOFF", (params) -> {lights.turnOff(); return null;});
        df.addOnAction("ColorLoop", (params) -> {lights.colorLoop(); return null;});
        df.addOnAction("ChangeColor", (params) -> {lights.changeColor(params.get("color").getAsString()); return null;});
        df.addOnAction("SetLights", (params) -> {lights.setBrightness(params.get("intensity").getAsInt()); return null;});
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

        LOG.info("Starting Webhook");
        df.startServer();
    }

    /**
     * Gestione DB in modo che si aggiorni ogni ora
     */
    private static void startInsertData() {
        try {
            Database database = Database.getInstance();

            Thread hourlyData = new Thread(database.insertHourlyData(fitbit), "updating-hour-data");
            Thread dailyData = new Thread(database.insertDailyData(fitbit), "updating-day-data");

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
    private static void startSensorLog(int seconds) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                boolean notInterrupted = true;
                while(notInterrupted) {
                    try {
                        sensor.update((seconds/2) * 1000);
                        LOG.info("Luminosità rilevata: " + sensor.getBrightnessLevel());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notInterrupted = false;
                    }
                }
            }
        }, "sensor");

        thread.start();
    }

    /*
    TODO AUTOMATIC: {B}, {C}, {D}, {E}

    XXX Gestione DB in modo che si aggiorni ogni ora
    /B/ Gestione luci in modo che la luminosità sia sempre la stessa
    /C/ Gestione luci a seconda del battito cardiaco
    /D/ Ad una certa ora guarda i passi e se sono pochi dillo
    /E/ Se i battiti sono troppo bassi/alti avvisare il tizio

    TODO USER-INTERACTION {A}, {C}

    /A/ Dati del sonno/battito/passi che l'utente puo' richiedere
    XXX Gestione luci secondo le esigenze dell'utente ( settare Dialogflow e server + risolvere bug )
    /C/ EXTRA Gestione musica tramite comando vocale

    // Randomly at night heavy metal start
    */
}
