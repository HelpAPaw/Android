package org.helpapaw.helpapaw.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.backendless.Backendless;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.NotificationUtils;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends MultiDexApplication {

    public static final String  BACKENDLESS_APP_ID          = "***REMOVED***";
    public static final String  BACKENDLESS_REST_API_KEY    = "***REMOVED***";
    private static final String BACKENDLESS_ANDROID_API_KEY = "***REMOVED***";
    private static final String IS_TEST_ENVIRONMENT_KEY     = "IS_TEST_ENVIRONMENT_KEY";

    private static Boolean isTestEnvironment;
    private static PawApplication pawApplication;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pawApplication = this;
        isTestEnvironment = loadIsTestEnvironment();
        Backendless.initApp(this, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_API_KEY);

        // Register device for token
        Injection.getPushNotificationsRepositoryInstance().registerDeviceToken();

        // This is done in order to handle the situation where user token is saved on the device but is invalidated on the server
        final UserManager userManager = Injection.getUserManagerInstance();
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess(String userId) {
                FirebaseCrashlytics.getInstance().setUserId(userId);
            }

            @Override
            public void onLoginFailure(String message) {
                userManager.logout(new UserManager.LogoutCallback() {
                    @Override
                    public void onLogoutSuccess() {}

                    @Override
                    public void onLogoutFailure(String message) {}
                });
            }
        });

        // Prevent android.os.FileUriExposedException on API 24+
        // https://stackoverflow.com/a/45569709/2781218
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static PawApplication getContext() {
        return pawApplication;
    }

    public static Boolean getIsTestEnvironment() {
        return isTestEnvironment;
    }

    public static void setIsTestEnvironment(Boolean isTestEnvironment) {
        PawApplication.isTestEnvironment = isTestEnvironment;
        pawApplication.saveIsTestEnvironment(isTestEnvironment);
    }

    private Boolean loadIsTestEnvironment() {
        SharedPreferences prefs = getSharedPreferences("HelpAPaw", MODE_PRIVATE);
        return prefs.getBoolean(IS_TEST_ENVIRONMENT_KEY, false);
    }

    private void saveIsTestEnvironment(Boolean isTestEnvironment) {
        SharedPreferences prefs = pawApplication.getSharedPreferences("HelpAPaw", MODE_PRIVATE);
        prefs.edit().putBoolean(IS_TEST_ENVIRONMENT_KEY, isTestEnvironment).apply();
    }
}
