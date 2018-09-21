package org.helpapaw.helpapaw.authentication.register;

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
public class RegisterPresenter extends Presenter<RegisterContract.View>
        implements RegisterContract.UserActionsListener,
        PrivacyPolicyConfirmationContract.Obtain,
        PrivacyPolicyConfirmationContract.UserResponse {

    private static final int MIN_PASS_LENGTH = 6;

    private UserManager userManager;

    private boolean showProgressBar;

    private String email;
    private String password;
    private String name;
    private String phoneNumber;

    public RegisterPresenter(RegisterContract.View view) {
        super(view);
        showProgressBar = false;
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onInitRegisterScreen() {
        setProgressIndicator(showProgressBar);
    }

    @Override
    public void onRegisterButtonClicked(String email, String password, String passwordConfirmation, String name, String phoneNumber) {
        getView().clearErrorMessages();

        if (isEmpty(email) || !Utils.getInstance().isEmailValid(email)) {
            getView().showEmailErrorMessage();
            return;
        }

        if (isEmpty(password) || password.length() < MIN_PASS_LENGTH) {
            getView().showPasswordErrorMessage();
            return;
        }

        if (!password.equals(passwordConfirmation)) {
            getView().showPasswordConfirmationErrorMessage();
            return;
        }

        if (isEmpty(name)) {
            getView().showNameErrorMessage();
            return;
        }

        getView().hideKeyboard();
        setProgressIndicator(true);

        // Save values for later;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;

        PrivacyPolicyConfirmationGetter privacyPolicyConfirmationGetter = new PrivacyPolicyConfirmationGetter(this, PawApplication.getContext());
        privacyPolicyConfirmationGetter.execute();
    }

    private void attemptToRegister(String email, String password, String name, String phoneNumber) {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.register(email, password, name, phoneNumber, new UserManager.RegistrationCallback() {
                @Override
                public void onRegistrationSuccess() {
                    if (!isViewAvailable()) return;
                    getView().showRegistrationSuccessfulMessage();
                    getView().closeRegistrationScreen();
                }

                @Override
                public void onRegistrationFailure(String message) {
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

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }

    @Override
    public void onLoginButtonClicked() {
        getView().closeRegistrationScreen();
    }

    @Override
    public void onWhyPhoneButtonClicked() {
        getView().showWhyPhoneDialog();
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private boolean isEmpty(String value) {
        return !(value != null && value.length() > 0);
    }

    @Override
    public void onPrivacyPolicyObtained(String privacyPolicy) {
        if (!isViewAvailable()) return;

        if (privacyPolicy != null) {
            getView().showPrivacyPolicyDialog(privacyPolicy);
        }
        else {
            setProgressIndicator(false);
            getView().showErrorMessage(PawApplication.getContext().getString(R.string.txt_error_getting_privacy_policy));
        }
    }

    @Override
    public void onUserAcceptedPrivacyPolicy() {
        attemptToRegister(email, password, name, phoneNumber);
    }

    @Override
    public void onUserDeclinedPrivacyPolicy() {
        email = null;
        password = null;
        name = null;
        phoneNumber = null;

        setProgressIndicator(false);
    }
}
