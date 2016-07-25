package org.helpapaw.helpapaw.authentication.register;

/**
 * Created by iliyan on 7/25/16
 */
public interface RegisterContract {

    interface View {

        void showErrorMessage(String message);

        void showEmailErrorMessage();

        void showPasswordErrorMessage();

        void clearErrorMessages();

        void hideKeyboard();

        void setProgressIndicator(boolean active);

        void openLoginScreen();

    }

    interface UserActionsListener {

        void onRegisterButtonClicked(String email, String password, String name, String phoneNumber);

        void onLoginButtonClicked();

    }
}
