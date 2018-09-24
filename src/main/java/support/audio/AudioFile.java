package support.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Usa i file nella cartella resources/audio/ per riprodurre i suoni
 */
public class AudioFile implements Audio {

    /**
     * La path dove si trovano gli audio
     */
    public static final String PATH_AUDIO = "src/main/resources/audio/";

    /**
     * L'ultimo audio fatto partire
     */
    private final Clip clip;

    /**
     * Serve per crearsi una mappa di tutte le canzoni
     */
    private final static Map<String, File> files = getAllFiles(PATH_AUDIO);

    /**
     * Mappa che serve ad avere per ogni sotto-dir di audio una lista di ogni file audio che c'e'
     */
    private final static Map<String, List<File>> dirs = getAllDirs(PATH_AUDIO);

    /**
     * Crea un oggetto audio che si puo' poi far riprodurre e stoppare
     */
    public AudioFile() {
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        this.clip = clip;
    }

    /**
     * Utilizzando questo costruttore si fa partire in automatico il file scelto
     * @param file il nome del file scelto da far partire subito
     */
    public AudioFile(String file) {
        this();
        this.play(file);
    }

    /**
     * Fa partire una canzone che si trova nella cartella audio o in una delle sue sottocartelle
     * @param name la stringa per far partire la canzone
     */
    @Override
    public void play(String name) {
        stop();
        try {
            File file = files.get(name);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fa' partire una canzone a caso nella cartella selezionata
     * @param name il nome della cartella
     */
    @Override
    public void playRandom(String name) {
        List<File> songs = dirs.get(name);
        play(songs.get((int)(Math.random()*songs.size())).getName());
    }

    @Override
    public void stop() {
        try {
            clip.stop();
            clip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fa in modo di mappare tutti i file in una directory e in tutte le sue sub-dir
     * @param path la path iniziale
     * @return una mappa di NomeFile -> File
     */
    private static Map<String, File> getAllFiles(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        Map<String, File> map = new HashMap<>();

        for (File file : listOfFiles) {
            if(file.isFile())
                map.put(file.getName(), file);
            else
                map.putAll(getAllFiles(file.getPath()));
        }

        return map;
    }

    /**
     * Crea una mappa contenente tutti i file della cartella scelta associati con ogni dir corrente<br>
     * @param path la path iniziale
     * @return una mappa di directory con i loro file
     */
    private static Map<String, List<File>> getAllDirs(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        List<File> list = new LinkedList<>();
        Map<String, List<File>> map = new HashMap<>();

        for (File file : listOfFiles) {
            if(file.isFile())
                list.add(file);
            else
                map.putAll(getAllDirs(file.getPath()));
        }
        map.put(folder.getName(), list);

        return map;
    }
}
