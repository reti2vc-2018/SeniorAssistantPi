package device;

import java.util.Map;
import manage.rest;

public class hue {
    String baseURL = "192.168.0.2";
    String username = "admin";
    String lightsURL = baseURL+"/api/"+username+"/lights/";

    Map<String, ?> allLights = rest.get(lightsURL);

    public void turnOn() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : true }";
            rest.put(callURL, body, "application/json");
        }
    }

    public void turnOff() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : false }";
            rest.put(callURL, body, "application/json");
        }
    }

    public void setBrightness(int num) {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"bri\" : "+num+" }";
            rest.put(callURL, body, "application/json");
        }
    }

    public void setAttribute(String attribute, String value){
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \""+attribute+"\" : "+value+" }";
            rest.put(callURL, body, "application/json");
        }
    }


}
