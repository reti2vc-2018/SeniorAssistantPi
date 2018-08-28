package main;

import device.*;

/**
 * Created by 20015159 on 28/08/2018.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Fitbit fitbit = new Fitbit();
        Sensor sensor = new Sensor();
        Hue hue = new Hue();

        while(true) {
            double heart = fitbit.getHeartRate();
            int brightness = sensor.getBrightnessLevel();

            // AUTOMATIC
            // Inserire ui dati nel DB ogni ora
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
}
