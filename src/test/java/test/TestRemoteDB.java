package test;

import org.junit.Test;
import support.database.Database;
import support.database.RemoteDB;

import static org.junit.Assert.assertTrue;

public class TestRemoteDB {

    private static final String REMOTE_URL = "http://127.0.0.1:5000/api/";
    private static final String USERNAME = "vecchio1";

    @Test
    public void test() {
        Database database = new RemoteDB(USERNAME, REMOTE_URL);
        assertTrue(database.isReachable());

        //assertTrue(database.updateHeart(System.currentTimeMillis(), Math.random()*70 + 50));
        //assertTrue(database.updateSleep(System.currentTimeMillis(), (long) (Math.random()*7200000) + 0));
        //assertTrue(database.updateSteps(System.currentTimeMillis(), (long) (Math.random()*100) + 100));
        //database.getHeartDataOfLast(10);
    }
}
