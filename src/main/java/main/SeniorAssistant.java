package main;

import device.Fitbit;
import device.Hue;
import device.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.database.Database;
import support.database.LocalDB;
import support.database.RemoteDB;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 20015159 on 28/08/2018.
 * Ci si puo' interfacciare con l'assistente tramite Telegram o dal sito di ngrok.
 */
public class SeniorAssistant {

    /**
     * Un Logger per capire meglio quali pezzi vengono eseguiti e quali no.
     */
    public static final Logger LOG = LoggerFactory.getLogger("SeniorAssistant");

    /**
     * Funzione principale, qui si  creano tutte le classi che verranno utilizzate.<br>
     * Si possono passare dei parametri usando -(nome parametro)::(valore parametro)<br>
     * Ogni parametro deve esser separato da uno o piu spazi<br>
     * Parametri possibili:<br>
     * <ul>
     *     <li>hueAddress</li>
     *     <li>hueUser</li>
     *     <li>sensorAddress</li>
     *     <li>autoBrightness</li>
     *     <li>sensorNode</li>
     *     <li>remoteDbUser</li>
     * </ul>
     * @param args i possibili argomenti da passare al programma
     */
    public static void main(String[] args) {
        //TODO magari aggiungere un arg con la path per la musica in modo che si possa ampliare in futuro
        VariousThreads threads = new VariousThreads(); // this should be the first action of the main
        Map<String, String> arguments = getArgsMap(args);

        // list of arguments to use in the classes
        String hueAddress = arguments.get("hueaddress");
        String hueUser = arguments.get("hueuser");
        //TODO GIOVEDI String sensorAddress = arguments.get("sensorAddress");
        Integer sensorNode = getInt(arguments.get("sensornode"));
        String remoteDbUser = arguments.get("remotedbuser");
        boolean autoBrightness = arguments.containsKey("autobrightness");

        try {
            LOG.info("Connessione alle Philips Hue...");
            Hue lights = (hueAddress!=null && hueUser!=null? new Hue(hueAddress, hueUser):new Hue());

            if(autoBrightness) try {
                LOG.info("Connessione ai sensori...");
                Sensor sensor = new Sensor(sensorNode);

                threads.startHueAutoBrightness(lights, sensor);
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }

            // Lo dichiaro qui, cosi' anche se non ci si puo' collegare al fitbit si puo comunque comandare le luci
            Fitbit fitbit=null;
            try {
                LOG.info("Connessione al Fitbit, ignorare eventuale errore per setPermissionsToOwnerOnly...");
                fitbit = new Fitbit();

                LOG.info("Connessione al database...");
                Database database = remoteDbUser == null ? new LocalDB() : new RemoteDB(remoteDbUser);

                threads.startInsertData(database, fitbit);
                threads.startHueControlledByHeartBeat(lights, fitbit, database);
                threads.startCheckSteps(database);
            } catch (Exception e) {
                LOG.warn("Non e' stato possibile collegarsi al fitbit");
                e.printStackTrace();
            }

            threads.startWebhook(lights, fitbit);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        LOG.info("FINE MAIN");
    }

    /*
    AUTOMATIC:

    XXX Gestione DB in modo che si aggiorni ogni ora
    XXX Gestione luci in modo che la luminosità sia sempre la stessa
    XXX Gestione luci a seconda del battito cardiaco
    XXX Ogni X ore/minuti guarda i passi e se sono pochi dillo
    XXX Se i battiti sono troppo bassi/alti avvisare il tizio

    USER-INTERACTION:

    XXX Dati del sonno/battito/passi che l'utente puo' richiedere
    XXX Gestione luci secondo le esigenze dell'utente ( settare Dialogflow e server + risolvere bug )
    XXX Gestione musica tramite comando vocale //TODO inserire qualche canzone

    */

    /* ------------------------------------------------------------------------------------
            Le funzioni qui sotto servono solamente per gli argomenti passati al main
       ------------------------------------------------------------------------------------ */

    /**
     * Prende gli argomenti nel formato "^-(?&lt;name&gt;[a-zA-Z]+)(::)?(?&lt;argument&gt;.*)$" e li inserisce in una mappa.
     * Se l'argomento non e' nel formato giusto lo ignora.
     * @param args un'array di stringhe contente i vari argomenti
     * @return una mappa con key il nome dell'argomento (la parte prima del :: e dopo il meno) e come valore il valore di esso (la parte dopo ::)
     */
    private static Map<String, String> getArgsMap(String[] args) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("^-(?<name>[a-zA-Z]+)(::)?(?<argument>.*)$");

        for (String arg: args) {
            Matcher matcher = pattern.matcher(arg);
            if (matcher.find())
                map.put(matcher.group("name").toLowerCase(), matcher.group("argument"));
        }
        LOG.info(map.toString());
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
