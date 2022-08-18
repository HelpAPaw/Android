package org.helpapaw.helpapaw.data.repositories.vetClinics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.VetClinic;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapsVetClinicsRepository implements VetClinicsRepository {

    List<VetClinic> vetClinics = new ArrayList<>();

    @Override
    public void getVetClinics(double latitude, double longitude, int radius,
                              LoadVetClinicsCallback callback) {

        VetClinicsTask vetClinicsTask = new VetClinicsTask();
        vetClinicsTask.delegate = new VetClinicsAsyncResponse() {
            @Override
            public void onVetClinicsSuccess(List<VetClinic> result) {
                if (result != null) {
                    vetClinics.addAll(result);
                    callback.onVetClinicsLoaded(vetClinics);
                } else {
                    callback.onVetClinicsFailure(""); //TODO: replace with unknown error
                }
            }

            @Override
            public void onVetClinicsFailure(String error) {
                callback.onVetClinicsFailure(error);
            }
        };
        vetClinicsTask.execute(createVetClinicsRequest(latitude, longitude, radius));
    }

    public String createVetClinicsRequest(double lat, double lon, int radius) {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + lat + "," + lon +
                "&radius=" + radius +
                "&types=" + "veterinary_care" +
                "&sensor=true" +
                "&key=" + PawApplication.getContext().getResources().getString(R.string.google_android_map_api_key); //TODO: change api key
    }

    @Override
    public void getVetClinicDetails(VetClinic vetClinic, LoadVetClinicDetailsCallback callback) {

        VetClinicDetailsTask vetClinicDetailsTask = new VetClinicDetailsTask();
        vetClinicDetailsTask.delegate = result -> {
            //TODO: test null case
            if (result != null) {
                callback.onVetClinicDetailsLoaded(result);
            } else {
                callback.onVetClinicDetailsFailure(""); //TODO: extract error
            }
        };
        vetClinicDetailsTask.execute(createVetClinicDetailsRequest(vetClinic.getId()));
    }

    private String createVetClinicDetailsRequest(String id) {
        return "https://maps.googleapis.com/maps/api/place/details/json?" +
                "place_id=" + id +
                "&key=" + PawApplication.getContext().getResources().getString(R.string.google_android_map_api_key); //TODO: change api key
    }
}