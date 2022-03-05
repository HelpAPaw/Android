package org.helpapaw.helpapaw.userprofile;

import android.view.View;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;


/**
 * Created by niya
 */
public class UserProfilePresenter extends Presenter<UserProfileContract.View>
        implements UserProfileContract.UserActionsListener {

    private static final int MIN_PASS_LENGTH = 6;

    private final UserManager userManager;

    public UserProfilePresenter(UserProfileContract.View view) {
        super(view);

        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onInitUserProfileScreen() {
        getView().showUserProfile(userManager.getCurrentUser());
    }

    @Override
    public void onDeleteUserProfileClicked() {
        getView().deleteUserProfile();
    }

    @Override
    public void onUpdateUser(
            final String userName, final String userPhone,
            final String password, final String passwordConfirm) {
        FirebaseCrashlytics.getInstance().log("Initiate change for user ");

        boolean passwordsDoesNotMatch = !password.equals(passwordConfirm);

        if (passwordsDoesNotMatch) {
            getView().showPasswordDoesNotMatchMessage();
        } else if (!password.isEmpty() && password.length() < MIN_PASS_LENGTH) {
            getView().showPasswordErrorMessage();
            return;
        } else {
            getView().setProgressVisibility(View.VISIBLE);

            userManager.update(userName, userPhone, password, new UserManager.UpdateUserCallback() {
                @Override
                public void onUpdateUserSuccess() {
                    getView().showUserProfile(userManager.getCurrentUser());
                    getView().setProgressVisibility(View.GONE);
                }

                @Override
                public void onUpdateUserFailure(String message) {
                    if (!isViewAvailable()) return;
                    getView().setProgressVisibility(View.GONE);
                    getView().showMessage(message);
                }
            });
        }
    }

    @Override
    public void onDeleteUserProfile() {
        getView().setProgressVisibility(View.VISIBLE);

        userManager.delete(userManager.getLoggedUserId(), new UserManager.DisableUserCallback() {
            @Override
            public void onDisableUserSuccess() {
                onLogOut();
            }

            @Override
            public void onDisableUserFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
                getView().setProgressVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onLogOut() {
        userManager.logout(new UserManager.LogoutCallback() {
            @Override
            public void onLogoutSuccess() {
                getView().onFinishActivity();
            }

            @Override
            public void onLogoutFailure(String message) {
                getView().showMessage(message);
            }
        });
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }
}
