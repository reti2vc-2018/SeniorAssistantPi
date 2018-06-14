import manage.AuthFITBIT;
import manage.FITBITData.*;

public class Main {
    public static void main(String[] args) throws Exception {
        AuthFITBIT fitbit = new AuthFITBIT();

      //  HeartRate h = fitbit.run("https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json", HeartRate.class, false); // 1sec/time/00:00/00:01.json
       // Sleep s = fitbit.run("https://api.fitbit.com/1.2/user/-/sleep/date/today.json", Sleep.class, false);

        Device dev = fitbit.run("https://api.fitbit.com/1/user/-/devices.json", Device.class, true);
        //System.out.println(dev);
        //System.out.println(h.dateTime + " " + h.average);
    }
}
