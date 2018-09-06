package test;

import ai.api.GsonFactory;
import device.Hue;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TestLights {

    public static final int TIMEOUT = 200;

    @Test
    synchronized public void firstTestLights() throws InterruptedException {
        Hue lights = new Hue();

        Set<String> toRemove = new HashSet<>();
        for(String str: lights.getNameLights())
            if(!(Integer.parseInt(str)%2 == 0))
                toRemove.add(str);
        lights.removeLights(toRemove);

        for(int i=0; i<10; i++) {
            lights.turnOn();
            this.wait(TIMEOUT);
            lights.turnOff();
            this.wait(TIMEOUT);
        }

        lights.turnOn();
        for(int i=Hue.MAX_BRIGHTNESS; i>0; i-=10) {
            lights.setBrightness(i);
            this.wait(TIMEOUT);
        }

        for(int i=0; i<Hue.MAX_BRIGHTNESS; i+=10) {
            lights.setBrightness(i);
            this.wait(TIMEOUT);
        }

        lights.setBrightness(Hue.MAX_BRIGHTNESS);
        lights.colorLoop();
        this.wait(TIMEOUT*10);

        // change colors
        for (int i=0; i<=360; i++) {
            double radian = (0.0174533*i);
            double x = Math.cos(radian);
            double y = Math.sin(radian);
            lights.setState("xy", GsonFactory.getDefaultFactory().getGson().toJson(new Double[]{x, y}));
            this.wait(TIMEOUT);
        }

    }

}
