package support.audio;

/**
 * Classe che serve ad aiutare a far partire la musica
 */
public interface Audio {

    /**
     * Fa partire una audio in base al nome di essa.<br>
     * Se un audio era gia' stato fatto partire esso viene fermato<br>
     * Il nome puo' variare in base all'implementazione: nome di file o nome da cercare su internet...
     * @param name la stringa per far partire la canzone
     */
    void play(String name);

    /**
     * Fa' partire un audio a caso fra quelli selezionati dalla stringa<br>
     * Se un audio era gia' stato fatto partire esso viene fermato<br>
     * In base all'implementazione puo' essere un nome che appartiene a vari file,<br>
     * il nome di una cartella contenente i file, o una stringa di ricerca...
     */
    void playRandom(String name);

    /**
     * Ferma l'ultimo che e' stato fatto partire
     */
    void stop();
}
