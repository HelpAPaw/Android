package org.helpapaw.helpapaw.utils;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CrashLogger implements ICrashLogger {

    public void recordException(@NonNull Throwable throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable);
    }

    public void log(@NonNull String message) {
        FirebaseCrashlytics.getInstance().log(message);
    }

    public void setUserId(@NonNull String identifier) {
        FirebaseCrashlytics.getInstance().setUserId(identifier);
    }
}
