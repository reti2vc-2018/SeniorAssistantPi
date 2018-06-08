package manage.FITBITData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartRate {

    public String dateTime;
    public double average;

    @JsonProperty("activities-heart")
    public void quelloCheVoglio(Map<String, Object>[] activities){
        dateTime = (String) activities[0].get("dateTime");
    }

    @JsonProperty("activities-heart-intraday")
    public void qualcosAltro(Map<String, Map<String, String>[]> map) {
        Map<String, String>[] data = map.get("dataset");

        int sum = 0;
        for(Map<String, String> dat: data)
            sum += Integer.parseInt(dat.get("value"));
        average = ((double)sum)/data.length;
    }
}
