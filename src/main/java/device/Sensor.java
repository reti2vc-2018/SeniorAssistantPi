package device;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import support.ZWaySimpleCallback;

/**
 * Sensore che permette di registrare vari dati dell'ambiente
 */
public class Sensor {

    /**
     * IP del sensore a cui ci si vuole agganciare
     */
    private static final String IP_ADDRESS = "172.30.1.137";

    /**
     * Porta in cui si ascolta per i sensori
     */
    private static final int PORT = 8083;

    /**
     * Username con cui si entra nel dispositivo
     */
    private static final String USERNAME = "admin";
    /**
     * Password del dispositivo
     */
    private final String PASSWORD = "raz4reti2";

    /**
     * Tutti i devices che esistono nella rete
     */
    private DeviceList allZWaveDevices;
    /**
     * I device che vengono selezionati e filtrati dall'utente (ovvero quelli che verranno usati per prendere i dati)
     */
    private DeviceList devices;

    /**
     * Crea un sensore contenente tutti i nodi
     * @throws NullPointerException se non trova nessun sensore
     */
    public Sensor() throws NullPointerException { this(null); }

    /**
     * Si connette ad un sensore che ha il nodeId selezioniato
     * @param nodeId nodo che viene selezionato
     * @throws NullPointerException se non trova nessun sensore
     */
    public Sensor (Integer nodeId) throws NullPointerException {
        // create an instance of the Z-Way library; all the params are mandatory (we are not going to use the remote service/id)
        IZWayApi zwayApi = new ZWayApiHttp(IP_ADDRESS, PORT, "http", USERNAME, PASSWORD, 0, false, new ZWaySimpleCallback());

        // get all the Z-Wave devices
        allZWaveDevices = zwayApi.getDevices();

        if(allZWaveDevices == null)
            throw new NullPointerException("I sensori non sono stati trovati");

        if(nodeId != null)
            useNode(nodeId);
        else
            devices = allZWaveDevices;
    }

    /**
     * Cambia i dispositivi selezionati in base al nodeId che viene scelto
     * @param nodeId il nodo che viene selezionato
     */
    public void useNode(int nodeId) {
        devices = new DeviceList();
        for (Device devi : allZWaveDevices.getAllDevices())
            if(devi.getNodeId() == nodeId)
                devices.addDevice(devi);
    }

    /**
     * Legge i valori della luminosita' segnata dai dispositivi e ne ritorna il valore
     * @return la luminosita' segnata dai dispositivi (da 0 a 100)
     */
    public double getBrightnessLevel() {
        update();
        for (Device device : devices.getAllDevices())
            if (device.getMetrics().getProbeTitle().equalsIgnoreCase("luminiscence"))
                return Double.parseDouble(device.getMetrics().getLevel())/10;
        return 0;
    }

    /**
     * Fa in modo di forzare l'aggiornamento dei dispositivi
     */
    synchronized private void update() {
        for (Device device : devices.getAllDevices())
            try {
                device.update();
            } catch (Exception e) { }
    }
}
