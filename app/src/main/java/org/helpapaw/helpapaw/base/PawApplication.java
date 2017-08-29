package org.helpapaw.helpapaw.base;

import android.app.Application;

import com.backendless.Backendless;

import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends Application {
    public static final String YOUR_APP_ID = "7381F40A-5BA6-6CB5-FF82-1F0334A63B00";
    private static final String YOUR_SECRET_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";
    public static final String YOUR_APP_VERSION = "v1";

    public static final Boolean TEST_VERSION = true;

    private static PawApplication pawApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        pawApplication = this;
        Backendless.initApp(this, YOUR_APP_ID, YOUR_SECRET_KEY, YOUR_APP_VERSION);

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
