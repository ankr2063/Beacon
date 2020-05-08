package com.ankit.beacon.bluetooth.event;

import android.bluetooth.le.AdvertiseSettings;

import net.alea.beaconsimulator.bluetooth.ExtendedAdvertiseData;

import java.util.UUID;

public class BeaconChangedEvent {

    private final UUID mId;
    private final ExtendedAdvertiseData mAdData;
    private final AdvertiseSettings mAdSettings;

    public BeaconChangedEvent(UUID id, ExtendedAdvertiseData adData, AdvertiseSettings adSettings) {
        mId = id;
        mAdData = adData;
        mAdSettings = adSettings;
    }

    public UUID getBeaconId() {
        return mId;
    }

    public ExtendedAdvertiseData getAdvertiseData() {
        return mAdData;
    }

    public AdvertiseSettings getAdvertiseSettings() {
        return mAdSettings;
    }
}
