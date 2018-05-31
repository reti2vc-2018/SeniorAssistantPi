package device;

import de.fh_zwickau.informatik.sensor.IZWayApiCallbacks;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistory;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryList;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.instances.Instance;
import de.fh_zwickau.informatik.sensor.model.instances.InstanceList;
import de.fh_zwickau.informatik.sensor.model.locations.Location;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.fh_zwickau.informatik.sensor.model.modules.ModuleList;
import de.fh_zwickau.informatik.sensor.model.namespaces.NamespaceList;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationList;
import de.fh_zwickau.informatik.sensor.model.profiles.Profile;
import de.fh_zwickau.informatik.sensor.model.profiles.ProfileList;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.controller.ZWaveController;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.devices.ZWaveDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mandatory callback class for the Z-Way Library in use.
 * Really trivial implementation: log all the responses at debug level.
 *
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.0 (24/05/2017)
 * @see <a href="https://github.com/pathec/ZWay-library-for-Java">Z-Way Library on GitHub</a> for documentation about the used library
 */
public class ZWaySimpleCallback implements IZWayApiCallbacks {

    @Override
    public void getStatusResponse(String s) {
        this.logMessage("Status response: " + s);
    }

    @Override
    public void getRestartResponse(Boolean aBoolean) {
        this.logMessage("Restart response: " + aBoolean);
    }

    @Override
    public void getLoginResponse(String s) {
        this.logMessage("Login response: " + s);
    }

    @Override
    public void getNamespacesResponse(NamespaceList namespaceList) {
        this.logMessage("Namespaces are: " + namespaceList);
    }

    @Override
    public void getModulesResponse(ModuleList moduleList) {
        this.logMessage("Modules are: " + moduleList);
    }

    @Override
    public void getInstancesResponse(InstanceList instanceList) {
        this.logMessage("Instances are: " + instanceList);
    }

    @Override
    public void postInstanceResponse(Instance instance) {
        this.logMessage("Received a POST for the instance: " + instance);
    }

    @Override
    public void getInstanceResponse(Instance instance) {
        this.logMessage("The instance is: " + instance);
    }

    @Override
    public void putInstanceResponse(Instance instance) {
        this.logMessage("Received a PUT for the instance: " + instance);
    }

    @Override
    public void deleteInstanceResponse(boolean b) {
        this.logMessage("The instance has been deleted? " + String.valueOf(b));
    }

    @Override
    public void getDevicesResponse(DeviceList deviceList) {
        this.logMessage("Devices are: " + deviceList);
    }

    @Override
    public void putDeviceResponse(Device device) {
        this.logMessage("Received a PUT for device: " + device);
    }

    @Override
    public void getDeviceResponse(Device device) {
        this.logMessage("The device is: " + device);
    }

    @Override
    public void getDeviceCommandResponse(String s) {
        this.logMessage("The device command is: " + s);
    }

    @Override
    public void getLocationsResponse(LocationList locationList) {
        this.logMessage("Locations are: " + locationList);
    }

    @Override
    public void postLocationResponse(Location location) {
        this.logMessage("Received a POST for location: " + location);
    }

    @Override
    public void getLocationResponse(Location location) {
        this.logMessage("The location is: " + location);
    }

    @Override
    public void putLocationResponse(Location location) {
        this.logMessage("Received a PUT for location: " + location);
    }

    @Override
    public void deleteLocationResponse(boolean b) {
        this.logMessage("Location has been deleted? " + b);
    }

    @Override
    public void getProfilesResponse(ProfileList profileList) {
        this.logMessage("Profiles are: " + profileList);
    }

    @Override
    public void postProfileResponse(Profile profile) {
        this.logMessage("Received a POST for profile: " + profile);
    }

    @Override
    public void getProfileResponse(Profile profile) {
        this.logMessage("The profile is: " + profile);
    }

    @Override
    public void putProfileResponse(Profile profile) {
        this.logMessage("Received a PUT for profile: " + profile);
    }

    @Override
    public void deleteProfileResponse(boolean b) {
        this.logMessage("Profile has been deleted? " + b);
    }

    @Override
    public void getNotificationsResponse(NotificationList notificationList) {
        this.logMessage("Notifications are: " + notificationList);
    }

    @Override
    public void getNotificationResponse(Notification notification) {
        this.logMessage("The notification is: " + notification);
    }

    @Override
    public void putNotificationResponse(Notification notification) {
        this.logMessage("Received a PUT for notification: " + notification);
    }

    @Override
    public void getDeviceHistoriesResponse(DeviceHistoryList deviceHistoryList) {
        this.logMessage("Device histories are: " + deviceHistoryList);
    }

    @Override
    public void getDeviceHistoryResponse(DeviceHistory deviceHistory) {
        this.logMessage("The device history is: " + deviceHistory);
    }

    @Override
    public void getZWaveDeviceResponse(ZWaveDevice zWaveDevice) {
        this.logMessage("The Z-Wave device is: " + zWaveDevice);
    }

    @Override
    public void getZWaveControllerResponse(ZWaveController zWaveController) {
        this.logMessage("The Z-Wave controller is: " + zWaveController);
    }

    @Override
    public void apiError(String s, boolean b) {
        this.logError("API Error: " + s);
    }

    @Override
    public void httpStatusError(int i, String s, boolean b) {
        this.logError("HTTP Status Error: " + i + s);
    }

    @Override
    public void authenticationError() {
        this.logError("Authentication Error");
    }

    @Override
    public void responseFormatError(String s, boolean b) {
        this.logError("Wrong format: " + s);
    }

    @Override
    public void message(int i, String s) {
        this.logMessage("You've got a message: " + i + " " + s);
    }

    /**
     * Utility method to print the log messages of this class.
     *
     * @param message the {@link String} to print
     */
    private void logMessage(String message) {
        Logger logger = LoggerFactory.getLogger(Sensor.class);
        logger.debug(message);
    }

    /**
     * Utility method to print the error messages of this class.
     *
     * @param error the {@link String} to print
     */
    private void logError(String error) {
        Logger logger = LoggerFactory.getLogger(Sensor.class);
        logger.error(error);
    }
}