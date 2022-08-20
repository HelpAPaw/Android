package org.helpapaw.helpapaw.data.repositories.vetClinics;

import android.os.AsyncTask;

import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.utils.Injection;

public class VetClinicDetailsTask extends AsyncTask<String, Integer, String> {

    public VetClinicDetailsAsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... url) {
        String result;
        try {
            DownloadUrl downloadUrl = new DownloadUrl();
            result = downloadUrl.readUrl(url[0]);
        } catch (Exception e) {
            //TODO: handle crash reporting in the repository
            Injection.getCrashLogger().recordException(e);
            result = e.getLocalizedMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            VetClinic vetClinicDetails;
            VetClinicsParser vetClinicsParser = new VetClinicsParser();
            vetClinicDetails = vetClinicsParser.parseDetails(result);
            delegate.onVetClinicDetailsSuccess(vetClinicDetails);
        } catch (Exception e) {
            delegate.onVetClinicDetailsFailure(e.getLocalizedMessage());
        }
    }
}
