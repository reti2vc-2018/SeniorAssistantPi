package manage;

import com.google.api.client.util.Key;

import java.util.List;

//da modificare inserendo gli attributi del json (activity, heartrate, sleep, location)

public class Data {

    @Key
    public String id;

    @Key
    public List<String> tags;

    @Key
    public String title;

    @Key
    public String url;
}