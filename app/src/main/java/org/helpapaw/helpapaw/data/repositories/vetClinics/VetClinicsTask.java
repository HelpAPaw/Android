package org.helpapaw.helpapaw.data.repositories.vetClinics;

import android.os.AsyncTask;

import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

public class VetClinicsTask extends AsyncTask<String, Integer, String> {

    public VetClinicsAsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... url) {
        String result;
        try {
            DownloadUrl downloadUrl = new DownloadUrl();
            result = downloadUrl.readUrl(url[0]);
        } catch (Exception e) {
            Injection.getCrashLogger().recordException(e);
            result = e.getLocalizedMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            List<VetClinic> vetClinicsList;
            VetClinicsParser vetClinicsParser = new VetClinicsParser();
            vetClinicsList = vetClinicsParser.parse(result);
            delegate.onVetClinicsSuccess(vetClinicsList);
        } catch (Exception e) {
            delegate.onVetClinicsFailure(e.getLocalizedMessage());
        }
    }
}
