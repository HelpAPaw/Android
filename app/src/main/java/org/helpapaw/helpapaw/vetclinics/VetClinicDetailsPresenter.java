package org.helpapaw.helpapaw.vetclinics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.VetClinic;


public class VetClinicDetailsPresenter extends Presenter<VetClinicDetailsContract.View>
        implements VetClinicDetailsContract.UserActionsListener {

    private VetClinic vetClinic;

    public VetClinicDetailsPresenter(VetClinicDetailsContract.View view) {
        super(view);
    }

    @Override
    public void onInitDetailsScreen(VetClinic vetClinic) {

        if (vetClinic != null) {
            this.vetClinic = vetClinic;
            getView().showVetClinicDetails(vetClinic);
        }
    }

    @Override
    public void onNavigateButtonClicked() {
        double latitude = vetClinic.getLatitude();
        double longitude = vetClinic.getLongitude();
        getView().openNavigation(latitude, longitude);
    }

    @Override
    public void onCallButtonClicked() {
        String phoneNumber = vetClinic.getPhoneNumber();
        getView().openNumberDialer(phoneNumber);
    }

    @Override
    public void onMoreInfoButtonClicked() {
        getView().openUrl(vetClinic.getUrl());
    }
}
