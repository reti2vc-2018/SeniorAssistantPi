package test;

import device.Hue;
import device.Sensor;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

 public class TestSensor {
    private Sensor sensor = new Sensor(2);
     private Hue lights;

    @Test
     synchronized public void firstTestSensor() {
        System.out.println(sensor.getBrightnessLevel());
    }

    @Test
    synchronized public void secondTestSensor() {
        int i=0;
        lights = new Hue();
        Set<String> toRemove = new HashSet<>();

        for(String str: lights.getNameLights())
            if(!str.equals("4"))
                toRemove.add(str);
        lights.removeLights(toRemove);
        lights.turnOn();

        while(i<999999) {
            if (sensor.getBrightnessLevel() < 200) {
                lights.getCurrentBrightness();
                lights.setBrightness(200);
            }

            if (sensor.getBrightnessLevel() > 600) {
                lights.setBrightness(0);
            }
            System.out.println(i+"-"+sensor.getBrightnessLevel());
            i++;
        }
    }
}
