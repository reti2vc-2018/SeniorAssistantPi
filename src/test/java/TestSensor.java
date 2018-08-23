import device.Hue;
import device.Sensor;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

 public class TestSensor {
    Sensor sensor = new Sensor(2);
     Hue lights;

    @Test
     synchronized public void firstTestSensor() throws InterruptedException {
        sensor.update(2);
        System.out.println(sensor.luminiscenceLevel());
    }

    @Test
    synchronized public void secondTestSensor() throws InterruptedException {
        int i=0;
        lights = new Hue();
        Set<String> toRemove = new HashSet<>();

        for(String str: lights.getNameLights())
            if(!str.equals("4"))
                toRemove.add(str);
        lights.removeLights(toRemove);
        lights.turnOn();

        while(i<999999) {
            if (sensor.luminiscenceLevel() < 200) {
                lights.getCurrentBrightness();
                lights.setBrightness(200);
            }

            if (sensor.luminiscenceLevel() > 600) {
                lights.setBrightness(0);
            }
            System.out.println(i+"-"+sensor.luminiscenceLevel());
            sensor.update(100);
            i++;
        }
    }
}
