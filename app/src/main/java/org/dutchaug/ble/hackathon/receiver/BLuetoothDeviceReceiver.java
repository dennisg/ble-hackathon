package org.dutchaug.ble.hackathon.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.dutchaug.ble.hackathon.service.BluetoothDetectionService;
import org.dutchaug.ble.hackathon.util.IBeaconUtil;

import java.util.UUID;

public class BLuetoothDeviceReceiver extends BroadcastReceiver {

    private final BluetoothDeviceDetection listener;

    public interface BluetoothDeviceDetection {
        void onBluetoothDevice(BluetoothDevice device);
    }

    private BLuetoothDeviceReceiver(BluetoothDeviceDetection l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDetectionService.ACTION_DEVICE_DETECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            listener.onBluetoothDevice(device);
        }
    }


    public static BroadcastReceiver register(Context ctx, BluetoothDeviceDetection l) {
        BroadcastReceiver receiver = new BLuetoothDeviceReceiver(l);
        IntentFilter filter = new IntentFilter(BluetoothDetectionService.ACTION_DEVICE_DETECTED);
        ctx.registerReceiver(receiver, filter);
        return receiver;
    }

}
