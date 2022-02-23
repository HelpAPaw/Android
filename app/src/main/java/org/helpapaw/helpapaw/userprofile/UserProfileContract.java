package org.helpapaw.helpapaw.userprofile;

import com.backendless.BackendlessUser;

/**
 * Created by niya
 */
public interface UserProfileContract {

    interface View {

        boolean isActive();

        void showMessage(String message);

        void showUserProfile(BackendlessUser currentUser);

        void editUserProfile();

        void saveEditUserProfile();

        void deleteUserProfile();

        void onUserProfileDeleted();
    }

    interface UserActionsListener {

        void onInitUserProfileScreen();

        void onUpdateUser(String userName, String userPhone);

        void onDeleteUserProfile();

        void onEditUserProfileClicked();

        void onDeleteUserProfileClicked();

        void onSaveEditUserClicked();

    }
}
