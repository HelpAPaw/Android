package org.helpapaw.helpapaw.data.repositories.vetClinics;

import android.os.AsyncTask;

import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.utils.Injection;

//TODO: update similarly to VetClinicsTask
public class VetClinicDetailsTask extends AsyncTask<String, Integer, String> {

    private String googlePlaceData;

    public VetClinicDetailsAsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... url) {
        try {
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlaceData = downloadUrl.readUrl(url[0]);
        } catch (Exception e) {
            Injection.getCrashLogger().recordException(e);
        }
        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String result) {
        VetClinic vetClinicDetails;
        VetClinicsParser vetClinicsParser = new VetClinicsParser();
        vetClinicDetails = vetClinicsParser.parseDetails(result);
        delegate.vetClinicDetailsLoaded(vetClinicDetails);
    }
}
