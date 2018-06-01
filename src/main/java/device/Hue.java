package device;

import java.util.Map;
import java.util.Set;

import manage.Rest;

public class Hue {
    //private String baseURL = "192.168.0.2";
    //private String username = "C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx";
    private String lightsURL;// = baseURL+"/api/"+username+"/lights/";

    private Map<String, ?> allLights;

    public Hue () {
        this("http://172.30.1.138/api/C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx/lights/");
    }
    public Hue (String url) {
        lightsURL = url;
        allLights = Rest.get(lightsURL);
    }

    public Set<String> getNameLights() {
        return allLights.keySet();
    }

    public void removeLights(Set<String> toRemove) {
        for(String string : toRemove)
            allLights.remove(string);
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

    /*public void setAttribute(String attribute, String value){
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \""+attribute+"\" : "+value+" }";
            Rest.put(callURL, body, "application/json");
        }
    }*/

    public void colorLoop() {
        for (String light : allLights.keySet()) {
            String callURL = lightsURL + light + "/state";
            String body = "{ \"on\" : true, \"effect\" : \"colorloop\" }";
            Rest.put(callURL, body, "application/json");
        }
    }


}
