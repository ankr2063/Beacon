package com.ankit.beacon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

public class Config {

    public final static String PREF_KEY_LANGUAGE = "pref_language";
    public final static String PREF_KEY_SCAN_KEEP_SCREEN_ON = "pref_scan_keep_screen_on";
    public final static String PREF_KEY_BROADCAST_RESILIENCE = "pref_broadcast_resilience";

    private final SharedPreferences mPrefs;

    public Config(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public Locale getLocale() {
        return new Locale(mPrefs.getString(PREF_KEY_LANGUAGE, Locale.getDefault().getLanguage()));
    }

    public boolean getBroadcastResilience() {
        return mPrefs.getBoolean(PREF_KEY_BROADCAST_RESILIENCE, false);
    }

    public void setBroadcastResilience(boolean resilient) {
        mPrefs
                .edit()
                .putBoolean(PREF_KEY_BROADCAST_RESILIENCE, resilient)
                .apply();
    }

    public boolean getKeepScreenOnForScan() {
        return mPrefs.getBoolean(PREF_KEY_SCAN_KEEP_SCREEN_ON, false);
    }

    public void setKeepScreenOnForScan(boolean value) {
        mPrefs
                .edit()
                .putBoolean(PREF_KEY_SCAN_KEEP_SCREEN_ON, value)
                .apply();
    }

}
