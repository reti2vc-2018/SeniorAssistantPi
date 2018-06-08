package manage.FITBITData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartRate {

    public String dateTime;

    @JsonProperty("activities-heart")
    public void quelloCheVoglio(Map<String, Object>[] activities){
        dateTime = (String) activities[0].get("dateTime");
    }
}
