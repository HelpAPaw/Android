package org.helpapaw.helpapaw.signalsmap;

import android.net.Uri;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
interface SignalsMapContract {

    interface View {

        void showMessage(String message);

        void displaySignals(List<Signal> signals, boolean showPopup);

        void displaySignals(List<Signal> signals, boolean showPopup, String focusedSignalId);

        void updateMapCameraPosition(double latitude, double longitude, Float zoom);

        void setAddSignalViewVisibility(boolean visibility);

        void hideKeyboard();

        void showSendPhotoBottomSheet();

        void openCamera();

        void openGallery();

        void saveImageFromURI(Uri photoUri);

        void openLoginScreen();

        void setThumbnailImage(String photoUri);

        void clearSignalViewData();

        void setSignalViewProgressVisibility(boolean visibility);

        void openSignalDetailsScreen(Signal signal);

        void closeSignalsMapScreen();

        void showDescriptionErrorMessage();

        void showAddedSignalMessage();

        void showNoInternetMessage();

        void setProgressVisibility(boolean visibility);

        boolean isActive();

        void onLogoutSuccess();
        void onLogoutFailure(String message);

    }

    interface UserActionsListener {

        void onInitSignalsMap(String focusedSignalId);

        void onLocationChanged(double latitude, double longitude, int radius, int timeout);

        void onAddSignalClicked(boolean visibility);

        void onCancelAddSignal();

        void onSendSignalClicked(String description, String authorPhone);

        void onChoosePhotoIconClicked();

        void onCameraOptionSelected();

        void onGalleryOptionSelected();

        void onSignalPhotoSelected(String photoUri);

        void onStoragePermissionForCameraGranted();

        void onStoragePermissionForGalleryGranted();

        void onSignalInfoWindowClicked(Signal signal);

        void onBackButtonPressed();

        void onRefreshButtonClicked();

        void onSignalStatusUpdated(Signal signal);

        void onAuthenticationAction();

        void onLoginAction();


    }
}
