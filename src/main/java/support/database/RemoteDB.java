package support.database;

import ai.api.GsonFactory;
import com.google.gson.Gson;
import device.fitbitdata.HeartRate;
import device.fitbitdata.Steps;
import support.Rest;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe che si connette al server del progetto di C#
 */
public class RemoteDB implements Database {

    /**
     * Serve per mandare i messaggi, convertendo le classi in Json
     */
    private static final Gson GSON = GsonFactory.getDefaultFactory().getGson();

    private static final DateFormat STD_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH");

    /**
     * L'url base che verra' usato per inviare/ricevere i dati
     */
    public final String base_url;
    /**
     * L'username dell'utente
     */
    public final String username;

    /**
     * Inseredo lo username e basta l'indirizzo a cui tentera' la connesione e' <a href="http://127.0.0.1:5000/api/">http://127.0.0.1:5000/api/</a>
     * @param username il nome utente assiociato per aggiornare i dati
     */
    public RemoteDB(String username) {
        this(username, "http://127.0.0.1:5000/api/");
    }

    /**
     * Costruttore che ha bisogno sia dello username che dell'url per la connesione
     * @param username il nome utente assiociato per aggiornare i dati
     * @param base_url l'url a cui si punta per aggiornare/leggere i dati
     */
    public RemoteDB(String username, String base_url) {
        this.username = username;
        this.base_url = base_url;
    }

    @Override
    public boolean isReachable() {
        return !Rest.get(base_url+"user/"+username).isEmpty();
    }

    @Override
    public boolean updateHeart(long dateMilliSec, double heartRate) {
        return sendData("heartbeat", dateMilliSec, heartRate);
    }

    @Override
    public boolean updateSleep(long dateStartSleep, long duration) {
        return sendData("sleep", dateStartSleep, duration);
    }

    @Override
    public boolean updateSteps(long dateMilliSec, long steps) {
        return sendData("step", dateMilliSec, steps);
    }

    @Override
    public List<HeartRate> getHeartDataOfLast(int days) {
        try {
            String url = base_url+"heartbeat/"+username+"/last/"+(days*24);
            Map<String, List<Map<String, Object>>> map = (Map<String, List<Map<String, Object>>>) Rest.get(url);

            List<HeartRate> list = new ArrayList<>(map.get("list").size());
            for(Map<String, Object> data: map.get("list")) {
                HeartRate heart = new HeartRate();
                heart.setAverage((double)data.get("value"));
                heart.setDate(STD_FORMAT.parse((String)data.get("time")).getTime());

                list.add(heart);
            }
            return list;
        } catch (ParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    @Override
    public List<Steps> getStepDataOfLast(int days) {
        try {
            String url = base_url+"step/"+username+"/last/"+(days*24);
            Map<String, List<Map<String, Object>>> map = (Map<String, List<Map<String, Object>>>) Rest.get(url);

            List<Steps> list = new ArrayList<>(map.get("list").size());
            for(Map<String, Object> data: map.get("list")) {
                Steps steps = new Steps();
                steps.setSteps((int)data.get("value"));
                steps.setDate(STD_FORMAT.parse((String)data.get("time")).getTime());

                list.add(steps);
            }
            return list;
        } catch (ParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    /**
     * Serve ad inviare una richiesta PUT per aggiornare i dati
     * @param type il tipo di dato che deve esser aggiornato
     * @param date la data da inserire in millisecondi
     * @param value l'oggetto da inviare
     * @return vero se dopo la PUT si e' riusciti ad inserire il valore
     */
    private boolean sendData(String type, long date, Object value) {
        String url = base_url+type+"/";
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("time", new Timestamp(date));
        map.put("value", value);

        Rest.put(url, GSON.toJson(map), "application/json");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd/HH");

        url = url+username+"/"+format.format(new Date(date));
        List<Map<String, Object>> list = (List<Map<String, Object>>) Rest.get(url).get("list");
        for(Map<String, Object> obj: list)
            try {
                if (STD_FORMAT.parse((String) obj.get("time")).getTime() == date)
                    return true;
            } catch (Exception e) {}
        return false;
    }

}
