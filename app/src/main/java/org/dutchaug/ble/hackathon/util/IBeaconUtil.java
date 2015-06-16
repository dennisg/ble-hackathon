package org.dutchaug.ble.hackathon.util;


import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility class to deconstruct the scanRecord (BLE advertisement).
 */
public class IBeaconUtil {

    private IBeaconUtil() {}

    public static double getRange(int txCalibratedPower, int rssi) {
        int ratio_db = txCalibratedPower - rssi;
        double ratio_linear = Math.pow(10, ratio_db / 10.);

        return Math.sqrt(ratio_linear);
    }

    public static boolean isIBeacon(byte[] scanRecord) {
        ByteBuffer tmp = ByteBuffer.wrap(scanRecord);
        //ignore the flag, can be 0x06 or 0x1A
        return (((tmp.getLong() & 0x0201001aff4c0002L) == 0x0201001aff4c0002L) && (tmp.get() & 0xff) == 0x15);
    }

    public static UUID proximityUUID(byte[] scanRecord) {
        ByteBuffer tmp = ByteBuffer.wrap(scanRecord);

        tmp.position(9); //skip the header
        StringBuilder buf = new StringBuilder();
        for (int i=0;i<8;i++) {
            String str = Integer.toHexString(tmp.getShort() & 0xFFFF);
            buf.append(str);
            if (i > 0 && i < 5) buf.append("-");
        }

        return UUID.fromString(buf.toString().toUpperCase());
    }

    public static short major(byte[] scanRecord) {
        return ByteBuffer.wrap(scanRecord).getShort(25);
    }

    public static short minor(byte[] scanRecord) {
        return ByteBuffer.wrap(scanRecord).getShort(27);
    }

    public static int power(byte[] scanRecord) {
        return ByteBuffer.wrap(scanRecord).get(29);
    }

}
