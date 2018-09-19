package test;

import device.Fitbit;
import org.junit.Test;
import support.Rest;

public class TestFitbit {

	@Test
	public void test01() throws Exception {
		Fitbit fitBit = new Fitbit();
        fitBit.getHoursSleep();

        System.out.println("Today's average heart-rate: "+fitBit.getHeartRate());
        System.out.println("Today's hours of sleep: "+fitBit.getHoursSleep());
        System.out.println("Today's steps: "+fitBit.getSteps());
        System.out.println("Fine.");

        Rest.get("https://api.fitbit.com/1/user/-/activities/steps/date/today/1d.json");
	}

	@Test
    public void test02() throws Exception {
        Fitbit fitbit = new Fitbit();
        System.out.println(fitbit.getSteps(60));
    }
}
