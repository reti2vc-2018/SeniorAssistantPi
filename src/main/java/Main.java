import manage.AuthFITBIT;
import manage.FITBITData.*;

public class Main {
    public static void main(String[] args) throws Exception {
        AuthFITBIT fitbit = new AuthFITBIT();

        HeartRate h = fitbit.run("https://api.fitbit.com/1/user/-/activities/heart/date/today/1d/1sec/time/11:00/11:45.json", HeartRate.class);
        //Sleep s = fitbit.run("https://api.fitbit.com/1.2/user/-/sleep/date/today.json", Sleep.class);
        Device dev = fitbit.run("https://api.fitbit.com/1/user/-/devices.json", Device.class);

        System.out.println(h.dateTime + " " + h.average);
    }
}
