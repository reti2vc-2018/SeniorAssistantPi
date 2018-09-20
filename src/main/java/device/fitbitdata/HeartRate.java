package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Classe per vedere il dato del battito cardiaco
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartRate extends FitbitData {

    private double average;

    @JsonProperty("activities-heart-intraday")
    private void setAverage(Map<String, Object> map) {
        List<Map> data = (List) map.get("dataset");

        int sum = 0;
        for(Map<String, Object> dat: data)
            sum += (int)dat.get("value");
        average = ((double)sum)/data.size();

        if(data.size() == 0)
            average = 0;
    }

    /**
     * Setta il vaore medio del battito cardiaco
     * @param average il valore medio del battito
     */
    public void setAverage(double average) { this.average = average; }

    /**
     * Ricevi il valore medio del battito cardiaco
     * @return il valore medio
     */
    public double getAverage() { return average; }
}
