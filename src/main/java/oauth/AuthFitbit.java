package oauth;

import java.io.IOException;
import java.util.Arrays;

import ai.api.GsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//todo add docs
public class AuthFitbit {

    private static final Logger LOG = LoggerFactory.getLogger("Fitbit Response");

    private final HttpRequestFactory requestFactory;

    public AuthFitbit() throws Exception {
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        final Credential credential = authorize();

        this.requestFactory = HTTP_TRANSPORT.createRequestFactory( request -> {
            credential.initialize(request);
            request.setParser(new JsonObjectParser(JSON_FACTORY));
        });
    }

    private static ObjectMapper mapper = new ObjectMapper();
    /** Directory to store user credentials. */
    /* Throw a Warning when change permission: they said it's a google bug 'cause is meant to run in linux/unix
     *
     * https://stackoverflow.com/questions/30634827/warning-unable-to-change-permissions-for-everybody
     * https://github.com/google/google-http-java-client/issues/315
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/seniorAssistant");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** OAuth 2 scope. */
    private static final String SCOPE[] = new String[]{"activity","heartrate","sleep","settings"};
    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final String TOKEN_SERVER_URL = " https://api.fitbit.com/oauth2/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://www.fitbit.com/oauth2/authorize";

    /** Authorizes the installed application to access user's protected data. */
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

    public <O> O run(String url, Class<O> returnClass) throws IOException {
        return run(url, returnClass, false);
    }

    public <O> O run(String url, Class<O> returnClass, boolean useAsParse) throws IOException {
        FITBITUrl fitbitUrl = new FITBITUrl(url);
        fitbitUrl.setFields("");

        HttpRequest request = requestFactory.buildGetRequest(fitbitUrl);
        HttpResponse response = request.execute();

        /*
        GenericJson json = response.parseAs(GenericJson.class);
        response.disconnect();

        System.out.println(returnClass.getSimpleName());
        System.out.println(url);
        System.out.println(json.toPrettyString());

        return mapper.readValue(json.toString(), returnClass);
        */

        /**/
        O ret = ( useAsParse ?
                response.parseAs(returnClass) :
                mapper.readValue(response.parseAs(GenericJson.class).toString(), returnClass)
            );

        response.disconnect();

        // todo remove this, it's only useful if you need to see the request
        LOG.info(GsonFactory.getDefaultFactory().getGson().toJson(ret));

        return ret;
        /**/
    }
}
