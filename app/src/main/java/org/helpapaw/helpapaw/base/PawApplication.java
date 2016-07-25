package org.helpapaw.helpapaw.base;

import android.app.Application;

import com.backendless.Backendless;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends Application {
    private static final String YOUR_APP_ID = "7381F40A-5BA6-6CB5-FF82-1F0334A63B00";
    private static final String YOUR_SECRET_KEY = "***REMOVED***";
    private static final String YOUR_APP_VERSION = "v1";

    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.initApp(this, YOUR_APP_ID, YOUR_SECRET_KEY, YOUR_APP_VERSION);
    }
}
