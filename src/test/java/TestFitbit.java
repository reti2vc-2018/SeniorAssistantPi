import org.junit.Test;

import device.FitBit;

public class TestFitbit {
	
	@Test
	public void test01() throws Exception {
		FitBit fitBit = new FitBit();
        fitBit.getHoursSleep();

        System.out.println("Today's average heart-rate: "+fitBit.getHeartRate());
        System.out.println("Today's hours of sleep: "+fitBit.getHoursSleep());
        System.out.println("Today's steps: "+fitBit.getSteps());
        System.out.println("Fine.");
	}
}
