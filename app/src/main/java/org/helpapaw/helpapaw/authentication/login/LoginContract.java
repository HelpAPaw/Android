package org.helpapaw.helpapaw.authentication.login;

import android.app.Activity;

import com.facebook.CallbackManager;

/**
 * Created by iliyan on 7/25/16
 */
public interface LoginContract {

    interface View {

        void showMessage(String message);

        void showEmailErrorMessage();

        void showPasswordErrorMessage();

        void clearErrorMessages();

        void openRegisterScreen();

        void setProgressIndicator(boolean active);

        void hideKeyboard();

        void closeLoginScreen();

        void showNoInternetMessage();

        boolean isActive();

    }

    interface UserActionsListener {

        void onInitLoginScreen();

        void onLoginButtonClicked(String email, String password);

        void onRegisterButtonClicked();

        void onLoginFbButtonClicked(Activity activity, CallbackManager callbackManager);
    }
}
