package support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 20015159 on 11/09/2018.
 */
public class Musich {

    public static final String SEARCH_URL = "https://www.youtube.com/watch?v=";

    private static final String API_URL = "https://www.googleapis.com/youtube/v3/search?";
    private static final String KEY = "AIzaSyCtCK0EPR3k_hEEyar0PeY5v9E9UyTX4TM";

    public static List<String> getVideosId(String search) {
        final int maxResult = 5;
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

}
