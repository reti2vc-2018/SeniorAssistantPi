package device.fitbitdata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Steps extends FitbitData {

	private int steps = 0;
	
	@JsonProperty("activities-steps")
	public void setSteps(Map<String, String>[] array) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    
		for(Map<String, String> map : array)
			if(map.get("dateTime").equals(strDate))
				steps = Integer.parseInt(map.get("value"));
	}

	public void setSteps(int steps) { this.steps = steps; }
	public int getSteps() { return steps; }
}
