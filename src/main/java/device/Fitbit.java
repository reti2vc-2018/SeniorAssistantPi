package device;

import device.fitbitdata.HeartRate;
import device.fitbitdata.Sleep;
import device.fitbitdata.Steps;
import oauth.AuthFitbit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe che permette di ricevere i dati di un particolare account FitBit
 */
public class Fitbit {

	/**
	 * Logger per vedere cosa invia e riceve questa classe
	 */
	private static final Logger LOG = LoggerFactory.getLogger("Fitbit");

	/**
	 * Url da dove si possono prendere i dati dai vari dispositivi fitbit
	 */
	public static final String BASIC_URL = "https://api.fitbit.com/";

	/**
	 * Utente del fitbit<br>
	 * In questo caso e' universale e prende l'utente che e' attualmente loggato
	 */
	public static final String USER = "/user/-/";

	/**
	 * Un minuto in millisecondi
	 */
	private static final long MINUTE = 60000;

	/**
	 * L'oauth per l'account fitbit
	 */
	private final AuthFitbit auth;

	/**
	 * Una mappa contenente le ultime classi usate nelle richieste effettuate<br>
	 * Una sorta di cache
	 */
	private final Map<Class<?>, Long> latestRequest = new HashMap<>();

	/**
	 * Un calendario in modo da sapere la data per i dati
	 */
	private final Calendar calendar = Calendar.getInstance();

	/**
	 * La classe per sapere i dati sul battito cardiaco
	 */
	private HeartRate heart = null;

	/**
	 * La classe per sapere i dati sul sonno
	 */
	private Sleep sleep = null;

	/**
	 * La classe per sapere i dati sui passi effettuati
	 */
	private Steps steps = null;

	/**
	 * Crea un'istanza di fitbit<br>
	 * Se l'utente non ha ancora accettato i dati richiesti o l'utente non e'
	 * loggato, verra' aperto il browser in modo che si possano inserire i dati
	 * 
	 * @throws Exception Nel caso qualunque cosa andasse storta (vedi messaggio)
	 */
	public Fitbit() throws Exception { this.auth = new AuthFitbit(); }

	/**
	 * Ricevi i passi che l'utente ha effettuato nell'ultimo giorno
	 * 
	 * @return un intero rappresentante i passi effettuati
	 */
	public synchronized int getSteps() {
		steps = update(Steps.class, steps, "1" + USER + "activities/steps/date/today/1d/1min.json");
		return steps.getSteps();
	}

	/**
	 * Ricevi i passi che l'utente ha effettuato negli ultimi minuti richiesti
	 *
	 * @param lastMinutes gli ultimi minuti che si vogliono vedere (positivi e !=0 se no ritorno -1)
	 * @return un intero rappresentante i passi effettuati
	 */
	public synchronized int getSteps(int lastMinutes) {
		if(lastMinutes<=0)
			return -1;

		steps = update(Steps.class, steps, "1" + USER + "activities/steps/date/today/1d/1min.json");

		List<Map<String, Object>> list = steps.getStepsData();
		final int now = list.size()-1;

		int totalSteps = 0;
		for(int i=0; i<lastMinutes; i++)
			totalSteps += (int)list.get(now-i).get("value");

		return totalSteps;
	}

	/**
	 * Ricevi il battito cardiaco dell'utente<br>
	 * Il risultato e' una media del battito che l'utente ha avuto negli ultimi 15 minuti
	 * 
	 * @return un intero rappresentante la media del battito cardiaco degli ultimi 15 minuti
	 */
	public synchronized double getHeartRate() { return getHeartRate(15); }

	/**
	 * Ricevi il battito cardiaco dell'utente<br>
	 * Il risultato e' una media del battito che l'utente ha avuto negli ultimi minuti
	 * 
	 * @param lastMinutes fino a quanti minuti bisogna tenere conto (positivi e !=0 se no ritorno -1)
	 * @return un intero rappresentante la media del battito cardiaco degli ultimi minuti specificati
	 */
	public synchronized double getHeartRate(int lastMinutes) {
		if(lastMinutes<=0)
			return -1;

		long currentMillisec = System.currentTimeMillis();

		String now = getHourMinutes(currentMillisec);
		String ago = getHourMinutes(currentMillisec - (MINUTE * lastMinutes));

		if (now.compareTo(ago) < 0)
			ago = "00:00";

		heart = update(HeartRate.class, null,"1" + USER + "activities/heart/date/today/1d/1sec/time/" + ago + "/" + now + ".json");
		return heart.getAverage();
	}

	/**
	 * Ricevi le ore di sonno che l'utente ha fatto nell'ultimo giorno
	 * 
	 * @return un intero rappresentante le ore passate a dormire
	 */
	public synchronized long getHoursSleep() {
		sleep = update(Sleep.class, sleep,"1.2" + USER + "sleep/date/today.json");
		return sleep.getMinutesAsleep()/60;
	}

	/**
	 * Ricevi tutti i dati presenti per il sonno di questo giorno.
	 * La lista contiene per ogni volta che l'utente ha dormito:<br>
	 * - la data di quando si e' addormentato<br>
	 * - la durata del sonno<br>
	 * - la data di fine<br>
	 * @return una lista contenente ogni volta che l'utente ha dormito
	 */
	public synchronized List<Sleep.SleepData> getDetailedSleep() {
		sleep = update(Sleep.class, sleep,"1.2" + USER + "sleep/date/today.json");
		return sleep.getDatas();
	}

	/**
	 * Semplice funzione che controlla che si possa fare l'update o meno di una specifica classe.<br>
	 * Se e' possibile fare l'update viene mandata una run all'url selezionato e viene ritornata la variabile aggiornata<br>
	 * Altrimenti viene ritornata la variabile passata<br>
	 * Nel caso di fallimento della richiesta varra' restituito la variabile passata in input
	 *
	 * @param varClass la classe della variabile passata
	 * @param variable la variabile che deve fare l'update (passando null si forza la richiesta)
	 * @param url l'url da cui prende i dati aggiornati
	 * @return la variabile aggiornata
	 */
	private synchronized <T> T update(Class<T> varClass, T variable, String url) {
		try {
			long current = System.currentTimeMillis();
			long latest = latestRequest.get(varClass);

			// don't update
			if( (variable!=null) && (current - latest < MINUTE * 5) )
				return variable;
		} catch (NullPointerException e) {
			// do nothing and update
		}

		LOG.info("Updating " + varClass.getSimpleName() + " form " + BASIC_URL + url);
		try {
			variable = auth.run(BASIC_URL + url, varClass);
			latestRequest.put(varClass, System.currentTimeMillis());
		} catch (IOException e) {
			LOG.error("Non sono riuscito a prender i dati aggiornati: " + e.getMessage());
			e.printStackTrace();
		}
		return variable;
	}

	/**
	 * Funzione che transforma i millisecondi nel formato "hh:mm"
	 * 
	 * @param milliseconds millisecondi da trasformare
	 * @return una stringa nel formato "hh:mm"
	 */
	private String getHourMinutes(long milliseconds) {
		calendar.setTimeInMillis(milliseconds);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minu = calendar.get(Calendar.MINUTE);
		return String.format("%02d:%02d", hour, minu);
	}

	// Device dev = auth.run(BASIC_URL + "devices.json", Device.class);
}
