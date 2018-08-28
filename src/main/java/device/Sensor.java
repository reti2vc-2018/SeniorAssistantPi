package device;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import support.ZWaySimpleCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensore che permette di registrare vari dati dell'ambiente
 */
public class Sensor {
	
	/**
	 * Logger?
	 */
    Logger logger = LoggerFactory.getLogger(Sensor.class);

    // sample RaZberry IP address
    public String ipAddress = "172.30.1.137";

    // sample username and password
    public String username = "admin";
    public String password = "raz4reti2";

    public IZWayApi zwayApi;
    private DeviceList allZWaveDevices;
    private DeviceList devices;

    /**
     * Crea un sensore contenente tutti i nodi
     */
    public Sensor() {
        this(null);
    }

    public Sensor (Integer nodeId) {
        // create an instance of the Z-Way library; all the params are mandatory (we are not going to use the remote service/id)
        zwayApi = new ZWayApiHttp(ipAddress, 8083, "http", username, password, 0, false, new ZWaySimpleCallback());

        // get all the Z-Wave devices
        allZWaveDevices = zwayApi.getDevices();

        if(nodeId != null)
            useNode(nodeId);
        else
            devices = allZWaveDevices;
    }

    public void useNode(int nodeId) {
        devices = new DeviceList();
        for (Device devi : allZWaveDevices.getAllDevices())
            if(devi.getNodeId() == nodeId)
                devices.addDevice(devi);
    }

    public int getBrightnessLevel() {
        for (Device device : devices.getAllDevices())
        if (device.getMetrics().getProbeTitle().equalsIgnoreCase("luminiscence"))
            return Integer.parseInt(device.getMetrics().getLevel());
        return -99;
    }

    synchronized public void update(int timeout) throws InterruptedException {
        //setInitialValues();
        for (Device device : devices.getAllDevices())
            device.update();
        wait(timeout);
    }
    /*public boolean IsLowLuminescence(int Luminescence) {
                if (dev.getProbeType().equalsIgnoreCase("Luminescence"))
                    if (Integer.parseInt(dev.getMetrics().getLevel()) < Luminescence)
                        return true;
                    else
                        return false;
                    return false;
    }*/
}
