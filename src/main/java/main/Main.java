package main;

import ai.api.GsonFactory;
import device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.Database;

import java.io.IOException;

// todo docs
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

    private static Database database;

    /**
     * Funzione principale, qui si  creano tutte le classi che verranno utilizzate.
     * @param args per ora nulla, ma forse in futuro si potrebbe mettere roba
     */
    public static void main(String[] args) {
        LOG.info("Connecting to hue lights");
        lights = new Hue("localhost:8090", "newdeveloper");

        LOG.info("Connecting to the sensors");
        sensor = new Sensor();

        try {
            LOG.info("Connecting to Database");
            database = Database.getInstance();

            LOG.info("Connecting to Fitbit");
            fitbit = new Fitbit();

            startInsertData();
            // add here functions associated with fitbit
        } catch (Exception e) { // in this way the program will continue even without fitbit
            e.printStackTrace();
        }

        startWebhook();
    }

    /**
     * Fa partire il server Webhook per DialogFlow e continua l'esecuzione
     */
    private static void startWebhook() {
        LOG.info("Adding actions to Webhook");
        DialogFlowWebHook df = new DialogFlowWebHook();

        df.addOnAction("LightsON", (params) -> {lights.turnOn(); return null;});
        df.addOnAction("LightsOFF", (params) -> {lights.turnOff(); return null;});
        df.addOnAction("ColorLoop", (params) -> {lights.colorLoop(); return null;});
        df.addOnAction("ChangeColor", (params) -> {lights.changeColor(params.get("color").getAsString()); return null;});

        LOG.info("Starting Webhook");
        df.startServer();
    }

    /**
     * Gestione DB in modo che si aggiorni ogni ora
     */
    private static void startInsertData() {
        LOG.info("Connecting to DB to write fitbit data periodically");
        try {
            fitbit.getHoursSleep();

            Database database = Database.getInstance();

            Thread hourlyData = new Thread(database.insertHourlyData(fitbit), "updating-hour-data");
            Thread dailyData = new Thread(database.insertDailyData(fitbit), "updating-day-data");

            hourlyData.start();
            dailyData.start();
            LOG.info("Threads started for updating database");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public static void main(String[] args) throws Exception {
        Fitbit fitbit = new Fitbit();
        Sensor sensor = new Sensor();
        Hue hue = new Hue();

        while(true) {
            double heart = fitbit.getHeartRate();
            int brightness = sensor.getBrightnessLevel();

            // AUTOMATIC
            /X/ Gestione DB in modo che si aggiorni ogni ora
            // Gestione luci in modo che la luminosità sia sempre la stessa
            // Gestione luci a seconda del battito cardiaco
            // Ad una certa ora guarda i passi e se sono pochi dillo
            // Se i battiti sono troppo bassi/alti avvisare il tizio

            // USER-INTERACTION
            // Dati del sonno/battito/passi che l'utente puo' richiedere
            /X/ Gestione luci secondo le esigenze dell'utente ( settare Dialogflow e server + risolvere bug )
            // EXTRA Gestione musica tramite comando vocale

            // Randomly at night heavy metal start
        }
    }
    */
}
