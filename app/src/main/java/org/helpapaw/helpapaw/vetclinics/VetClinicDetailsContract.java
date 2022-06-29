package org.helpapaw.helpapaw.vetclinics;

import org.helpapaw.helpapaw.data.models.VetClinic;

import java.util.HashMap;


public interface VetClinicDetailsContract {

    interface View {

        void showVetClinicDetails(VetClinic vetClinic);

        void openNavigation(double latitude, double longitude);

        void openNumberDialer(String phoneNumber);

        void showNoInternetMessage();
    }

    interface UserActionsListener {

        void onInitDetailsScreen(VetClinic vetClinic);

        void onNavigateButtonClicked();

        void onCallButtonClicked();
    }
}
