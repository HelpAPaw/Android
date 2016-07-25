package org.helpapaw.helpapaw.authentication.login;

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

        void openSignalsMapScreen();

        void setProgressIndicator(boolean active);

        void hideKeyboard();

    }

    interface UserActionsListener {

        void onLoginButtonClicked(String email, String password);

        void onRegisterButtonClicked();

    }
}
