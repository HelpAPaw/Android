package org.helpapaw.helpapaw.userprofile;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;


/**
 * Created by niya
 */
public class UserProfilePresenter extends Presenter<UserProfileContract.View>
        implements UserProfileContract.UserActionsListener {

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
    public void onEditUserProfileClicked() {
        getView().editUserProfile();
    }

    @Override
    public void onDeleteUserProfileClicked() {
        getView().deleteUserProfile();
    }

    @Override
    public void onSaveEditUserClicked() {
        getView().saveEditUserProfile();
    }

    @Override
    public void onUpdateUser(final String userName, final String userPhone) {
        FirebaseCrashlytics.getInstance().log("Initiate change for user ");

        userManager.update(userName, userPhone, new UserManager.UpdateUserCallback() {
            @Override
            public void onUpdateUserSuccess() {
                getView().showUserProfile(userManager.getCurrentUser());
            }

            @Override
            public void onUpdateUserFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }
}
