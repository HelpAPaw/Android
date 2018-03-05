package org.helpapaw.helpapaw.base;

import android.app.Application;
import android.os.StrictMode;

import com.backendless.Backendless;

import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.NotificationUtils;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends Application {
    public static final String  BACKENDLESS_APP_ID          = "***REMOVED***";
    public static final String  BACKENDLESS_REST_API_KEY    = "***REMOVED***";
    private static final String BACKENDLESS_ANDROID_API_KEY = "***REMOVED***";

    public static final Boolean TEST_VERSION = false;

    private static PawApplication pawApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        pawApplication = this;
        Backendless.initApp(this, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_API_KEY);
        NotificationUtils.registerNotificationChannels(this);

        // This is done in order to handle the situation where user token is saved on the device but is invalidated on the server
        final UserManager userManager = Injection.getUserManagerInstance();
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                // Do nothing
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
}
