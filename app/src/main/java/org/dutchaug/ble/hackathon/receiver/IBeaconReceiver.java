package org.dutchaug.ble.hackathon.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.dutchaug.ble.hackathon.util.IBeaconUtil;
import org.dutchaug.ble.hackathon.service.BluetoothDetectionService;

import java.util.UUID;

import timber.log.Timber;

public class IBeaconReceiver extends BroadcastReceiver {

    private final IBeaconDetection listener;

    public interface IBeaconDetection {
        void onIBeacon(UUID uuid, short major, short minor, double range);
    }

    private IBeaconReceiver(IBeaconDetection l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDetectionService.ACTION_DEVICE_DETECTED)) {
            byte[] scanRecord = intent.getByteArrayExtra(BluetoothDetectionService.EXTRA_SCAN_RECORD);
            int rssi = intent.getIntExtra(BluetoothDetectionService.EXTRA_RSSI, -1);

            if (IBeaconUtil.isIBeacon(scanRecord)) {
                UUID uuid = IBeaconUtil.proximityUUID(scanRecord);
                short major = IBeaconUtil.major(scanRecord);
                short minor = IBeaconUtil.minor(scanRecord);

                int p = IBeaconUtil.power(scanRecord);
                double range = IBeaconUtil.getRange(p, rssi);

                listener.onIBeacon(uuid, major, minor, range);
            }
        }
    }


    public static BroadcastReceiver register(Context ctx, IBeaconDetection l) {
        BroadcastReceiver receiver = new IBeaconReceiver(l);
        IntentFilter filter = new IntentFilter(BluetoothDetectionService.ACTION_DEVICE_DETECTED);
        ctx.registerReceiver(receiver, filter);
        return receiver;
    }

}
