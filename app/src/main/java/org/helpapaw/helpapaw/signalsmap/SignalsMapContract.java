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

        void toggleAddSignalView();

        void hideKeyboard();

        void showSendPhotoBottomSheet();

        void openCamera();

        void openGallery();

        void openLoginScreen();
    }

    interface UserActionsListener {

        void onLocationChanged(double latitude, double longitude);

        void onAddSignalClicked();

        void onSendSignalClicked(String description);

        void onSignalPhotoClicked();

        void onCameraOptionSelected();

        void onGalleryOptionSelected();

        void onSignalPhotoSelected(String photoUri);
    }
}
