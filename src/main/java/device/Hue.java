package device;

import java.util.Map;
import java.util.Set;

import support.Rest;

/**
 * Classe che permette di controllare le luci Philips Hue
 */
public class Hue {
    public static final int MAX_BRIGHTNESS = 255;
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

    private int brightness = 0;

    public Hue () {
        this("http://172.30.1.138/api/C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx/lights/");
    }
    
    /**
     * Inizializza la classe cercando tutte le luci all'indirizzo url specificato
     * 
     * @param url l'indirizzo da inserire delle luci Hue
     */
    public Hue (String url) {
        lightsURL = url;
        allLights = Rest.get(lightsURL);
        // Todo brightness initial
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
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : true }";
            Rest.put(callURL, body, "application/json");
        }
    }

    /**
     * Spegne tutte le luci controllate
     */
    public void turnOff() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : false }";
            Rest.put(callURL, body, "application/json");
        }
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
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"bri\" : "+num+" }";
            Rest.put(callURL, body, "application/json");
        }
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

    /**
     * Modifica il colore delle luci in modo da fare un bel effetto arcobaleno continuo
     */
    public void colorLoop() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : true, \"effect\" : \"colorloop\" }";
            Rest.put(callURL, body, "application/json");
        }
    }

    /**
     * Funzione generale per poter utilizzare qualunque valore, ma non funziona
     */
    /*public void setAttribute(String attribute, String value){
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \""+attribute+"\" : "+value+" }";
            Rest.put(callURL, body, "application/json");
        }
    }*/
}
