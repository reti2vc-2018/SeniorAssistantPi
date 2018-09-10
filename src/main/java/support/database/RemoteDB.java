package support.database;

import device.fitbitdata.HeartRate;

import java.util.List;

// TODO implement
public class RemoteDB implements Database {

    public RemoteDB(String url) {

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
