package device;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Classe per creare un Webhook che Dialog-Flow possa utilizzare per le sue azioni
 */
public class DialogFlowWebHook {

    /**
     * Un logger per vedere le cose piu' easy
     */
    private static final Logger LOG = LoggerFactory.getLogger(DialogFlowWebHook.class);

    /**
     * Stringa che viene usata se l'azione esiste ma lancia un qualche tipo di errore
     */
    public static final String ACTION_ERROR = "Purtroppo chi mi ha programmato e' un pirla, non posso fare cio' che hai chiesto";

    /**
     * Errore che viene mostrato all'utente se l'azione inviata non corrisponde a nessuna di quelle inserite
     */
    public static final String ERROR = "Non mi hanno imparato abbastanza per fare questo";

    /**
     * L'eventuale path successiva all'url dichiarato nel Webhook di Dialog-Flow
     */
    public final String path;

    /**
     * La porta in cui il server ascoltera'
     */
    public final int port;

    /**
     * Mappa che contiene tutte le azioni e il loro ID
     */
    private final Map<String, Action> actions;

    /**
     * Crea una classe vuota per un server che risponde alle chiamate di Dialog-Flow.
     * La path viene impostata di default a "/" e la porta a 4567
     */
	public DialogFlowWebHook() { this("/", 4567); }

    /**
     * Crea una classe vuota per un server che risponde alle chiamate di Dialog-Flow.
     * @param path il percorso dopo l'url inidicato nel WebHook di Dialog-Flow
     * @param port la porta da cui il Webhoook ascolta (se inserito numero negativo ascolta di default sulla porta 4567)
     */
    public DialogFlowWebHook(String path, int port) {
	    this.path = path;
	    this.port = port>0? port:4567;
        this.actions = new HashMap<>();
    }

    /**
     * Aggiunge un'azione ad una specifica richiesta di Dialog-Flow
     * @param actionId il nome dell'azione che viene passata da Dialog-Flow
     * @param action l'azione da fare (e' consigliato usare le espressioni lambda)
     */
	public void addOnAction(String actionId, Action action) { this.actions.put(actionId, action); }

    /**
     * Fa partire il server per accettare richieste da Dialog-Flow ascoltando connessioni in post.<br>
     * Ogni richiesta viene esaminata e fatta coincidere con un'azione specificata precedentemente.<br>
     * Se nessuna azione viene riscontrata, viene inviato un errore, rimuovendo i messaggi<br>
     * Inoltre aggiunge un'interfaccia in get che riguarda un iframe di dialogflow
     */
	public void startServer() {
        Spark.port(this.port);
        Gson gson = GsonFactory.getDefaultFactory().getGson();
        post(this.path, (request, response) -> {
            Fulfillment output = new Fulfillment();
            AIResponse input = gson.fromJson(request.body(), AIResponse.class);

            String inputAction = input.getResult().getAction();
            Map<String, JsonElement> inputParam = input.getResult().getParameters();
            String text;
            try {
                LOG.debug("AZIONE: "+ inputAction + ", PARAMS: " + inputParam);
                Action action = actions.get(inputAction);
                try {
                    text = action.doAction(inputParam);
                } catch (NullPointerException e) {
                    LOG.warn("AZIONE FALLITA: "+ inputAction);
                    text = ACTION_ERROR;
                }
            } catch (NullPointerException e) {
                LOG.error("NESSUNA AZIONE TROVATA: "+ inputAction);
                text = ERROR;
            }

            if(text == null)
                text = input.getResult().getFulfillment().getSpeech();

            LOG.debug("RISPOSTA: " + text);
            output.setDisplayText(text);
            output.setSpeech(text);

            response.type("application/json");
            return output;
        }, gson::toJson);

        get(this.path, (request, response) -> {
            return "<iframe\n" +
                    "    allow=\"microphone;\"\n" +
                    "    width=\"350\"\n" +
                    "    height=\"430\"\n" +
                    "    src=\"https://console.dialogflow.com/api-client/demo/embedded/SeniorAssistant\">\n" +
                    "</iframe>";
        });
    }

    /**
     * Interfaccia usata per fare un'azione per ogni Id di Dialog-Flow
     */
    public interface Action {
        /**
         * Fai l'azione desiderata.
         * Se ritorna una stringa allora il testo viene cambiato. Se ritorna null non cambia il testo
         *
         * @param params una mappa contenente tutti i parametri impostati da dialogflow
         * @return Una stringa che verra' usata come messaggio o null se non si vuole
         */
	    String doAction(Map<String, JsonElement> params);
    }
}
