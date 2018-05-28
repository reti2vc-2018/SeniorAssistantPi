package device;

import java.util.Map;
import manage.Rest;

public class Hue {
    //private String baseURL;// = "192.168.0.2";
    //private String username;// = "admin";
    private String lightsURL;// = baseURL+"/api/"+username+"/lights/";

    private Map<String, ?> allLights;

    public Hue () {
        this("192.168.0.2/api/admin/lights/");
    }
    public Hue (String url) {
        lightsURL = url;
        allLights = Rest.get(lightsURL);
    }

    public void turnOn() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : true }";
            Rest.put(callURL, body, "application/json");
        }
    }

    public void turnOff() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : false }";
            Rest.put(callURL, body, "application/json");
        }
    }

    public void setBrightness(int num) {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"bri\" : "+num+" }";
            Rest.put(callURL, body, "application/json");
        }
    }

    public void setAttribute(String attribute, String value){
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \""+attribute+"\" : "+value+" }";
            Rest.put(callURL, body, "application/json");
        }
    }


}
