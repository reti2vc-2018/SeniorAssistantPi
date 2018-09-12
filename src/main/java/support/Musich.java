package support;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe che serve a far partire un video di youtube in una frame
 */
public class Musich {

    /**
     * L'url da dove possiamo pescare i dati di youtube dei video
     */
    private static final String API_URL = "https://www.googleapis.com/youtube/v3/search?";
    /**
     * La key necessaria per prendere i dati da youtube, (per ora abbiamo usato la key di Dawit di SeniorAssistant)<br>
     * Tra l'altro l'ho presa a caso... quindi boh.
     */
    private static final String KEY = "AIzaSyCtCK0EPR3k_hEEyar0PeY5v9E9UyTX4TM";

    /**
     * IL thread che ha fatto partire il frame corrente
     */
    private Thread currentThread;
    /**
     * Il frame (ovvero la windows) che e' attualmente attiva
     */
    private JFrame currentFrame;

    /**
     * Serve ad inizializzare la libreria SWT di eclipse e chrriis.dj
     */
    public Musich() {
        /*
        * Viene mandato questo errore in console, pero' boh... solo ogni tanto:
        * Exception "java.lang.ClassNotFoundException: com/intellij/codeInsight/editorActions/FoldingData"while constructing DataFlavor for: application/x-java-jvm-local-objectref; class=com.intellij.codeInsight.editorActions.FoldingData
        * A volte ne tira tante, a volte nessuna.
        * Sta' di fatto che per quanto ne so non compromette il funzionamento.
        * Al massimo usare un logger che dice di ignorare questo errore nel log
         */
        NativeInterface.initialize();
        NativeInterface.open();
        NativeInterface.runEventPump(); // this should be called at the end of main (it said this but who cares)
    }

    /**
     * Serve ad avere una lista di id dei video che corrispondono alle keyword indicate nella ricerca
     * @param search la ricerca da fare su youtube
     * @param maxResult quanti risultati si vuole avere
     * @return una lista di id dei video che corrispondono alla ricerca
     */
    public static List<String> getVideosId(String search, final int maxResult) {
        try {
            search = URLEncoder.encode(search, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, ?> response = Rest.get(API_URL +
                "maxResults=" + maxResult +
                "&part=snippet" +
                "&type=video" +
                "&q=" + search +
                "&key=" + KEY);

        List<Map<String, ?>> items = (List<Map<String, ?>>)response.get("items");
        List<String> videosId = new ArrayList<>(maxResult);

        for(Map<String, ?> obj: items) {
            Map<String, String> id = (Map<String, String>)obj.get("id");
            videosId.add(id.get("videoId"));
        }

        return videosId;
    }

    /**
     * Serve ad avere una lista di id dei video che corrispondono alle keyword indicate nella ricerca<br>
     * La lista conterra' i primi 5 id  dei video trovati.
     * @param search la ricerca da fare su youtube
     * @return una lista di id dei video che corrispondono alla ricerca
     */
    public List<String> getVideosId(String search) {
        return Musich.getVideosId(search, 5);
    }

    /**
     * Dato l'id di un video viene fatta partire un frame contenente il video richiesto
     * @param videoId l'id del video da far vedere
     * @param seconds da quanti secondi bisogna far partire il video
     */
    public void play(final String videoId, int seconds) {
        this.stop();
        currentThread = new Thread( () -> {
            currentFrame = new JFrame("YouTube Viewer");

            SwingUtilities.invokeLater(() -> {
                JWebBrowser browser = new JWebBrowser();
                browser.setBarsVisible(false);
                browser.navigate(getYoutubeLink(videoId, seconds));

                currentFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                currentFrame.getContentPane().add(browser, BorderLayout.CENTER);
                currentFrame.setSize(800, 600);
                currentFrame.setLocationByPlatform(true);
                currentFrame.setVisible(true);
            });

            // don't forget to properly close native components
            Runtime.getRuntime().addShutdownHook(new Thread(() -> NativeInterface.close()));
        }, "NativeInterface");

        currentThread.start();
    }

    /**
     * Dato l'id di un video viene fatta partire un frame contenente il video richiesto
     * @param videoId l'id del video da far vedere
     */
    public void play(final String videoId) {
        play(videoId, 0);
    }

    /**
     * Serve a far partire un video a caso tra quelli che corrispondono alle keyword indicate nella ricerca
     * @param search la ricerca da fare su youtube
     * @param maxResult fra quanti risultati deve scegliere
     */
    public void playRandom(String search, int maxResult) {
        this.play(getVideosId(search, maxResult).get((int)(Math.random()*maxResult)));
    }

    /**
     * Ferma il video che e' in riprduzione in questo momento.<br>
     * Se non ce ne sono, amen... non fa nulla.
     */
    public void stop() {
        if(currentThread != null) {
            currentThread.interrupt();
            currentFrame.dispatchEvent(new WindowEvent(currentFrame, WindowEvent.WINDOW_CLOSING));

            currentThread = null;
            currentFrame = null;
        }
    }

    /**
     * Ricevi il link di youtube del video a partire dal suo ID
     * @param videoId l'id del video
     * @param seconds i secondi dall'inizio del video (0 o negativi e viene ignorato)
     * @return una stringa
     */
    public static String getYoutubeLink(String videoId, int seconds) {
        return videoId==null? "":"https://www.youtube.com/watch?v=" + videoId + (seconds>0? "&t="+seconds:"");
    }
}
