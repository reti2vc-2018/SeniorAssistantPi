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

    public void looping() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : true, \"effect\" : \"colorloop\" }";
            rest.put(callURL, body, "application/json");
        }
    }


}
