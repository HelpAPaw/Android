package org.helpapaw.helpapaw.data.repositories.vetClinics;

import android.os.AsyncTask;

import org.helpapaw.helpapaw.data.models.VetClinic;

import java.util.List;

public class VetClinicsTask extends AsyncTask<String, Integer, String> {

    private String googlePlacesData;

    public VetClinicsAsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... url) {
        try {
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        List<VetClinic> vetClinicsList;
        VetClinicsParser vetClinicsParser = new VetClinicsParser();
        vetClinicsList =  vetClinicsParser.parse(result);
        delegate.vetClinicsLoaded(vetClinicsList);
    }
}
