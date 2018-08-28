package device;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.post;

/**
 * Classe per creare un Webhook che Dialog-Flow possa utilizzare per le sue azioni
 */
public class DialogFlowWebHook {

    /**
     * Un logger per vedere le cose piu' easy
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Errore che viene mostrato all'utente se l'azione inviata non corrisponde a nessuna di quelle inserite
     */
    public static final String ERROR = "Non mi hanno imparato abbastanza per fare questo";

    /**
     * L'eventuale path successiva all'url dichiarato nel Webhook di Dialog-Flow
     */
    public final String path;

    /**
     * Mappa che contiene tutte le azioni e il loro ID
     */
    private final Map<String, Action> actions;

    /**
     * Crea una classe vuota per un server che risponde lle chiamate di Dialog-Flow.
     * La path viene impostata di default a "/"
     */
	public DialogFlowWebHook() {
	    this("/");
    }

    /**
     * Crea una classe vuota per un server che risponde lle chiamate di Dialog-Flow.
     * @param path il percorso dopo l'url inidicato nel WebHook di Dialog-Flow
     */
    public DialogFlowWebHook(String path) {
	    this.path = path;
        this.actions = new HashMap<>();
    }

    /**
     * Aggiunge un'azione ad una specifica richiesta di Dialog-Flow
     * @param actionId il nome dell'azione che viene passata da Dialog-Flow
     * @param action l'azione da fare (usare lambda)
     */
	public void addOnAction(String actionId, Action action) {
        this.actions.put(actionId, action);
	}

    /**
     * Fa partire il server per accettare richieste da Dialog-Flow.
     * Ogni richiesta viene esaminata e fatta coincidere con una azione specificata precedentemente.
     * Se nessuna azione viene riscontrata, viene inviato un errore, rimuovendo i messaggi
     */
	public void startServer() { // todo add param port? Spark.port(num);
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        post(this.path, (request, response) -> {
            Fulfillment output = new Fulfillment();
            AIResponse input = gson.fromJson(request.body(), AIResponse.class);

            String text = null;
            try {
                log.info("AZIONE: "+input.getResult().getAction());
                Action action = actions.get(input.getResult().getAction());
                text = action.doAction();
            } catch (NullPointerException e) {
                log.info("NESSUNA AZIONE TROVATA");
                text = ERROR;
            }

            if(text != null) {
                log.info(text);
                output.setDisplayText(text);
                output.setSpeech(text);
            }

            response.type("application/json");
            return output;
        }, gson::toJson);
    }

    /**
     * Interfaccia usata per fare un'azione per ogni Id di Dialog-Flow
     */
    public interface Action {
        /**
         * Fai l'azione desiderata.
         * Se ritorna una stringa allora il testo viene cambiato. Se ritorna null non cambia il testo
         * @return Una stringa che verra' usata come messaggio o null se non si vuole
         */
	    String doAction();
    }
}
