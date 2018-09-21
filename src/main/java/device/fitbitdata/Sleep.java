package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Classe per recuperare i dati del sonno dell'utente
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sleep {

    private long minutesAsleep;

    private List<SleepData> datas;

    @JsonProperty("summary")
    private void setMinutesAsleep(Map<String, Object> map) {
        minutesAsleep = (long) map.get("totalMinutesAsleep");
    }

    @JsonProperty("sleep")
    private void setSleepsList(Map<String, Object>[] array) {
        datas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat();
        for(Map<String, Object> map : array) {
            Date date_start = null;
            try {
                date_start = sdf.parse((String) map.get("startTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int duration = (int) map.get("duration");

            datas.add(new SleepData(date_start, duration));
        }
    }

    /**
     * I minuti totali che l'utente ha avuto di sonno durante il giorno
     * @return i minuti totali
     */
    public long getMinutesAsleep() {
        return minutesAsleep;
    }

    /**
     * Ricevi i dati più specifici riguardo al sonno
     * @return una lista con i dati specifici del sonno
     */
    public List<SleepData> getDatas() {
        return datas;
    }

    /**
     * Classe utilizzata per avere i dati più specifici del sonno
     */
    public class SleepData {
        /**
         * La data d'inizio del sonno in millisec
         */
        public final long start_date;
        /**
         * La durata del sonno in millisec
         */
        public final long duration;

        SleepData(Date start_date, long duration) {
            this.start_date = start_date.getTime();
            this.duration = duration;
        }
    }
}
