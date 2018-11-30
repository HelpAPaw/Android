package org.helpapaw.helpapaw.data.user;

/**
 * Created by iliyan on 7/25/16
 */
public interface UserManager {

    void login(String email, String password, LoginCallback loginCallback);

    void loginWithFacebook(String accessToken, LoginCallback loginCallback);

    void register(String email, String password, String name, String phoneNumber, RegistrationCallback registrationCallback);

    void logout(LogoutCallback logoutCallback);

    void isLoggedIn(LoginCallback loginCallback);

    String getUserToken();

    boolean isLoggedIn();

    void getUserName(final GetUserPropertyCallback getUserPropertyCallback);

    void getHasAcceptedPrivacyPolicy(GetUserPropertyCallback getUserPropertyCallback);
    void setHasAcceptedPrivacyPolicy(boolean value, SetUserPropertyCallback setUserPropertyCallback);

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

    interface GetUserPropertyCallback {
        void onSuccess(Object value);
        void onFailure(String message);
    }

    interface SetUserPropertyCallback {
        void onSuccess();
        void onFailure(String message);
    }
}
