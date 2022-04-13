package org.helpapaw.helpapaw.userprofile;

import org.helpapaw.helpapaw.data.user.DisplayUser;

/**
 * Created by niya
 */
public interface UserProfileContract {

    interface View {

        boolean isActive();

        void showMessage(String message);

        void showNoInternetMessage();

        void showPasswordDoesNotMatchMessage();

        void showPasswordErrorMessage();

        void setProgressVisibility(int visibility);

        void showUserProfile(DisplayUser currentUser);

        void showDeleteUserProfileConfirmation();

        void onFinishActivity();
    }

    interface UserActionsListener {

        void onInitUserProfileScreen();

        void onUpdateUser(String userName, String userPhone, String password, String passwordConfirm);

        void onDeleteUserProfile();

        void onLogOut();

        void onDeleteUserProfileClicked();
    }
}
