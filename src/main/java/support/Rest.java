package support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Una classe generica che invia delle richieste Rest e le parsifica nel JSON corrispondente
 */
public class Rest {

    /**
     * Un logger per vedere quando le richieste falliscono
     */
    private static final Logger LOG = LoggerFactory.getLogger("Rest");

    /**
     * Un GSON utile per il parsing della risposta
     */
    private static final Gson gson = new Gson();

    /**
     * Perform a GET request towards a server
     *
     * @param URL the @link{URL} to call
     * @return the response, parsed from JSON
     */
    public static Map<String, ?> get(String URL) {
        Map<String, ?> response = new HashMap<>();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet request = new HttpGet(URL);

        CloseableHttpResponse result;
        try {
            result = httpclient.execute(request);
            String json = EntityUtils.toString(result.getEntity());
            // do something useful with the response body
            response = gson.fromJson(json, Map.class);
            // should be inside a finally...
            result.close();
            httpclient.close();
        } catch (IOException e) {
            LOG.error("GET: " + e.getMessage());
        }

        return response;
    }

    /**
     * Perform a PUT request towards a server
     *
     * @param URL         the @link{URL} to call
     * @param contentBody the content body of the request
     * @param contentType the content type of the request
     */
    public static void put(String URL, String contentBody, String contentType) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPut request = new HttpPut(URL);

        StringEntity params;
        try {
            params = new StringEntity(contentBody);
            request.addHeader("content-type", contentType);
            request.setEntity(params);
            // I don't really care about the response
            HttpResponse result = httpclient.execute(request);
            // should be in finally...
            httpclient.close();
        } catch (Exception e) {
            LOG.error("PUT: " + e.getMessage());
        }

    }

}