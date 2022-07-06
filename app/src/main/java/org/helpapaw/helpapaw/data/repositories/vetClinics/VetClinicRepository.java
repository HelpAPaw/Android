package org.helpapaw.helpapaw.data.repositories.vetClinics;

import org.helpapaw.helpapaw.data.models.VetClinic;

import java.util.List;

public interface VetClinicRepository {

    void getVetClinics(double latitude, double longitude, int radius, LoadVetClinicsCallback callback);

    void getVetClinicDetails(VetClinic vetClinic, LoadVetClinicDetailsCallback callback);

    interface LoadVetClinicsCallback {

        void onVetClinicsLoaded(List<VetClinic> vetClinics);

        void onVetClinicsFailure(String message);
    }

    interface LoadVetClinicDetailsCallback {

        void onVetClinicDetailsLoaded(VetClinic vetClinicDetails);

        void onVetClinicDetailsFailure(String message);
    }
}
