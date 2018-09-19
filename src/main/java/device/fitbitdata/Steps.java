package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Steps extends FitbitData {

	private int steps;
	private List<Map<String, Object>> stepsData;
	
	@JsonProperty("activities-steps")
	private void setSteps(Map<String, String>[] array) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    
		for(Map<String, String> map : array)
			if(map.get("dateTime").equals(strDate))
				steps = Integer.parseInt(map.get("value"));
	}

	@JsonProperty("activities-steps-intraday")
	private void setDetailedSteps(Map<String, Object> map) {
		stepsData = new ArrayList<>((List<Map<String, Object>>) map.get("dataset"));
	}

	public void setSteps(int steps) { this.steps = steps; }
	public int getSteps() { return steps; }
	public List<Map<String, Object>> getStepsData() { return stepsData; }
}
