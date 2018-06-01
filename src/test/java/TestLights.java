import device.Hue;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TestLights {

    @Test
    synchronized public void firstTestLights() throws InterruptedException {
        Hue lights = new Hue();

        Set<String> toRemove = new HashSet<>();
        for(String str: lights.getNameLights())
            if(!str.equals("4"))
                toRemove.add(str);
        lights.removeLights(toRemove);

        for(int i=0; i<10; i++) {
            lights.turnOn();
            this.wait(0b11001000);  // 200
            lights.turnOff();
            this.wait(0b11001000);  // 200
        }

        lights.turnOn();
        for(int i=256; i>=0; i--) {
            lights.setBrightness(i);
            this.wait(50);
        }

        for(int i=0; i<256; i++) {
            lights.setBrightness(i);
            this.wait(50);
        }

        lights.colorLoop();
        this.wait(10000);   // 10 sec
        lights.turnOff();
    }

}
