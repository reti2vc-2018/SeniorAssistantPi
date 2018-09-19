package oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Classe piu' importante per la connessione al fitbit
 */
public class AuthFitbit {

    /**
     * Un logger per rendere le cose semplici in caso di casini
     */
    private static final Logger LOG = LoggerFactory.getLogger("Fitbit Auth");

    /**
     * Un mapper per trasformare i json in mappe.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Directory dove vengono messi i dati utente (Token)<br>
     * <br>
     * Throw a Warning when change permission: they said it's a google bug 'cause is meant to run in linux/unix<br>
     * https://stackoverflow.com/questions/30634827/warning-unable-to-change-permissions-for-everybody<br>
     * https://github.com/google/google-http-java-client/issues/315<br>
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/seniorAssistant");

    /**
     * OAuth 2 scope.<br>
     * Nel nostro caso sono le varie categorie dove si trovano le informazioni di cui abbiamo bisogno
     */
    private static final String SCOPE[] = new String[]{"activity","heartrate","sleep","settings"};

    /**
     * Instanza globale di HttpTranspot necessaria per l'autorizzazione e per le richieste
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Istanza globale di una Json Factory
     */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /**
     * Url dove e' possiblie richiedere il token
     */
    private static final String TOKEN_SERVER_URL = " https://api.fitbit.com/oauth2/token";

    /**
     * Pagina dove si richiede l'autorizzazione a tutti i campi richiesti
     */
    private static final String AUTHORIZATION_SERVER_URL = "https://www.fitbit.com/oauth2/authorize";

    /**
     * Istanza globale del {@link DataStoreFactory}. Il miglior metodo e' creare una singola
     * istanza globale condivisa attraverso tutta l'applicazione.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Un HttpRequestFactory che serve per creare una richiesta http
     */
    private final HttpRequestFactory requestFactory;

    /**
     * Prova a connettersi al sito di Fitbit per controllare l'autorizzazione<br>
     * Se la richiesta inviata e' sbagliata lancia una eccezione.<br>
     * Se l'utente non ha ancora autorizzato l'applicazioe, allora una pagina sul browser (o un link in console) appare<br>
     * ci si logga e si lascia l'autorizzazione a questa applicazione.
     * @throws Exception per qualunque cosa apparentemente (e noi ci siamo riusciti)
     */
    public AuthFitbit() throws Exception {
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        final Credential credential = authorize();

        this.requestFactory = HTTP_TRANSPORT.createRequestFactory( request -> {
            credential.initialize(request);
            request.setParser(new JsonObjectParser(JSON_FACTORY));
        });
    }

    /**
     * Autorizza l'applicazione ad accedere ai dati utente richiesti
     */
    private Credential authorize() throws Exception {
        // set up authorization code flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
                .authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new BasicAuthentication (
                        OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
                OAuth2ClientCredentials.API_KEY,
                AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(SCOPE))
                .setDataStoreFactory(DATA_STORE_FACTORY).build();
        // authorize
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
                OAuth2ClientCredentials.DOMAIN).setPort(OAuth2ClientCredentials.PORT).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize( "user" );
    }

    /**
     * Effettua una chiamata al server fitbit richiedendo i dati indicati dall'url
     *
     * @param url l'url in cui effettuare la richiesta
     * @return una stringa in formato Json, che e' il risultato
     * @throws IOException nel caso ci sia un errore con la richiesta
     */
    public String run(String url) throws IOException {
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = requestFactory.buildGetRequest(genericUrl);
        HttpResponse response = request.execute();

        String content = response.parseAsString();
        response.disconnect();
        LOG.debug("Recived: " + content);

        return content;
    }

    /**
     * Fa una chiamata al server fitbit richiedendo i dati indicati dall'url<br>
     * La classe e' richiesta se si vuole fare il parsing diretto e ricevere la classe parsificata con un mapper<br>
     * in questo modo non tutti i campi devono esistere
     *
     * @param url l'url in cui effettuare la richiesta
     * @param returnClass la classe da ritornare
     * @param <O> la classe che ritorna
     * @return un oggetto rappresentante la richiesta fatta all'url
     * @throws IOException nel caso ci sia un errore con la richiesta o con il parsing di quest'ultima
     */
    public <O> O run(String url, Class<O> returnClass) throws IOException {
        O ret = MAPPER.readValue(this.run(url), returnClass);
        LOG.debug("Saved in class: " + JSON_FACTORY.toString(ret));

        return ret;
        /**/
    }
}
