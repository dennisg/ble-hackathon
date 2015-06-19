package org.dutchaug.ble.hackathon.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * scans for BLE devices, and notifies using broadcasts.
 */
public class BluetoothDetectionService extends Service implements Callable<Void>, BluetoothAdapter.LeScanCallback {

    public static final String ACTION_DEVICE_DETECTED = "org.dutchaug.ble.DEVICE_DETECTED";
    public static final String EXTRA_DEVICE = BluetoothDevice.EXTRA_DEVICE;
    public static final String EXTRA_RSSI = BluetoothDevice.EXTRA_RSSI;
    public static final String EXTRA_SCAN_RECORD = "EXTRA_SCAN_RECORD";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final BroadcastReceiver receiver = new BluetoothStateReceiver();

    private volatile boolean running = false;
    private volatile boolean skipScanning = false;

    private long TIME_SCANNING = 5; //in seconds
    private long TIME_IDLE = 3; //in seconds

    public BluetoothDetectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            executorService.submit(this);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false; //stops the background thread
        super.onDestroy();
    }

    @Override
    public synchronized Void call() throws Exception {
        running = true;

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        if (adapter == null) {
            Timber.w("No Bluetooth adapter found");
            return null;
        }

        if (!adapter.isEnabled()) {
            Timber.w("Bluetooth adapter is disabled");
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);

        try {
            while (running) {
                running = detect(adapter);
                TimeUnit.SECONDS.sleep(TIME_IDLE);
            }
        } finally {
            unregisterReceiver(receiver);
        }

        return null;
    }

    private boolean detect(BluetoothAdapter adapter) {
        adapter.startLeScan(this);
        try {
            TimeUnit.SECONDS.sleep(TIME_SCANNING);
        }
        catch (InterruptedException e) {
            return false;
        }
        adapter.stopLeScan(this);

        return true;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        Intent broadcast = new Intent(BluetoothDetectionService.ACTION_DEVICE_DETECTED);
        broadcast.putExtra(EXTRA_DEVICE, device);
        broadcast.putExtra(EXTRA_RSSI, rssi);
        broadcast.putExtra(EXTRA_SCAN_RECORD, scanRecord);

        sendBroadcast(broadcast);
    }

    private class BluetoothStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)) {
                    case BluetoothAdapter.STATE_OFF:
                        //adapter is disabled
                    case BluetoothAdapter.STATE_CONNECTING:
                    case BluetoothAdapter.STATE_CONNECTED:
                        //adapter is connected to via GATT to a device
                    case BluetoothAdapter.STATE_TURNING_ON:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        skipScanning = true;
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                    case BluetoothAdapter.STATE_DISCONNECTING:
                    case BluetoothAdapter.STATE_ON:
                        skipScanning = false;
                        break;
                }
            }
        }
    }

}
