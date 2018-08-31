package device;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ai.api.GsonFactory;
import support.Rest;

/**
 * Classe che permette di controllare le luci Philips Hue
 */
public class Hue {

    /**
     * La luminopsita' massima a cui si puo' arrivare
     */
    public static final int MAX_BRIGHTNESS = 254;

    /**
     * Una mappa che ad ogni colore (in lingua ita) assegna il proprio valore in hue
     */
    public static final Map<String, Double[]> COLORS = new HashMap<>();

    // todo set right colors
    static {
        COLORS.put("giallo", new Double[]{0.55, 0.45});
        COLORS.put("rosso", new Double[]{0.7, 0.25});
        COLORS.put("verde", new Double[]{0.15, 0.65});
        COLORS.put("blu", new Double[]{0.0, 0.0});
        COLORS.put("rosa", new Double[]{0.45, 0.15});
        COLORS.put("viola", new Double[]{0.25, .1});
        COLORS.put("azzurro", new Double[]{0.15, 0.25});
        COLORS.put("arancione", new Double[]{0.63, 0.35});
        //COLORS.put("nero", new Double[]{1.0, 1.0});
        COLORS.put("bianco", new Double[]{0.35, 0.3});
    }

    //private String baseURL = "192.168.0.2";
    //private String username = "C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx";
	/**
	 * L'url in cui si possono trovare le luci
	 */
    private String lightsURL; // = baseURL+"/api/"+username+"/lights/";

    /**
     * Tutte le luci che sono state registrate dall'url
     */
    private Map<String, ?> allLights;

    /**
     * L'ultima luminosita' impostata
     */
    private int brightness = 0;

    /**
     * Cerca le luci Philips Hue a ll'indirizzo <a href="http://172.30.1.138/api/C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx/lights/">http://172.30.1.138/api/C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx/lights/</a>
     */
    public Hue () {
        this("172.30.1.138", "C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx");
    }

    /**
     * Cerca le luci Philips Hue nell'indirizzo specificato e con l'utente specificato
     * @param ip l'indirizzo IP
     * @param user l'utente
     */
    public Hue(String ip, String user) {
        this("http://" + ip + "/api/" + user + "/lights/");
    }

    /**
     * Inizializza la classe cercando tutte le luci all'indirizzo url specificato
     * 
     * @param url l'indirizzo da inserire delle luci Hue
     */
    public Hue (String url) {
        lightsURL = url;
        allLights = Rest.get(lightsURL);
        // Todo brightness initial, maybe by default 50% or 75% of the total
    }

    /**
     * Ritorna un insieme contenente tutti i nomi delle luci che si sono trovate
     *
     * @return l'insieme dei nomi delle luci
     */
    public Set<String> getNameLights() {
        return allLights.keySet();
    }

    /**
     * Rimuove dal controllo tutte le luci che hanno il nome uguale ad uno contenuto nell'insieme passato
     * 
     * @param toRemove le luci da rimuovere
     */
    public void removeLights(Set<String> toRemove) {
        for(String string : toRemove)
            allLights.remove(string);
    }

    /**
     * Accende tutte le luci controllate
     */
    public void turnOn() {
        setState("on", "true");
    }

    /**
     * Spegne tutte le luci controllate
     */
    public void turnOff() {
        setState("on", "false");
    }
    
    /**
     * Ritorna la liminosita' attuale delle luci controllate
     * @return
     */
    public int getCurrentBrightness() {
        return brightness;
    }
    
    /**
     * Modifica la luminosita' delle luci a seconda del valore inserito
     * @param num la luminosita' che si vuole
     */
    public void setBrightness(int num) {
    	if (num<0)
    		num=0;
    	setState("bri", String.valueOf(num));
        brightness = num;
    }

    /**
     * Dinuisce la luminosita' delle luci controllate della percentuale che viene passata
     * @param percentage la percentuale di diminuzione
     */
    public void increaseBrightness(int percentage) {
        if (percentage<0)
            percentage = 0;
        else if (percentage>100)
            percentage = 100;
        setBrightness(brightness + (percentage*MAX_BRIGHTNESS)/100);
    }

    /**
     * Aumenta la luminosita' delle luci controllate del 10%
     */
    public void increaseBrightness() {
        increaseBrightness(10);
    }

    /**
     * Dinuisce la luminosita' delle luci controllate della percentuale che viene passata
     * @param percentage la percentuale di diminuzione
     */
    public void decreaseBrightness(int percentage) {
        if (percentage<0)
            percentage = 0;
        else if (percentage>100)
            percentage = 100;
        setBrightness(brightness - (percentage*MAX_BRIGHTNESS)/100);
    }

    /**
     * Dinuisce la luminosita' delle luci controllate del 10%
     */
    public void decreaseBrightness() {
        decreaseBrightness(10);
    }

    public void changeColor(String colorName) {
        String hueColor = GsonFactory.getDefaultFactory().getGson().toJson(COLORS.get(colorName));
        setState("xy", hueColor);
    }

    /**
     * Modifica il colore delle luci in modo da fare un bel effetto arcobaleno continuo
     */
    public void colorLoop() {
        setState("effect", "colorloop");
    }

    /**
     * Funzione generale per poter utilizzare qualunque valore, ma non funziona<br>
     * Da testare, visto che mi sembra strano che non funzi...
     * e invece funziona.
     */
    public void setState(String attribute, String value){
        for (String light : allLights.keySet())
            Rest.put(lightsURL + light + "/state",
                "{ \"" + attribute + "\" : " + value + " }",
                "application/json");
    }
}
