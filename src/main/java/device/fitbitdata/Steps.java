package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Classe che serve a vedere i passi fatti secondo il fitbit
 */
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

	/**
	 * Assegna il valore dei passi
	 * @param steps i passi
	 */
	public void setSteps(int steps) { this.steps = steps; }

	/**
	 * I passi totali fatti durante il giorno
	 * @return i passi totali
	 */
	public int getSteps() { return steps; }

	/**
	 * Prendi i dati specifici dei passi
	 * @return una lista contenente una mappa con delle ore e minuti ad un valore del
	 */
	public List<Map<String, Object>> getStepsData() { return stepsData; }
}
