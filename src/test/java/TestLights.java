import device.Hue;
import org.junit.Test;

public class TestLights {

    @Test
    synchronized public void firstTestLights() throws InterruptedException {
        Hue lights = new Hue("http://localhost/api/newdeveloper/");

        for(int i=0; i<10; i++) {
            lights.turnOn();
            this.wait(0b11001000);  // 200
            lights.turnOff();
            this.wait(0b11001000);  // 200
        }

        lights.turnOn();
        for(int i=0; i<256; i++) {
            lights.setBrightness(i);
            this.wait(50);
        }

        for(int i=256; i>=0; i--) {
            lights.setBrightness(i);
            this.wait(50);
        }

    }

}
