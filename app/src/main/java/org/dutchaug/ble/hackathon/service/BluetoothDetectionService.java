package org.dutchaug.ble.hackathon.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
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

    private volatile boolean running = false;

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
        running = false;
        super.onDestroy();
    }

    @Override
    public synchronized Void call() throws Exception {
        running = true;

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        while (running) {
            try {
                detect(adapter);
            } catch (Exception e) {
                Timber.e(e, "Stopping BLE detection");
                //stop the service
                running = false;
            }
            TimeUnit.SECONDS.sleep(3);
        }

        return null;
    }

    private void detect(BluetoothAdapter adapter) throws Exception {
        adapter.startLeScan(this);
        TimeUnit.SECONDS.sleep(5);
        adapter.stopLeScan(this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        Intent broadcast = new Intent(BluetoothDetectionService.ACTION_DEVICE_DETECTED);
        broadcast.putExtra(EXTRA_DEVICE, device);
        broadcast.putExtra(EXTRA_RSSI, rssi);
        broadcast.putExtra(EXTRA_SCAN_RECORD, scanRecord);

        //Timber.i("broadcast... %s", device);
        sendBroadcast(broadcast);
    }
}
