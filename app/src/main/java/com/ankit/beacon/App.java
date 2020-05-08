package com.ankit.beacon;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.ankit.beacon.bluetooth.BeaconStore;
import com.ankit.beacon.bluetooth.BtNumbers;
import com.ankit.beacon.bluetooth.event.BroadcastChangedEvent;
import com.ankit.beacon.bluetooth.model.BeaconSimulatorService;
import com.ankit.beacon.event.UserRequestStartEvent;
import com.ankit.beacon.event.UserRequestStopAllEvent;
import com.ankit.beacon.event.UserRequestStopEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class App extends Application {

    private static final Logger sLogger = LoggerFactory.getLogger(App.class);

    private static App sInstance;

    private ComponentName mRebootReceiverComponent;
    private PackageManager mPm;

    private BeaconStore mBeaconStore;
    private BtNumbers mBtNumbers;
    private Config mConfig;


    public static App getInstance() {
        return sInstance;
    }


    @Override
    public void onCreate() {
        sInstance = this;

        super.onCreate();

        LoggerConfig.configLogger();            // We can start to log starting here
        sLogger.info("Beacon simulator starting!");

        ////PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        ////LanguageSetting.setDefaultLanguage(Locale.getDefault().getLanguage());

        mRebootReceiverComponent = new ComponentName(this, RebootReceiver.class);
        mPm = getPackageManager();

        mBeaconStore = new BeaconStore(getApplicationContext());
        mConfig = new Config(this);

        sLogger.info("System language is: {}", Locale.getDefault().getLanguage());
        sLogger.info("Config - language: {}, resilient: {}, keep_screen: {}",
                getConfig().getLocale().getLanguage(), getConfig().getBroadcastResilience(), getConfig().getKeepScreenOnForScan());

        // Purge or process running list of beacon
        // It may be due to a crash, or a sudden device reboot
        if (mConfig.getBroadcastResilience() ) {
            startResilientBeacons();
        }
        else {
            mBeaconStore.removeAllActiveBeacons();
        }
        EventBus.getDefault().register(this);
    }


    public BeaconStore getBeaconStore() {
        return mBeaconStore;
    }


    public BtNumbers getBtNumbers() {
        if (mBtNumbers == null) {
            mBtNumbers = new BtNumbers(this);
        }
        return mBtNumbers;
    }


    public Config getConfig() {
        return mConfig;
    }


    // Before setContentView
    public void updateLanguage(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(getConfig().getLocale());
        res.updateConfiguration(conf, dm);
    }

    @Subscribe
    public void onUserRequestStartEvent(UserRequestStartEvent event) {
        mBeaconStore.putActiveBeacon(event.getId());
        if (mConfig.getBroadcastResilience()) {
            enableRebootResilience(true);
        }
    }

    @Subscribe
    public void onUserRequestStopEvent(UserRequestStopEvent event) {
        mBeaconStore.removeActiveBeacon(event.getId());
        if (mBeaconStore.activeBeacons().size() == 0) {
            enableRebootResilience(false);
        }
    }

    @Subscribe
    public void onUserRequestStopAllEvent(UserRequestStopAllEvent event) {
        mBeaconStore.removeAllActiveBeacons();
        enableRebootResilience(false);
    }

    @Subscribe
    public void onBeaconBroadcastChange(BroadcastChangedEvent event) {
        if (event.isFailure()) {
            mBeaconStore.removeActiveBeacon(event.getBeaconId());
        }
    }

    public void startResilientBeacons() {
        final Set<String> beacons = getBeaconStore().activeBeacons();
        sLogger.info("Start pending beacons: {}", beacons);
        if (beacons.size() != 0) {
            for(String beaconId : beacons) {
                BeaconSimulatorService.startBroadcast(this, UUID.fromString(beaconId), false);
            }
        }
        else {
            // No beacons in running list, resilient mode should be disabled until beacons are broadcasted
            enableRebootResilience(false);
        }
    }

    public void enableRebootResilience(boolean enable) {
        if (enable && mBeaconStore.activeBeacons().size() == 0) {
            // This method should not be cassed with true if there is not beacon running to avoid waking the app uselessly
            sLogger.debug("Reboot resilience asked, but no running beacons");
            return;
        }
        final int componentState = mPm.getComponentEnabledSetting(mRebootReceiverComponent);
        boolean lastState = false;
        switch (componentState) {
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                lastState = true;
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT: // Shoud be explicitely disabled by default in the manifest
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                lastState = false;
                break;
            default:
        }
        if (lastState == enable) {
            return;
        }
        final int status = enable
                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        mPm.setComponentEnabledSetting(mRebootReceiverComponent, status, PackageManager.DONT_KILL_APP);
        sLogger.info("Setting broadcast resilience to: {}", enable);
    }


}
