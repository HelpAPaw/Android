package org.helpapaw.helpapaw.authentication.login;

import android.text.TextUtils;
import android.util.Patterns;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

/**
 * Created by iliyan on 7/25/16
 */
public class LoginPresenter extends Presenter<LoginContract.View> implements LoginContract.UserActionsListener {
    private static final int MIN_PASS_LENGTH = 6;

    private UserManager userManager;

    private boolean showProgressBar;

    public LoginPresenter(LoginContract.View view) {
        super(view);
        userManager = Injection.getUserManagerInstance();
        showProgressBar = false;
    }

    @Override
    public void onInitLoginScreen() {
        setProgressIndicator(showProgressBar);
    }

    @Override
    public void onLoginButtonClicked(String email, String password) {
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
        setProgressIndicator(true);
        attemptToLogin(email, password);
    }

    private void attemptToLogin(String email, String password) {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.login(email, password, new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    getView().openSignalsMapScreen();
                }

                @Override
                public void onLoginFailure(String message) {
                    setProgressIndicator(false);
                    getView().showMessage(message);
                }
            });
        } else {
            getView().showMessage("No Internet connection!");
            setProgressIndicator(false);
        }
    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }

    @Override
    public void onRegisterButtonClicked() {
        getView().openRegisterScreen();
    }
}
