package org.helpapaw.helpapaw.authentication.login;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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

    private final UserManager userManager;
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
                public void onLoginSuccess(String userId) {
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

                try {
                    Boolean accepted = (Boolean) hasAcceptedPrivacyPolicy;
                    if (!accepted) {
                        PrivacyPolicyConfirmationGetter privacyPolicyConfirmationGetter = new PrivacyPolicyConfirmationGetter(LoginPresenter.this, PawApplication.getContext());
                        privacyPolicyConfirmationGetter.execute();
                    }
                    else {
                        if (!isViewAvailable()) return;
                        getView().closeLoginScreen();
                    }
                }
                catch (Exception ignored) {
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
        if (isViewAvailable()) {
            getView().setProgressIndicator(active);
        }
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
                public void onLoginSuccess(String userId) {
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

    @Override
    public void onForgotPasswordButtonClicked() {
        getView().showPasswordResetConfirmationDialog();
    }

    @Override
    public void onPasswordResetRequested(String email) {
        getView().clearErrorMessages();

        if (isEmpty(email) || !Utils.getInstance().isEmailValid(email)) {
            getView().showEmailErrorMessage();
            return;
        }

        getView().hideKeyboard();
        setProgressIndicator(true);
        sendResetPasswordRequest(email);
    }

    private void sendResetPasswordRequest(String email) {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.resetPassword(email, new UserManager.ResetPasswordCallback() {
                @Override
                public void onResetPasswordSuccess() {
                    if (!isViewAvailable()) return;
                    setProgressIndicator(false);
                    getView().showPasswordResetSuccessfulMessage();
                }

                @Override
                public void onResetPasswordFailure(String message) {
                    if (!isViewAvailable()) return;
                    setProgressIndicator(false);
                    getView().showErrorMessage(message);
                }
            });
        } else {
            getView().showNoInternetMessage();
            setProgressIndicator(false);
        }
    }

    @Override
    public void handleSignInWithGoogleResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            setProgressIndicator(true);
            userManager.loginWithGoogle(account, new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess(String userId) {
                    LoginPresenter.this.onLoginSuccess();
                }

                @Override
                public void onLoginFailure(String message) {
                    LoginPresenter.this.onLoginFailure(message);
                }
            });
        } catch (ApiException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            if (!isViewAvailable()) return;
            getView().showErrorMessage(e.getLocalizedMessage());
        }
    }
}
