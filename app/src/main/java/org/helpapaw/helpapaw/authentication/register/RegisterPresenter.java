package org.helpapaw.helpapaw.authentication.register;

import android.text.TextUtils;
import android.util.Patterns;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

/**
 * Created by iliyan on 7/25/16
 */
public class RegisterPresenter extends Presenter<RegisterContract.View> implements RegisterContract.UserActionsListener {

    private static final int MIN_PASS_LENGTH = 6;

    UserManager userManager;

    public RegisterPresenter(RegisterContract.View view) {
        super(view);
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onRegisterButtonClicked(String email, String password, String name, String phoneNumber) {
        getView().clearErrorMessages();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            getView().showEmailErrorMessage();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < MIN_PASS_LENGTH) {
            getView().showPasswordErrorMessage();
            return;
        }

        getView().hideKeyboard();
        getView().setProgressIndicator(true);
        attemptToRegister(email, password, name, phoneNumber);
    }

    private void attemptToRegister(String email, String password, String name, String phoneNumber) {
        userManager.register(email, password, name, phoneNumber, new UserManager.RegistrationCallback() {
            @Override
            public void onRegistrationSuccess() {
                getView().openLoginScreen();
            }

            @Override
            public void onRegistrationFailure(String message) {
                getView().setProgressIndicator(false);
                getView().showErrorMessage(message);
            }
        });
    }

    @Override
    public void onLoginButtonClicked() {
        getView().openLoginScreen();
    }
}
