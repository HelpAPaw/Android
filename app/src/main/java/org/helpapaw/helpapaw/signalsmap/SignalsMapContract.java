package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.models.VetClinic;

import java.io.File;
import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
interface SignalsMapContract {

    interface View {

        void showMessage(String message);

        void displaySignals(List<Signal> signals, boolean showPopup, boolean[] selectedTypes);

        void displaySignals(List<Signal> signals, boolean showPopup, String focusedSignalId,  boolean[] selectedTypes);

        void updateMapCameraPosition(double latitude, double longitude, Float zoom);

        void setAddSignalViewVisibility(boolean visibility);

        void setFilterSignalViewVisibility(boolean visibility);

        void setActiveFilterTextVisibility(boolean visibility);

        void hideKeyboard();

        void openLoginScreen();

        void showRegistrationRequiredAlert();

        void setThumbnailImage(File photoFile);

        void setAuthorPhone(String phoneNumber);

        void clearSignalViewData();

        void setSignalViewProgressVisibility(boolean visibility);

        void openSignalDetailsScreen(Signal signal);

        void openVetClinicDetailsScreen(VetClinic vetClinic);

        void closeSignalsMapScreen();

        void showVetClinicsOnMap(List<VetClinic> vetClinics);

        void hideVetClinicsFromMap();

        void showDescriptionErrorMessage();

        void showAddedSignalMessage();

        void showNoInternetMessage();

        void setSignalsMenuButtonRefreshingStatus(boolean isRefreshing);

        void setClinicsMenuButtonRefreshingStatus(boolean isRefreshing);

        void setClinicsMenuButtonToShow();

        boolean isActive();

        void onLogoutSuccess();
        void onLogoutFailure(String message);
    }

    interface UserActionsListener {

        void onInitSignalsMap(String focusedSignalId);

        void onLocationChanged(double latitude, double longitude, int radius, int timeout);

        void onAddSignalClicked(boolean visibility);

        void onCancelAddSignal();

        void onSendSignalClicked(String description, String authorPhone, int type);

        void onFilterSignalsClicked(boolean[] selectedTypes);

        void onChoosePhotoIconClicked();

        void onSignalInfoWindowClicked(Signal signal);

        void onVetClinicInfoWindowClicked(VetClinic vetClinic);

        void onShowVetClinicsClicked(double latitude, double longitude, int radius);

        void showVetClinics(double latitude, double longitude, int radius);

        boolean shouldShowVetClinics();

        void onBackButtonPressed();

        void onRefreshButtonClicked();

        void onFilterSignalsButtonClicked();

        void onSignalUpdated(Signal signal);

        void onAuthenticationAction();

        void onLoginAction();
    }
}
