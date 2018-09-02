package org.helpapaw.helpapaw.authentication.login;

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

    LoginPresenter(LoginContract.View view) {
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

        if (isEmpty(email) || !Utils.getInstance().isEmailValid(email)) {
            getView().showEmailErrorMessage();
            return;
        }

        if (isEmpty(password) || password.length() < MIN_PASS_LENGTH) {
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
                    LoginPresenter.this.onLoginSuccess();
                }

                @Override
                public void onLoginFailure(String message) {
                    LoginPresenter.this.onLoginFailure(message);
                }
            });
        } else {
            getView().showNoInternetMessage();
            setProgressIndicator(false);
        }
    }

    private void onLoginSuccess() {
        if (!isViewAvailable()) return;
        getView().closeLoginScreen();
    }

    private void onLoginFailure(String message) {
        if (!isViewAvailable()) return;
        setProgressIndicator(false);
        getView().showMessage(message);
    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    @Override
    public void onRegisterButtonClicked() {
        getView().openRegisterScreen();
    }

    private boolean isEmpty(String value) {
        return !(value != null && value.length() > 0);
    }

    @Override
    public void onLoginFbSuccess(String accessToken) {
        if (Utils.getInstance().hasNetworkConnection()) {
            setProgressIndicator(true);
            userManager.loginWithFacebook(accessToken, new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    setProgressIndicator(false);
                    LoginPresenter.this.onLoginSuccess();
                }

                @Override
                public void onLoginFailure(String message) {
                    setProgressIndicator(false);
                    LoginPresenter.this.onLoginFailure(message);
                }
            });
        } else {
            getView().showNoInternetMessage();
        }
    }
}
