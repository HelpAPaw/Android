package org.helpapaw.helpapaw.vetclinics;

import android.os.AsyncTask;

import java.util.HashMap;
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

        List<HashMap<String, String>> nearbyPlacesList;
        VetClinicsParser vetClinicsParser = new VetClinicsParser();
        nearbyPlacesList =  vetClinicsParser.parse(result);
        delegate.vetClinicsLoaded(nearbyPlacesList);
    }
}
