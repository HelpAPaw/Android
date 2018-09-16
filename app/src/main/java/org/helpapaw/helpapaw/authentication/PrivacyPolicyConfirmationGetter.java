package org.helpapaw.helpapaw.authentication;

import android.os.AsyncTask;

import org.helpapaw.helpapaw.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class PrivacyPolicyConfirmationGetter extends AsyncTask<Void, Void, String> {

    private WeakReference<PrivacyPolicyConfirmationContract.Obtain> weakAsker;

    public PrivacyPolicyConfirmationGetter(PrivacyPolicyConfirmationContract.Obtain asker) {
        weakAsker = new WeakReference<>(asker);
    }

    @Override
    protected String doInBackground(Void... params) {

        String str = null;
        try {
            str = Utils.getHtml("https://develop.backendless.com/***REMOVED***/console/fcfdrgddsebccdkjfamuhppaasnowqluooks/files/view/web/privacypolicy.htm");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    @Override
    protected void onPostExecute(String result) {
        if (weakAsker.get() != null) {
            weakAsker.get().onPrivacyPolicyObtained(result);
        }
    }
}
