package org.helpapaw.helpapaw.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.backendless.Backendless;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.NotificationUtils;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends Application {
    
    // TODO: before creating the PR, adjust this
//    public static final String  BACKENDLESS_APP_ID          = "BDCD56B9-351A-E067-FFA4-9EA9CF2F4000";
//    public static final String  BACKENDLESS_REST_API_KEY    = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";
//    private static final String BACKENDLESS_ANDROID_API_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";
//    private static final String IS_TEST_ENVIRONMENT_KEY     = "IS_TEST_ENVIRONMENT_KEY";

    // STAGING DB
    public static final String  BACKENDLESS_APP_ID          = "A2E71FD3-94A4-E6FE-FFFD-A881FCD2AA00";
    public static final String  BACKENDLESS_REST_API_KEY    = "B061AC2D-43F3-4F3C-B3F5-0C45673F82DF";
    private static final String BACKENDLESS_ANDROID_API_KEY = "02D2374C-C85C-442A-BAE8-E5C7A8DBBD4A";
    private static final String IS_TEST_ENVIRONMENT_KEY     = "IS_TEST_ENVIRONMENT_KEY";

    private static Boolean isTestEnvironment;
    private static PawApplication pawApplication;

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
