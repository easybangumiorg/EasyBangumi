package com.zane.androidupnpdemo.log;

import android.util.Log;

import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Description：How to configure java.util.logging on Android?
 * <p>
 * see https://stackoverflow.com/questions/4561345/how-to-configure-java-util-logging-on-android/9047282#9047282
 * <p>
 * <BR/>
 * Creator：yankebin
 * <BR/>
 * CreatedAt：2019-07-19
 */

public final class AndroidLoggingHandler extends Handler {

    public static void injectJavaLogger() {
        Enumeration<String> e = LogManager.getLogManager().getLoggerNames();
        while(e.hasMoreElements()){
            String name = e.nextElement();
            Logger rootLogger = LogManager.getLogManager().getLogger(name);
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            rootLogger.addHandler(new AndroidLoggingHandler());
            Logger.getLogger(name).setLevel(Level.FINEST);
        }

    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
        if (!super.isLoggable(record))
            return;

        String name = record.getLoggerName();
        int maxLength = 30;
        String tag = name.length() > maxLength ? name.substring(name.length() - maxLength) : name;

        try {
            int level = getAndroidLevel(record.getLevel());
            Log.println(level, tag, record.getMessage());
            if (record.getThrown() != null) {
                Log.println(level, tag, Log.getStackTraceString(record.getThrown()));
            }
        } catch (RuntimeException e) {
            Log.e("AndroidLoggingHandler", "Error logging message.", e);
        }
    }

    static int getAndroidLevel(Level level) {
        int value = level.intValue();

        if (value >= Level.SEVERE.intValue()) {
            return Log.ERROR;
        } else if (value >= Level.WARNING.intValue()) {
            return Log.WARN;
        } else if (value >= Level.INFO.intValue()) {
            return Log.INFO;
        } else {
            return Log.DEBUG;
        }
    }
}