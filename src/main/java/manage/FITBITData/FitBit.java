package manage.FITBITData;

import manage.AuthFITBIT;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FitBit {

    public static final String BASIC_URL = "https://api.fitbit.com/";
    public static final String USER = "/user/-/";
    private static final long MINUTE = 60000; /* 5 minutes in millisec */

    private final AuthFITBIT auth;
    private final Map<Class, Long> latestRequest = new HashMap<>();
    private final Calendar calendar = Calendar.getInstance();

    private HeartRate heart = null;
    private Sleep sleep = null;
    private Steps steps = null;

    public FitBit() throws Exception {
        this(new AuthFITBIT());
    }

    public FitBit(AuthFITBIT auth) {
        if(auth == null)
            throw new NullPointerException("I must have an Auth for the FitBit");
        this.auth = auth;
    }

    /* passi */
    //https://api.fitbit.com/1/user/-/activities/steps/date/today.json
    public int getSteps() throws IOException {
        if(shouldUpdateFor(Steps.class)) {
            long currentMillisec = System.currentTimeMillis();

            steps = auth.run(BASIC_URL + "1" + USER + "activities/steps/date/today.json", Steps.class);
            latestRequest.put(steps.getClass(), currentMillisec);
        }
        return 0;
    }

    /* battito */
    public double getHeartRate() throws IOException {
        if(shouldUpdateFor(HeartRate.class)) {
            long currentMillisec = System.currentTimeMillis();

            String now = getHourMinutes(currentMillisec);
            String ago = getHourMinutes(currentMillisec-(MINUTE*15));

            if(now.compareTo(ago) < 0)
                ago = "00:00";

            heart = auth.run(BASIC_URL + "1" + USER + "activities/heart/date/today/1d/1sec/time/"+ago+"/"+now+".json", HeartRate.class);
            latestRequest.put(heart.getClass(), currentMillisec);
        }
        return heart.getAverage();
    }

    /* sonno */
    public Object getHoursSleep() throws IOException {
        if(shouldUpdateFor(Sleep.class)) {
            long currentMillisec = System.currentTimeMillis();

            sleep = auth.run(BASIC_URL + "1.2" + USER + "sleep/date/today.json", Sleep.class);
            latestRequest.put(sleep.getClass(), currentMillisec);
        }
        return sleep.getMinutesAsleep();
    }

    private boolean shouldUpdateFor(Class type) {
        try {
            long current = System.currentTimeMillis();
            long latest = latestRequest.get(type);

            if (current - latest > MINUTE * 5)
                return true;
            return false;
        } catch (NullPointerException e) {}
        return true;
    }

    private String getHourMinutes(long milliseconds) {
        calendar.setTimeInMillis(milliseconds);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minu = calendar.get(Calendar.MINUTE);
        return String.format("%02d:%02d", hour, minu);
    }

    // Device dev = auth.run(BASIC_URL + "devices.json", Device.class);
}
