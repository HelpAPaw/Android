package org.helpapaw.helpapaw.userprofile;

import com.backendless.BackendlessUser;

/**
 * Created by niya
 */
public interface UserProfileContract {

    interface View {

        void showMessage(String message);

//        void hideKeyboard();
//
        void showUserProfile(BackendlessUser currentUser);
//
//        void setEditUserProfileButtonVisibility(int visibility);
//
//        void showNoInternetMessage();
//
        boolean isActive();

        void editUserProfile();

        void saveEditUserProfile();

        void deleteUserProfile();
    }

    interface UserActionsListener {

        void onInitUserProfileScreen();

        void onUpdateUser(String userName, String userPhone);

//        void onDeleteSignal();
//
//        void onUserProfileClosing();

        void onEditUserProfileClicked();

        void onDeleteUserProfileClicked();

        void onSaveEditUserClicked();

    }
}
