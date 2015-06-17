package org.dutchaug.ble.hackathon.ble;

import java.util.UUID;

/**
 * Created by dennisg on 11/02/15.
 */
public class BluetoothServices {

    private static final String OFFICIAL_FORMAT = "0000%s-0000-1000-8000-00805f9b34fb";
    private static final String SENSORTAG_FORMAT = "F000%s-0451-4000-B000-000000000000";

    public static final UUID GenericAccessService = official("1800");
    public static final UUID CurrentTimeService = official("1805");

    public static final UUID DeviceInformationService = official("180A");
    public static final UUID BatteryService = official("180F");
    public static final UUID LocationAndNavigationService = official("1819");


    static final UUID official(String id) {
        return UUID.fromString(String.format(OFFICIAL_FORMAT, id));
    }

    public static final UUID sensorTag(String id) {
        return UUID.fromString(String.format(SENSORTAG_FORMAT, id));
    }


}
