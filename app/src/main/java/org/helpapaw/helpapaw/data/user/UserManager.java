package org.helpapaw.helpapaw.data.user;

import com.backendless.BackendlessUser;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by iliyan on 7/25/16
 */
public interface UserManager {

    void login(String email, String password, LoginCallback loginCallback);

    void loginWithFacebook(String accessToken, LoginCallback loginCallback);

    void loginWithGoogle(GoogleSignInAccount account, LoginCallback loginCallback);

    void register(String email, String password, String name, String phoneNumber, RegistrationCallback registrationCallback);

    void delete(String userId, final DeleteUserCallback deleteUserCallback);

    void update(String name, String phoneNumber, String password, UpdateUserCallback updateUserCallback);

    void resetPassword(String email, ResetPasswordCallback resetPasswordCallback);

    void logout(LogoutCallback logoutCallback);

    void isLoggedIn(LoginCallback loginCallback);

    String getUserToken();

    String getLoggedUserId();

    boolean isLoggedIn();

    BackendlessUser getCurrentUser();

    void getUserName(final GetUserPropertyCallback getUserPropertyCallback);

    void getUserPhone(final GetUserPropertyCallback getUserPropertyCallback);

    void getHasAcceptedPrivacyPolicy(GetUserPropertyCallback getUserPropertyCallback);
    void setHasAcceptedPrivacyPolicy(boolean value, SetUserPropertyCallback setUserPropertyCallback);

    interface LoginCallback {
        void onLoginSuccess(String userId);
        void onLoginFailure(String message);
    }

    interface RegistrationCallback {
        void onRegistrationSuccess();
        void onRegistrationFailure(String message);
    }

    interface UpdateUserCallback {
        void onUpdateUserSuccess();
        void onUpdateUserFailure(String message);
    }

    interface DeleteUserCallback {
        void onDeleteUserSuccess();
        void onDeleteUserFailure(String message);
    }

    interface ResetPasswordCallback {
        void onResetPasswordSuccess();
        void onResetPasswordFailure(String message);
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
