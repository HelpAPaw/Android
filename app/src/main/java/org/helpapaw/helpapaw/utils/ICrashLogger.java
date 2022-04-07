package org.helpapaw.helpapaw.utils;

import androidx.annotation.NonNull;

public interface ICrashLogger {
    void recordException(@NonNull Throwable throwable);

    void log(@NonNull String message);

    void setUserId(@NonNull String identifier);
}
