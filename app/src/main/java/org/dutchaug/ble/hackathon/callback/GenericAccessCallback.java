package org.dutchaug.ble.hackathon.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.dutchaug.ble.hackathon.ble.GenericAccessService;

import timber.log.Timber;

/**
 * Created by dennisg on 17/06/15.
 */
public class GenericAccessCallback extends BluetoothGattCallback {




    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTING:
                    Timber.i("GATT Connecting");
                    break;
                case BluetoothGatt.STATE_CONNECTED:
                    Timber.i("GATT Connected");
                    gatt.discoverServices();
                    break;
                case BluetoothGatt.STATE_DISCONNECTING:
                    Timber.i("GATT Disconnecting");
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    Timber.i("GATT Disconnected");
                    gatt.close();
                    break;
            }
        }
    }


    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            //ready, let's read
            BluetoothGattService svc = gatt.getService(GenericAccessService.Service);
            if (svc != null) {
                BluetoothGattCharacteristic ch = svc.getCharacteristic(GenericAccessService.DeviceName);
                if (ch != null) {
                    boolean response = gatt.readCharacteristic(ch);
                    Timber.i("Read was successful: %s", response);
                } else {
                    Timber.w("Reading Device Name failed");
                }
            }
        }

    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Timber.i("Read Device Name: %s", GenericAccessService.getDeviceName(characteristic.getValue()));
            gatt.disconnect();
        }
    }


}
