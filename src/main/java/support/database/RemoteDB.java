package support.database;

import device.fitbitdata.HeartRate;
import support.Rest;

import java.util.List;
import java.util.Map;

// TODO implement
public class RemoteDB implements Database {

    private final String base_url;
    private final String username;

    public RemoteDB(String username) {
        this(username, "http://127.0.0.1:5001/api/");
    }

    public RemoteDB(String username, String base_url) {
        this.username = username;
        this.base_url = base_url;
    }

    @Override
    public boolean isReachable() {
        Map<String, ?> map = Rest.get(base_url+"user/");
        LOG.info(map.toString());
        return !map.isEmpty();
    }

    @Override
    public boolean updateHeart(long dateMilliSec, double heartRate) {
        return false;
    }

    @Override
    public boolean updateSleep(long dateStartSleep, long duration) {
        return false;
    }

    @Override
    public boolean updateSteps(long dateMilliSec, long steps) {
        return false;
    }

    @Override
    public List<HeartRate> getHeartDataOfLast(int days) {
        return null;
    }

}
