package manage;

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
/**
 * A generic class to perform HTTP calls and parse the resulting JSON.
 *
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.0 (18/05/2017)
 */
public class rest {

    // init gson
    private static final Gson gson = new Gson();

    /**
     * Perform a GET request towards a server
     *
     * @param URL the @link{URL} to call
     * @return the response, parsed from JSON
     */
    public static Map<String, ?> get(String URL) {
        // init
        Map<String, ?> response = new HashMap<>();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet request = new HttpGet(URL);

        CloseableHttpResponse result = null;

        try {
            result = httpclient.execute(request);
            String json = EntityUtils.toString(result.getEntity());
            // do something useful with the response body
            response = gson.fromJson(json, Map.class);
            // should be inside a finally...
            result.close();
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        // init
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPut request = new HttpPut(URL);
        StringEntity params = null;

        try {
            params = new StringEntity(contentBody);
            request.addHeader("content-type", contentType);
            request.setEntity(params);
            // I don't really care about the response
            HttpResponse result = httpclient.execute(request);
            // should be in finally...
            httpclient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}