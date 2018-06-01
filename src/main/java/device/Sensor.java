package device;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sensor {
    // init logger
    Logger logger = LoggerFactory.getLogger(Sensor.class);

    // sample RaZberry IP address
    String ipAddress = "http://172.30.1.137:8083";

    // sample username and password
    String username = "admin";
    String password = "raz4reti2";

    IZWayApi zwayApi;

    public Sensor() {
        // create an instance of the Z-Way library; all the params are mandatory (we are not going to use the remote service/id)
        zwayApi = new ZWayApiHttp(ipAddress, 8083, "http", username, password, 0, false, new ZWaySimpleCallback());
    }

    // get all the Z-Wave devices
    DeviceList allDevices = zwayApi.getDevices();

    public boolean IsLowLuminescence(int Luminescence) {
        for (Device dev : allDevices.getAllDevices()) {
            if (dev.getDeviceType().equalsIgnoreCase("SensorMultilevel"))
                if (dev.getProbeType().equalsIgnoreCase("luminescence"))
                    if (Integer.parseInt(dev.getMetrics().getLevel()) < Luminescence)
                        return true;
                    else
                        return false;
        }
        return false;
    }
}
