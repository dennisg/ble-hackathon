package org.dutchaug.ble.hackathon.ble;

import java.util.UUID;

/**
 * Created by dennisg on 11/02/15.
 */
public class GenericAccessService {

    public static final UUID Service = BluetoothServices.GenericAccessService;

    public static final UUID DeviceName = BluetoothServices.official("2A00");

    public static final UUID[] ALL = {DeviceName};

    public static String getDeviceName(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return "Unknown";
        }
        return new String(raw);
    }


}
