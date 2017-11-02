package org.helpapaw.helpapaw.base;

import android.app.Application;

import com.backendless.Backendless;

import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends Application {
    public static final String  BACKENDLESS_APP_ID          = "BDCD56B9-351A-E067-FFA4-9EA9CF2F4000";
    private static final String BACKENDLESS_ANDROID_API_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";
    public static final String BACKENDLESS_REST_API_KEY    = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";

    public static final Boolean TEST_VERSION = true;

    private static PawApplication pawApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        pawApplication = this;
        Backendless.initApp(this, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_API_KEY);

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
    }

    public static PawApplication getContext() {
        return pawApplication;
    }
}
