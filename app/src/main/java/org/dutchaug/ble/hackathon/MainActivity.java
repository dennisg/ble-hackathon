package org.dutchaug.ble.hackathon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.dutchaug.ble.hackathon.callback.GenericAccessCallback;
import org.dutchaug.ble.hackathon.receiver.BLuetoothDeviceReceiver;
import org.dutchaug.ble.hackathon.receiver.IBeaconReceiver;
import org.dutchaug.ble.hackathon.receiver.UriBeaconReceiver;
import org.dutchaug.ble.hackathon.service.BluetoothDetectionService;

import java.util.UUID;

import timber.log.Timber;


public class MainActivity extends Activity implements BLuetoothDeviceReceiver.BluetoothDeviceDetection, IBeaconReceiver.IBeaconDetection, UriBeaconReceiver.UriBeaconDetection {

    private static final int REQUEST_ENABLE_BT = 1;

    private BroadcastReceiver ibeacons;
    private BroadcastReceiver uribeacons;
    private BroadcastReceiver devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.plant(new Timber.DebugTree());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ibeacons = IBeaconReceiver.register(this, this);
        uribeacons = UriBeaconReceiver.register(this, this);
        devices = BLuetoothDeviceReceiver.register(this, this);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            //all is well
            onActivityResult(REQUEST_ENABLE_BT, RESULT_OK, null);
        }
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, BluetoothDetectionService.class));
        unregisterReceiver(devices);
        unregisterReceiver(uribeacons);
        unregisterReceiver(ibeacons);

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            startService(new Intent(this, BluetoothDetectionService.class));
        }
    }

    public void onBluetoothDevice(BluetoothDevice device) {
        Timber.i("BluetoothDevice: %s", device.getAddress());

        String deviceName = device.getName();
        if (deviceName != null && deviceName.startsWith("SensorTag")) {
            device.connectGatt(this, false, new GenericAccessCallback());
        }
    }

    @Override
    public void onIBeacon(UUID uuid, short major, short minor, double range) {
        Timber.i("IBeacon found: %s (%d ,%d)", uuid, major, minor);
    }

    @Override
    public void onUriBeacon(String uri) {
        Timber.i("UriBeacon found: %s", uri);
    }
}
