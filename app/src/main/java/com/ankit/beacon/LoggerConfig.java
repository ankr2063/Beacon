package com.ankit.beacon;

import com.ankit.beacon.android.slf4j.LoggerChannelLogcat;
import com.ankit.beacon.android.slf4j.PluggableLoggerFactory;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class LoggerConfig {

    public static void configLogger() {
        ((PluggableLoggerFactory) LoggerFactory.getILoggerFactory())
                .addLogger(new LoggerChannelLogcat(), Level.TRACE);
    }

}