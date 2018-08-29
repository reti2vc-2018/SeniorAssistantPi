package main;

import device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.DBConnect;

/**
 * Created by 20015159 on 28/08/2018.
 */
public class Main {

    /**
     * Un Logger per capire meglio quali pezzi vengono eseguiti e quali no.
     */
    private static Logger log = LoggerFactory.getLogger("SeniorAssistant");

    /**
     * Funzione principale, qui si  creano tutte le classi che verranno utilizzate.
     * @param args per ora nulla, ma forse in futuro si potrebbe mettere roba
     */
    public static void main(String[] args) {
        log.info("Connecting to hue lights");
        Hue lights = new Hue();
        log.info("Connecting to the sensors");
        Sensor sensor = new Sensor();

        try {
            log.info("Connecting to Fitbit");
            Fitbit fitbit = new Fitbit();
            startInsertData(fitbit);    // add here functions associated with fitbit
        } catch (Exception e) { // in this way the program will continue without fitbit
            e.printStackTrace();
        }

        startWebhook(lights);
    }

    /**
     * Fa partire il server Webhook per DialogFlow e continua l'esecuzione
     * @param hue Le luci che vengono modificate a seconda delle richieste dell'utente
     */
    private static void startWebhook(Hue hue) {
        log.info("Adding actions to Webhook");
        DialogFlowWebHook df = new DialogFlowWebHook();

        df.addOnAction("LightsON", () -> {hue.turnOn(); return "Luci accese";});
        df.addOnAction("LightsOFF", () -> {hue.turnOff(); return "Luci spente";});

        log.info("Starting Webhook");
        df.startServer();
    }

    /**
     * Cose da fare in questa funzione:
     * - far partire il database
     * - ogni ora aggiornare i dati del cuore. (Runnable che gira da se')
     * - alla fine della giornata fare un riepilogo del paziente (Runnable che gira da se')
     * (magari ci si calcola quando bisogna risvegliarsi e si mette un wait)
     * @param fibit da dove prende i dati
     */
    private static void startInsertData(Fitbit fibit) {
        log.info("Connecting to DB to write fitbit data periodically");
        /*
        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement("");

            ResultSet rs = st.executeQuery();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        */
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
            // Gestione DB in modo che si aggiorni ogni ora
            // Gestione luci in modo che la luminosità sia sempre la stessa
            // Gestione luci a seconda del battito cardiaco
            // Ad una certa ora guarda i passi e se sono pochi dillo
            // Se i battiti sono troppo bassi/alti avvisare il tizio

            // USER-INTERACTION
            // Dati del sonno/battito/passi che l'utente puo' richiedere
            // Gestione luci secondo le esigenze dell'utente
            // EXTRA Gestione musica tramite comando vocale

            // Randomly at night heavy metal start
        }
    }
    */
}
