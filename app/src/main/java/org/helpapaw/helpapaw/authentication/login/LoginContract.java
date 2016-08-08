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

        void setProgressIndicator(boolean active);

        void hideKeyboard();

        void closeLoginScreen();

        boolean isActive();
    }

    interface UserActionsListener {

        void onInitLoginScreen();

        void onLoginButtonClicked(String email, String password);

        void onRegisterButtonClicked();

    }
}
