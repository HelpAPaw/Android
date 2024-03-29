package org.helpapaw.helpapaw.vetclinics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.data.repositories.vetClinics.VetClinicsRepository;
import org.helpapaw.helpapaw.utils.Injection;


public class VetClinicDetailsPresenter extends Presenter<VetClinicDetailsContract.View>
        implements VetClinicDetailsContract.UserActionsListener {

    private final VetClinicsRepository vetClinicRepository;

    private final VetClinic vetClinic = new VetClinic();

    public VetClinicDetailsPresenter(VetClinicDetailsContract.View view) {
        super(view);

        vetClinicRepository = Injection.getVetClinicRepositoryInstance();
    }

    @Override
    public void onInitDetailsScreen(VetClinic selectedVetClinic) {
        if (selectedVetClinic != null) {
            vetClinicRepository.getVetClinicDetails(selectedVetClinic,
                    new VetClinicsRepository.LoadVetClinicDetailsCallback() {
                @Override
                public void onVetClinicDetailsLoaded(VetClinic vetClinicDetails) {
                    populateVetClinicInfo(vetClinicDetails);
                    getView().showVetClinicDetails(vetClinic);
                }

                @Override
                public void onVetClinicDetailsFailure(String message) {
                    getView().showErrorMessage(message);
                }
            });
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

    private void populateVetClinicInfo(VetClinic vetClinicDetails) {
        vetClinic.setId(vetClinicDetails.getId());
        vetClinic.setName(vetClinicDetails.getName());
        vetClinic.setLatitude(vetClinicDetails.getLatitude());
        vetClinic.setLongitude(vetClinicDetails.getLongitude());
        vetClinic.setAddress(vetClinicDetails.getAddress());
        vetClinic.setPhoneNumber(vetClinicDetails.getPhoneNumber());
        vetClinic.setUrl(vetClinicDetails.getUrl());
    }
}
