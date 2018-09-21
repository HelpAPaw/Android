package org.helpapaw.helpapaw.authentication;

import android.content.Context;
import android.os.AsyncTask;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class PrivacyPolicyConfirmationGetter extends AsyncTask<Void, Void, String> {

    private WeakReference<PrivacyPolicyConfirmationContract.Obtain> weakAsker;
    private WeakReference<Context>                                  weakContext;

    public PrivacyPolicyConfirmationGetter(PrivacyPolicyConfirmationContract.Obtain asker, Context context) {
        weakAsker = new WeakReference<>(asker);
        weakContext = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(Void... params) {

        String str = null;
        try {
            Context context = weakContext.get();
            if (context != null) {
                str = Utils.getHtml(context.getString(R.string.url_privacy_policy));
            }
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
