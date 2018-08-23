package device;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import device.fitbitdata.HeartRate;
import device.fitbitdata.Sleep;
import device.fitbitdata.Steps;
import oauth.AuthFitbit;

/**
 * Classe che permette di ricevere i dati di un particolare account FitBit
 */
public class Fitbit {

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
	private static final long MINUTE = 60000; /* 5 minutes in millisec */

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
	 * Crea una istanza di fitbit<br>
	 * Se l'utente non ha ancora accettato i dati richiesti o l'utente non e'
	 * loggato,<br>
	 * verra' aperto il browser in modo che si possano inserire i dati
	 * 
	 * @throws Exception Nel caso qualunque cosa andasse storta (vedi messaggio)
	 */
	public Fitbit() throws Exception {
		this.auth = new AuthFitbit();
	}

	/**
	 * Ricevi i passi che l'utente ha effettuato nell'ultimo giorno
	 * 
	 * @return un intero rappresentante i passi effettuati
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public int getSteps() throws IOException {
		if (shouldUpdateFor(Steps.class))
			steps = auth.run(BASIC_URL + "1" + USER + "activities/steps/date/today/1w.json", Steps.class);
		return steps.getSteps();
	}

	/**
	 * Ricevi il battito cardiaco dell'utente<br>
	 * Il risultato e' una media del battito che l'utente ha avuto negli ultimi 15 minuti
	 * 
	 * @return un intero rappresentante la media del battito cardiaco degli ultimi 15 minuti
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public double getHeartRate() throws IOException {
		return getHeartRate(15);
	}
	/**
	 * Ricevi il battito cardiaco dell'utente<br>
	 * Il risultato e' una media del battito che l'utente ha avuto negli ultimi minuti
	 * 
	 * @param lastMinutes fino a quanti minuti bisogna tenere conto (positivi se no ritorno -1)
	 * @return un intero rappresentante la media del battito cardiaco degli ultimi minuti specificati
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public double getHeartRate(int lastMinutes) throws IOException {
		if(lastMinutes<=0)
			return -1;
		if (shouldUpdateFor(HeartRate.class)) {
			long currentMillisec = System.currentTimeMillis();

			String now = getHourMinutes(currentMillisec);
			String ago = getHourMinutes(currentMillisec - (MINUTE * lastMinutes));

			if (now.compareTo(ago) < 0)
				ago = "00:00";

			heart = auth.run(
					BASIC_URL + "1" + USER + "activities/heart/date/today/1d/1sec/time/" + ago + "/" + now + ".json",
					HeartRate.class);
		}
		return heart.getAverage();
	}

	/**
	 * Ricevi le ore di sonno che l'utente ha fatto nell'ultimo giorno
	 * 
	 * @return un intero rappresentante le ore passate a dormire
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public Object getHoursSleep() throws IOException {
		if (shouldUpdateFor(Sleep.class))
			sleep = auth.run(BASIC_URL + "1.2" + USER + "sleep/date/today.json", Sleep.class);
		return sleep.getMinutesAsleep();
	}

	/**
	 * Semplice classe che controlla che si possa fare l'update o meno di una specifica classe<br>
	 * Se e' possibile fare l'update inserisce la classe nella mappa<br>
	 * In questo modo se questa funzione viene chiamata una seconda volta con lo stesso parametro restituira' falso<br>
	 * a meno che non si aspetti 5 minuti
	 * 
	 * @param type la classe da fare l'update
	 * @return vero se si puo' fare l'update
	 */
	private boolean shouldUpdateFor(Class<?> type) {
		try {
			long current = System.currentTimeMillis();
			long latest = latestRequest.get(type);

			if(current - latest < MINUTE * 5)
				return false;
		} catch (NullPointerException e) {
		}
		
		latestRequest.put(heart.getClass(), System.currentTimeMillis());
		return true;
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
