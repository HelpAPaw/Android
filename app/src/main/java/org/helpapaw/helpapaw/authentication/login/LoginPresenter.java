package org.helpapaw.helpapaw.authentication.login;

import android.app.Activity;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.facebook.CallbackManager;

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
                    if (!isViewAvailable()) return;
                    getView().closeLoginScreen();
                }

                @Override
                public void onLoginFailure(String message) {
                    if (!isViewAvailable()) return;
                    setProgressIndicator(false);
                    getView().showMessage(message);
                }
            });
        } else {
            getView().showNoInternetMessage();
            setProgressIndicator(false);
        }
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
    public void onLoginFbButtonClicked(Activity activity, CallbackManager callbackManager) {
        //TODO: inject this so the presenter doesn't know about Backendless
        Backendless.UserService.loginWithFacebookSdk(activity,
            callbackManager,
            new AsyncCallback<BackendlessUser>()
            {
                @Override
                public void handleResponse( BackendlessUser loggedInUser )
                {
                    // user logged in successfully
                    getView().closeLoginScreen();
                }

                @Override
                public void handleFault( BackendlessFault fault )
                {
                    // failed to log in
                    getView().showMessage(fault.getMessage());
                }
            },
            true);
    }
}
