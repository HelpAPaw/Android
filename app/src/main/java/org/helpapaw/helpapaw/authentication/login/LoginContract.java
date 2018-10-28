package org.helpapaw.helpapaw.authentication.login;

/**
 * Created by iliyan on 7/25/16
 */
public interface LoginContract {

    interface View {

        void showErrorMessage(String message);

        void showEmailErrorMessage();

        void showPasswordErrorMessage();

        void clearErrorMessages();

        void openRegisterScreen();

        void setProgressIndicator(boolean active);

        void hideKeyboard();

        void showPrivacyPolicyDialog(String privacyPolicy);

        void closeLoginScreen();

        void showNoInternetMessage();

        boolean isActive();
    }

    interface UserActionsListener {

        void onInitLoginScreen();

        void onLoginButtonClicked(String email, String password);

        void onRegisterButtonClicked();

        void onLoginFbSuccess(String accessToken);
    }
}
