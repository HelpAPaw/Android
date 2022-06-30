package org.helpapaw.helpapaw.vetclinics;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.List;

public class VetClinicDetailsTask extends AsyncTask<String, Integer, String> {

    private String googlePlaceData;

    public VetClinicDetailsAsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... url) {
        try {
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlaceData = downloadUrl.readUrl(url[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String result) {
        HashMap<String, String> placeDetails;
        DataParser dataParser = new DataParser();
        placeDetails =  dataParser.parseDetails(result);
        delegate.vetClinicDetailsLoaded(placeDetails);
    }
}
