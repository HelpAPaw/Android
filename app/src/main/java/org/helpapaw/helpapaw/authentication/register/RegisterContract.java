package org.helpapaw.helpapaw.authentication.register;

/**
 * Created by iliyan on 7/25/16
 */
public interface RegisterContract {

    interface View {

        void showErrorMessage(String message);

        void showEmailErrorMessage();

        void showPasswordErrorMessage();

        void showPasswordConfirmationErrorMessage();

        void showNameErrorMessage();

        void showWhyPhoneDialog();

        void showPrivacyPolicyDialog(String privacyPolicy);

        void clearErrorMessages();

        void hideKeyboard();

        void setProgressIndicator(boolean active);

        void showRegistrationSuccessfulMessage();

        void closeRegistrationScreen();

        void showNoInternetMessage();

        boolean isActive();
    }

    interface UserActionsListener {

        void onInitRegisterScreen();

        void onRegisterButtonClicked(String email, String password, String passwordConfirmation, String name, String phoneNumber);

        void onLoginButtonClicked();

        void onWhyPhoneButtonClicked();

    }
}
