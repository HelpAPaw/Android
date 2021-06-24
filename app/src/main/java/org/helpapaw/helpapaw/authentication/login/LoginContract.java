package org.helpapaw.helpapaw.authentication.login;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

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

        void showPasswordResetConfirmationDialog();

        void showPasswordResetSuccessfulMessage();

        boolean isActive();
    }

    interface UserActionsListener {

        void onInitLoginScreen();

        void onLoginButtonClicked(String email, String password);

        void onRegisterButtonClicked();

        void onForgotPasswordButtonClicked();

        void onPasswordResetRequested(String email);

        void onLoginFbSuccess(String accessToken);

        void handleSignInWithGoogleResult(Task<GoogleSignInAccount> completedTask);
    }
}
