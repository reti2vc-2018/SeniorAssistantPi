package test;

import org.junit.Test;
import support.database.Database;
import support.database.RemoteDB;

import static org.junit.Assert.assertTrue;

public class TestRemoteDB {

    private static final String REMOTE_URL = "http://127.0.0.1:5001/api/";
    private static final String USERNAME = "vecchio";

    @Test
    public void test() {
        Database database = new RemoteDB(USERNAME, REMOTE_URL);
        assertTrue(database.isReachable());
    }
}
