import device.Hue;
import device.Sensor;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

 public class TestSensor {
    Sensor sensor = new Sensor(5);
    Hue lights = new Hue();

    @Test
     synchronized public void firstTestSensor() throws InterruptedException {
        sensor.update(5);
        System.out.println(sensor.luminiscenceLevel());
    }

    @Test
    public void secondTestSensor() throws InterruptedException {
        sensor.update(5);
        Set<String> toRemove = new HashSet<>();
        for(String str: lights.getNameLights())
            if(!str.equals("4"))
                toRemove.add(str);
        lights.removeLights(toRemove);

        if(sensor.luminiscenceLevel() < 100) {
            lights.turnOn();
            lights.setBrightness(200);
        }
    }
}
