package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sleep {

    private int minutesAsleep;

    public int getMinutesAsleep() {
        return minutesAsleep;
    }

    @JsonProperty("summary")
    public void setMinutesAsleep(Map<String, Object> map) {
        minutesAsleep = (int) map.get("totalMinutesAsleep");
    }
}
