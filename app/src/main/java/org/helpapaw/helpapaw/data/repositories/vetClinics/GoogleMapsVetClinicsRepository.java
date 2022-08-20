package org.helpapaw.helpapaw.data.repositories.vetClinics;

import android.os.Handler;
import android.os.Looper;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleMapsVetClinicsRepository implements VetClinicsRepository {

    List<VetClinic> aggregatedVetClinics = new ArrayList<>();

    @Override
    public void getVetClinics(double latitude, double longitude, int radius,
                              LoadVetClinicsCallback callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            String result = null;
            String error = null;
            try {
                UrlDownloader downloadUrl = new UrlDownloader();
                result = downloadUrl.readUrl(createVetClinicsRequest(latitude, longitude, radius));
            } catch (Exception e) {
                Injection.getCrashLogger().recordException(e);
                error = e.getLocalizedMessage();
            }

            String finalResult = result;
            String finalError = error;
            handler.post(() -> {
                //UI Thread work here
                if (finalError != null) {
                    callback.onVetClinicsFailure(finalError);
                    return;
                }

                try {
                    List<VetClinic> newVetClinics;
                    GoogleMapsVetClinicsParser vetClinicsParser = new GoogleMapsVetClinicsParser();
                    newVetClinics = vetClinicsParser.parseClinicsList(finalResult);
                    if (newVetClinics != null) {
                        aggregatedVetClinics.addAll(newVetClinics);
                        callback.onVetClinicsLoaded(aggregatedVetClinics);
                    } else {
                        Injection.getCrashLogger().recordException(new Throwable("Empty clinics list from: " + finalResult));
                        callback.onVetClinicsFailure(PawApplication.getContext().getResources().getString(R.string.txt_unknown_error_occurred));
                    }
                } catch (Exception e) {
                    Injection.getCrashLogger().recordException(e);
                    if (callback != null) {
                        callback.onVetClinicsFailure(e.getLocalizedMessage());
                    }
                }
            });
        });
    }

    public String createVetClinicsRequest(double lat, double lon, int radius) {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + lat + "," + lon +
                "&radius=" + radius +
                "&types=" + "veterinary_care" +
                "&sensor=true" +
                "&key=" + PawApplication.getContext().getResources().getString(R.string.google_android_map_api_key);
    }

    @Override
    public void getVetClinicDetails(VetClinic vetClinic, LoadVetClinicDetailsCallback callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            String result = null;
            String error = null;
            try {
                UrlDownloader downloadUrl = new UrlDownloader();
                result = downloadUrl.readUrl(createVetClinicDetailsRequest(vetClinic.getId()));
            } catch (Exception e) {
                Injection.getCrashLogger().recordException(e);
                error = e.getLocalizedMessage();
            }

            String finalResult = result;
            String finalError = error;
            handler.post(() -> {
                //UI Thread work here
                if (finalError != null) {
                    callback.onVetClinicDetailsFailure(finalError);
                    return;
                }

                try {
                    VetClinic vetClinicDetails;
                    GoogleMapsVetClinicsParser vetClinicsParser = new GoogleMapsVetClinicsParser();
                    vetClinicDetails = vetClinicsParser.parseClinicDetails(finalResult);
                    if (vetClinicDetails != null) {
                        callback.onVetClinicDetailsLoaded(vetClinicDetails);
                    } else {
                        Injection.getCrashLogger().recordException(new Throwable("Empty clinic details from: " + finalResult));
                        callback.onVetClinicDetailsFailure(PawApplication.getContext().getResources().getString(R.string.txt_unknown_error_occurred));
                    }
                } catch (Exception e) {
                    Injection.getCrashLogger().recordException(e);
                    if (callback != null) {
                        callback.onVetClinicDetailsFailure(e.getLocalizedMessage());
                    }
                }
            });
        });
    }

    private String createVetClinicDetailsRequest(String id) {
        return "https://maps.googleapis.com/maps/api/place/details/json?" +
                "place_id=" + id +
                "&key=" + PawApplication.getContext().getResources().getString(R.string.google_android_map_api_key);
    }
}