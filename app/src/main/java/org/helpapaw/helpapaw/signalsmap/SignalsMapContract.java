package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public interface SignalsMapContract {

    interface View {

        void showMessage(String message);

        void displaySignals(List<Signal> signals);

        void updateMapCameraPosition(double latitude, double longitude, float zoom);

        void setAddSignalViewVisibility(boolean visibility);

        void hideKeyboard();

        void showSendPhotoBottomSheet();

        void openCamera();

        void openGallery();

        void openLoginScreen();

        void setThumbnailImage(String photoUri);

        void clearSignalViewData();

        void setSignalViewProgressVisibility(boolean visibility);
    }

    interface UserActionsListener {

        void onInitSignalsMap();

        void onLocationChanged(double latitude, double longitude);

        void onAddSignalClicked(boolean visibility);

        void onSendSignalClicked(String description);

        void onChoosePhotoIconClicked();

        void onCameraOptionSelected();

        void onGalleryOptionSelected();

        void onSignalPhotoSelected(String photoUri);

        void onStoragePermissionGranted();
    }
}
