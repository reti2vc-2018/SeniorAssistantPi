package manage;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.IOException;
import java.util.Arrays;

public class AuthFITBIT {

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
    private static final String SCOPE[] = new String[]{"activity","heartrate","location","sleep"};
    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /* When i try to accept the request it'll send a 401 Unauthorized
     * on internet i found that this message appears when the client put a wrong
     * header, client_id or client_secret
     * https://dev.fitbit.com/build/reference/web-api/oauth2/ -> ALT+F "401 Unauthorized"
     */
    private static final String TOKEN_SERVER_URL = " https://api.fitbit.com/oauth2/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://www.fitbit.com/oauth2/authorize";

    /** Authorizes the installed application to access user's protected data. */
    private static Credential authorize() throws Exception {
        OAuth2ClientCredentials.errorIfNotSpecified();
        // set up authorization code flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
                .authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new ClientParametersAuthentication(
                        OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
                OAuth2ClientCredentials.API_KEY,
                AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(SCOPE))
                .setDataStoreFactory(DATA_STORE_FACTORY).build();
        // authorize
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
                OAuth2ClientCredentials.DOMAIN).setPort(OAuth2ClientCredentials.PORT).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user" );
    }

    private static void run(HttpRequestFactory requestFactory) throws IOException {
        FITBITUrl url = new FITBITUrl("https://api.fitbit.com/1/user/-/profile.json"); //modificare con token?
        url.setFields("activity,heartrate,location,sleep");

        HttpRequest request = requestFactory.buildGetRequest(url);
        UserData data = request.execute().parseAs(UserData.class);
        if (data.list.isEmpty()) {
            System.out.println("Error in retrieve user data");
        } else/* {
            if (data.hasMore) {
                System.out.print("First ");
            }*/ //i don't think is necessary
       /*     System.out.println(data.list.size() + " favorite videos found:");
            */for (FITIBITData datas: data.list) {
                System.out.println(datas.toString());/*
                System.out.println("-----------------------------------------------");
                System.out.println("ID: " + datas.id);
                System.out.println("Title: " + datas.title);
                System.out.println("Tags: " + datas.tags);
                System.out.println("URL: " + datas.url);
         */   }/*
        }*/ //neither this
    }

    public static void main(String[] args) {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            final Credential credential = authorize();
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                            credential.initialize(request);
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });
            run(requestFactory);
            System.err.print("DONE");
            // Success!
            return;
        } catch (IOException e) {
            System.err.println(e.getClass().getSimpleName()+" "+e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(1);
    }

}
