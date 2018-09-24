package test;

import org.junit.Test;
import support.audio.AudioFile;
import support.audio.Musich;

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

    @Test
    public void test2() {
        AudioFile audio = new AudioFile();
        audio.play("Godzilla.wav");
        waitAndPrint(3);
        audio.play("Tullio.wav");
        waitAndPrint(10);
        audio.stop();
        waitAndPrint(2);
        audio.play("LeeroyJenkins.wav");
        waitAndPrint(5);
        audio.playRandom("random");
        waitAndPrint(10);
        audio.stop();
    }

    @Test
    public void test3() {
        AudioFile audio = new AudioFile();
        AudioFile audio2 = new AudioFile();
        audio.play("Tullio.wav");
        waitAndPrint(3);
        audio2.play("Tullio.wav");
        waitAndPrint(10);
        audio.stop();
        audio2.stop();
    }

    @Test
    public void test4() {
        AudioFile audio = new AudioFile();

        new AudioFile("pochi passi.wav");
        waitAndPrint(3);
        audio.play("molti battiti.wav");
        waitAndPrint(3);
        audio.stop();
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
