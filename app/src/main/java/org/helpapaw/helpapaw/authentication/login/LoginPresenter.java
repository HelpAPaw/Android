package org.helpapaw.helpapaw.authentication.login;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.PrivacyPolicyConfirmationContract;
import org.helpapaw.helpapaw.authentication.PrivacyPolicyConfirmationGetter;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

/**
 * Created by iliyan on 7/25/16
 */
public class LoginPresenter extends Presenter<LoginContract.View>
        implements LoginContract.UserActionsListener,
        PrivacyPolicyConfirmationContract.Obtain,
        PrivacyPolicyConfirmationContract.UserResponse {
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
        setProgressIndicator(true);
        userManager.getHasAcceptedPrivacyPolicy(new UserManager.GetUserPropertyCallback() {
            @Override
            public void onSuccess(Object hasAcceptedPrivacyPolicy) {

                if (!((Boolean) hasAcceptedPrivacyPolicy)) {
                    PrivacyPolicyConfirmationGetter privacyPolicyConfirmationGetter = new PrivacyPolicyConfirmationGetter(LoginPresenter.this, PawApplication.getContext());
                    privacyPolicyConfirmationGetter.execute();
                }
                else {
                    if (!isViewAvailable()) return;
                    getView().closeLoginScreen();
                }
            }

            @Override
            public void onFailure(String message) {
                if (!isViewAvailable()) return;
                setProgressIndicator(false);
                getView().showErrorMessage(message);
            }
        });
    }

    private void onLoginFailure(String message) {
        if (!isViewAvailable()) return;
        setProgressIndicator(false);
        getView().showErrorMessage(message);
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

    @Override
    public void onPrivacyPolicyObtained(String privacyPolicy) {
        if (!isViewAvailable()) return;
        setProgressIndicator(false);

        if (privacyPolicy != null) {
            getView().showPrivacyPolicyDialog(privacyPolicy);
        }
        else {
            getView().showErrorMessage(PawApplication.getContext().getString(R.string.txt_error_getting_privacy_policy));
        }
    }

    @Override
    public void onUserAcceptedPrivacyPolicy() {
        setProgressIndicator(true);
        userManager.setHasAcceptedPrivacyPolicy(true, new UserManager.SetUserPropertyCallback() {
            @Override
            public void onSuccess() {
                if (!isViewAvailable()) return;
                getView().closeLoginScreen();
            }

            @Override
            public void onFailure(String message) {
                if (!isViewAvailable()) return;
                setProgressIndicator(false);
                getView().showErrorMessage(message);
            }
        });
    }

    @Override
    public void onUserDeclinedPrivacyPolicy() {
        setProgressIndicator(true);
        userManager.logout(new UserManager.LogoutCallback() {
            @Override
            public void onLogoutSuccess() {
                setProgressIndicator(false);
            }

            @Override
            public void onLogoutFailure(String message) {
                setProgressIndicator(false);
            }
        });
    }
}
