package test;

import org.junit.Test;
import support.Musich;

public class TestMusich {

    @Test
    public void test() {
        Musich musich = new Musich();
        musich.playRandom("fairy tail motivational soundtrack", 10);
        waitAndPrint(20);
        musich.play("X9di06iCmuw", 114);
        waitAndPrint(60);
        musich.stop();
        waitAndPrint(10);
        musich.stop();
    }

    public void waitAndPrint(Integer seconds) {
        if(seconds != null) synchronized (seconds) {
            try {
                for(int i=seconds; i>0; i--) {
                    System.out.println("Tempo rimanente: " + i);
                    seconds.wait(1000); // 1 sec
                }
                System.out.println("Finito");

            } catch (Exception e) {
                System.out.println("INTERRUPTED " + e.getMessage());
            }
        }
    }
}
