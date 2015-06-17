package org.dutchaug.ble.hackathon.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.uribeacon.beacon.UriBeacon;
import org.dutchaug.ble.hackathon.service.BluetoothDetectionService;

public class UriBeaconReceiver extends BroadcastReceiver {

    private final UriBeaconDetection listener;

    public interface UriBeaconDetection {
        void onUriBeacon(String uri);
    }

    private UriBeaconReceiver(UriBeaconDetection l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDetectionService.ACTION_DEVICE_DETECTED)) {

            byte[] scanRecord = intent.getByteArrayExtra(BluetoothDetectionService.EXTRA_SCAN_RECORD);
            UriBeacon beacon = UriBeacon.parseFromBytes(scanRecord);
            if (beacon != null) {
                listener.onUriBeacon(beacon.getUriString());
            }
        }
    }




    public static BroadcastReceiver register(Context ctx, UriBeaconDetection l) {
        BroadcastReceiver receiver = new UriBeaconReceiver(l);
        IntentFilter filter = new IntentFilter(BluetoothDetectionService.ACTION_DEVICE_DETECTED);
        ctx.registerReceiver(receiver, filter);
        return receiver;
    }
}
