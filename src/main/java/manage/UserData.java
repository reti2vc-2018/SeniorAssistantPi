package manage;


import com.google.api.client.util.Key;
import java.util.List;

public class UserData {

    @Key
    public List<Data> list;
/*
    @Key
    public int limit;

    @Key("has more")
    public boolean hasMore;*/ //don't think are needed
}