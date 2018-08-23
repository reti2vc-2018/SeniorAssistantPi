package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    public String lastSyncTime;

   // @JsonProperty ("lastSyncTime")
    public String getLastSyncTime(List<Map<String,String>> sync){
        lastSyncTime = null;
        for(Map<String, String > d: sync) {
            String temp = d.get("lastSyncTime");
            if ((lastSyncTime == null) || (lastSyncTime.compareTo(temp) < 0))
                lastSyncTime = temp;
        }
        return lastSyncTime;
    }

}