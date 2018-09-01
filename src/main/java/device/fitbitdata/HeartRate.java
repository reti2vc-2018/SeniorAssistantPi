package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartRate {

    private double average;

    public double getAverage() { return average; }

    @JsonProperty("activities-heart-intraday")
    public void setAverage(Map<String, Object> map) {
        List<Map> data = (List) map.get("dataset");

        int sum = 0;
        for(Map<String, Object> dat: data)
            sum += (int)dat.get("value");
        average = ((double)sum)/data.size();

        if(data.size() == 0)
            average = 0;
    }
}
