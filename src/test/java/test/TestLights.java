package test;

import device.Hue;
import main.VariousThreads;
import org.junit.Before;
import org.junit.Test;

public class TestLights {

    private static final int TIMEOUT = 200;
    private static final int MAX = 100;
    private Hue lights;

    @Before
    public void init() {
        lights = new Hue();
        lights.turnOn();
        lights.setBrightness(100);
        lights.changeColor("bianco");
    }

    @Test
    synchronized public void firstTestLights() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            lights.turnOn();
            this.wait(TIMEOUT);
            lights.turnOff();
            this.wait(TIMEOUT);
        }
    }

    @Test
    synchronized public void testBrightness() throws InterruptedException {
        for (int i = MAX; i > 0; i -= 10) {
            lights.setBrightness(i);
            this.wait(TIMEOUT);
        }

        for (int i = 0; i < MAX; i += 10) {
            lights.setBrightness(i);
            this.wait(TIMEOUT);
        }
    }

    @Test
    synchronized public void testColorLoop() throws InterruptedException {
        lights.colorLoop();
        this.wait(TIMEOUT * 10);
    }

    @Test
    synchronized public void testColor() throws InterruptedException {
        String[] colors = {"rosso", "giallo", "verde", "blu", "bianco", "azzurro", "arancio"};

        for (String color : colors) {
            lights.changeColor(color);
            this.wait(TIMEOUT);
        }
    }

    @Test
    synchronized public void testAutoBright() throws InterruptedException {
        lights.setBrightness(MAX);

        for(int hour=0; hour<24; hour++)
            for(int minutes=0; minutes<60; minutes++) {
                final double hueBrightnes = lights.getCurrentBrightness();
                final double brightFactor = VariousThreads.calculateBrightFactor(hour, minutes, 100, 20);
                final double bright = brightFactor*100;

                System.out.printf("%2d:%02d: %+.3f {bri:%3.0f -> add:%+4.0f}\n", hour, minutes, brightFactor, hueBrightnes, bright);
                lights.addBrightness(bright);
                wait(TIMEOUT);
            }
    }

}
