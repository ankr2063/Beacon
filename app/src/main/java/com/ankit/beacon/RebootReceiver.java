package com.ankit.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RebootReceiver extends BroadcastReceiver {

    private static final Logger sLogger = LoggerFactory.getLogger(RebootReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                sLogger.info("Android boot completed, restarting beacon broadcast...");
                final BluetoothAdapter mBluetoothAdapter = ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                final boolean btResult = mBluetoothAdapter.enable();
                if (btResult) {
                    sLogger.info("Enabling Bluetooth interface for beacon broadcast");
                }
                else {
                    sLogger.warn("Could not enable Bluetooth interface at reboot for beacon broadcast");
                }
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED: {
                final int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                sLogger.debug("Bluetooth state changed: {}", btState);
                switch (btState) {
                    case BluetoothAdapter.STATE_ON:
                        sLogger.info("Bluetooth is ON, restarting beacon broadcast");
                        App.getInstance().startResilientBeacons();
                        break;
                }
                break;
            }
        }
    }

}
