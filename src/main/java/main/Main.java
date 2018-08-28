package main;

import device.*;

/**
 * Created by 20015159 on 28/08/2018.
 */
public class Main {

    public static void main(String[] args) {
        DialogFlowWebHook df = new DialogFlowWebHook();

        df.addOnAction("LightsON", () -> {return "Luci accese";});
        df.addOnAction("LightsOFF", () -> {return "Luci spente";});

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
    private void startDb(Fitbit fibit) {
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
