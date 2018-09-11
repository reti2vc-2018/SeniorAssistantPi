package support.database;

import device.fitbitdata.HeartRate;

import java.util.List;

// TODO implement
public class RemoteDB implements Database {

    public static final String BASE_URL = "https://localhost:"; //TODO inserire il percorso giusto con la porta

    private final String username;

    public RemoteDB(String username) {
        this.username = username;
    }

    @Override
    public boolean isReachable() {
        return false;
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
