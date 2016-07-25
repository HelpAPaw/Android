package org.helpapaw.helpapaw.data.user;

/**
 * Created by iliyan on 7/25/16
 */
public interface UserManager {

    void login(String email, String password, LoginCallback loginCallback);

    void register(String email, String password, String name, String phoneNumber, RegistrationCallback registrationCallback);

    void logout(LogoutCallback logoutCallback);

    void isLoggedIn(LoginCallback loginCallback);


    interface LoginCallback {

        void onLoginSuccess();

        void onLoginFailure(String message);
    }

    interface RegistrationCallback {

        void onRegistrationSuccess();

        void onRegistrationFailure(String message);
    }

    interface LogoutCallback {

        void onLogoutSuccess();

        void onLogoutFailure(String message);
    }
}
