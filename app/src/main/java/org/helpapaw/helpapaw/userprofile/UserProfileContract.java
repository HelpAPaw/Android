package org.helpapaw.helpapaw.userprofile;

import com.backendless.BackendlessUser;

/**
 * Created by niya
 */
public interface UserProfileContract {

    interface View {

        boolean isActive();

        void showMessage(String message);

        void showPasswordDoesNotMatchMessage();

        void showPasswordErrorMessage();

        void setProgressVisibility(int visibility);

        void showUserProfile(BackendlessUser currentUser);

        void deleteUserProfile();

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
