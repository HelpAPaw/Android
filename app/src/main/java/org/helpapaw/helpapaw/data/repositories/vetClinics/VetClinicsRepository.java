package org.helpapaw.helpapaw.data.repositories.vetClinics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.VetClinic;

import java.util.ArrayList;
import java.util.List;

public class VetClinicsRepository implements VetClinicRepository {

    List<VetClinic> vetClinics = new ArrayList<>();

    @Override
    public void getVetClinics(double latitude, double longitude, int radius,
                              LoadVetClinicsCallback callback) {
        VetClinicsTask vetClinicsTask = new VetClinicsTask();
        vetClinicsTask.delegate = result -> {
            for (VetClinic current : result) {
                vetClinics.add(current);
            }

            callback.onVetClinicsLoaded(vetClinics);
        };

        String vetClinicsRequest =
                new StringBuilder(createVetClinicsRequest(latitude, longitude, radius)).toString();

        vetClinicsTask.execute(vetClinicsRequest);
    }

    @Override
    public void getVetClinicDetails(VetClinic vetClinic, LoadVetClinicDetailsCallback callback) {


        VetClinicDetailsTask vetClinicDetailsTask = new VetClinicDetailsTask();
        vetClinicDetailsTask.delegate = result -> {
            callback.onVetClinicDetailsLoaded(result);
        };

        String vetClinicDetailsRequest =
                new StringBuilder(createVetClinicDetailsRequest(vetClinic.getId())).toString();
        vetClinicDetailsTask.execute(vetClinicDetailsRequest);
    }

    private StringBuilder createVetClinicDetailsRequest(String id) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("place_id=" + id);
        sb.append("&key=" + PawApplication.getContext().getResources().getString(R.string.google_android_map_api_key_test)); // TODO - we need to change this

        return sb;
    }

    public StringBuilder createVetClinicsRequest(double lat, double lon, int radius) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + lat + "," + lon);
        sb.append("&radius=" + radius);
        sb.append("&types=" + "veterinary_care");
        sb.append("&sensor=true");
        sb.append("&key=" + PawApplication.getContext().getResources().getString(R.string.google_android_map_api_key_test)); // TODO - we need to change this

        return sb;
    }
}