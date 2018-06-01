import device.Hue;
import device.Sensor;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestSensor {
    Sensor sensor = new Sensor();
    Hue hue = new Hue();

    @Test
    public void firstTestSensor() {
        assertTrue(sensor.IsLowLuminescence(50));
    }

    @Test
    public void secondTestSensor() {
        if(sensor.IsLowLuminescence(50)) {
            hue.turnOn();
            hue.setBrightness(200);
        }
    }
}
