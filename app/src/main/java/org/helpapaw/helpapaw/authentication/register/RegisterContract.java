package org.helpapaw.helpapaw.authentication.register;

/**
 * Created by iliyan on 7/25/16
 */
public interface RegisterContract {

    interface View {

        void showMessage(String message);

        void showEmailErrorMessage();

        void showPasswordErrorMessage();

        void showNameErrorMessage();

        void showWhyPhoneDialog();

        void clearErrorMessages();

        void hideKeyboard();

        void setProgressIndicator(boolean active);

        void closeRegistrationScreen();

        boolean isActive();

    }

    interface UserActionsListener {

        void onInitRegisterScreen();

        void onRegisterButtonClicked(String email, String password, String name, String phoneNumber);

        void onLoginButtonClicked();

        void onWhyPhoneButtonClicked();

    }
}
