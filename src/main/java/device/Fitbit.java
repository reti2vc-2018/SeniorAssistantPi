package device;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import device.fitbitdata.HeartRate;
import device.fitbitdata.Sleep;
import device.fitbitdata.Steps;
import oauth.AuthFitbit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public synchronized int getSteps() throws IOException {
		steps = update(Steps.class, steps, "1" + USER + "activities/steps/date/today/1w.json");
		return steps.getSteps();
	}

	/**
	 * Ricevi il battito cardiaco dell'utente<br>
	 * Il risultato e' una media del battito che l'utente ha avuto negli ultimi 15 minuti
	 * 
	 * @return un intero rappresentante la media del battito cardiaco degli ultimi 15 minuti
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public synchronized double getHeartRate() throws IOException { return getHeartRate(15); }

	/**
	 * Ricevi il battito cardiaco dell'utente<br>
	 * Il risultato e' una media del battito che l'utente ha avuto negli ultimi minuti
	 * 
	 * @param lastMinutes fino a quanti minuti bisogna tenere conto (positivi e !=0 se no ritorno -1)
	 * @return un intero rappresentante la media del battito cardiaco degli ultimi minuti specificati
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public synchronized double getHeartRate(int lastMinutes) throws IOException {
		if(lastMinutes<=0)
			return -1;

		long currentMillisec = System.currentTimeMillis();

		String now = getHourMinutes(currentMillisec);
		String ago = getHourMinutes(currentMillisec - (MINUTE * lastMinutes));

		if (now.compareTo(ago) < 0)
			ago = "00:00";

		heart = update(HeartRate.class, heart,"1" + USER + "activities/heart/date/today/1d/1sec/time/" + ago + "/" + now + ".json");
		return heart.getAverage();
	}

	/**
	 * Ricevi le ore di sonno che l'utente ha fatto nell'ultimo giorno
	 * 
	 * @return un intero rappresentante le ore passate a dormire
	 * @throws IOException nel caso la richiesta non vada a buon fine
	 */
	public synchronized long getHoursSleep() throws IOException {
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
	 * @throws IOException
	 */
	public synchronized List<Sleep.SleepData> getDetailedSleep() throws IOException {
		sleep = update(Sleep.class, sleep,"1.2" + USER + "sleep/date/today.json");
		return sleep.getDatas();
	}

	/**
	 * Semplice funzione che controlla che si possa fare l'update o meno di una specifica classe.<br>
	 * Se e' possibile fare l'update viene mandata una run all'url selezionato e viene ritornata la variabile aggiornata<br>
	 * Altrimenti viene ritornata la variabile passata
	 *
	 * @param varClass la classe della variabile passata
	 * @param variable la variabile che vede fare l'update
	 * @param url l'url da cui prende i dati aggiornati
	 * @return la variabile aggiornata
	 */
	private synchronized <T> T update(Class<T> varClass, T variable, String url) throws IOException {
		try {
			long current = System.currentTimeMillis();
			long latest = latestRequest.get(varClass);

			// don't update
			if(current - latest < MINUTE * 5)
				return variable;
		} catch (NullPointerException e) {
			// do nothing and update
		}
		latestRequest.put(varClass, System.currentTimeMillis());
		LOG.info("Updating " + varClass.getSimpleName() + " form " + BASIC_URL + url);
		return auth.run(BASIC_URL + url, varClass);
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
