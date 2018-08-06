package org.helpapaw.helpapaw.signalsmap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseActivity;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.services.BackgroundCheckJobService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static org.helpapaw.helpapaw.base.PawApplication.TEST_VERSION;

public class SignalsMapActivity extends BaseActivity {

    private SignalsMapFragment mSignalsMapFragment;
    private SharedPreferences mSharedPreferences;
    private final static String ACCEPTED_TERMS_CONDITIONS = "ACCEPTED_TERMS_CONDITIONS";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getApplicationContext().getSharedPreferences(this.getClass().getSimpleName(), MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSharedPreferences.getBoolean(ACCEPTED_TERMS_CONDITIONS, false) || !userManager.isLoggedIn()) {
            initFragment();
            scheduleBackgroundChecks();
        } else {
            final TextView message = new TextView(getApplicationContext());
          //  message.setPadding(50, 1, 1, 1);
            final SpannableString s =
                    new SpannableString("https://develop.backendless.com/***REMOVED***/console/fcfdrgddsebccdkjfamuhppaasnowqluooks/files/view/web/privacypolicy.htm");
            Linkify.addLinks(s, Linkify.WEB_URLS);
//            try {
//                message.setText(getHtml("https://develop.backendless.com/***REMOVED***/console/fcfdrgddsebccdkjfamuhppaasnowqluooks/files/view/web/privacypolicy.htm"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
           // message.setMovementMethod(LinkMovementMethod.getInstance());

           new RetrieveSiteData().execute();
        }
    }

    public static String getHtml(String url) throws IOException {
        // Build and set timeout values for the request.
        URLConnection connection = (new URL(url)).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        // Read and store the result line by line then return the entire string.
        InputStream in = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder html = new StringBuilder();
        for (String line; (line = reader.readLine()) != null; ) {
            html.append(line);
        }
        in.close();

        return html.toString();
    }

    @Override
    protected String getToolbarTitle() {
        String title = getString(R.string.app_name);

        if (TEST_VERSION) {
            title += " (TEST VERSION)";
        }

        return title;
    }

    private void initFragment() {
        if (mSignalsMapFragment == null) {
            if (getIntent().hasExtra(Signal.KEY_FOCUSED_SIGNAL_ID)) {
                mSignalsMapFragment = SignalsMapFragment.newInstance(getIntent().getStringExtra(Signal.KEY_FOCUSED_SIGNAL_ID));
            } else {
                mSignalsMapFragment = SignalsMapFragment.newInstance();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.grp_content_frame, mSignalsMapFragment);
            transaction.commit();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else {
            //noinspection RestrictedApi
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment instanceof SignalsMapFragment) {
                        ((SignalsMapFragment) fragment).onBackPressed();
                    }
                }
            }
        }
    }

    public Toolbar getToolbar() {
        return binding.toolbar;
    }

    private void scheduleBackgroundChecks() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        Job backgroundCheckJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(BackgroundCheckJobService.class)
                // uniquely identifies the job
                .setTag("BackgroundCheckJobService")
                .setRecurring(true)
                // start between 30 and 60 minutes from now
                .setTrigger(Trigger.executionWindow(15 * 60, 30 * 60))
                // overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();

        dispatcher.mustSchedule(backgroundCheckJob);
    }



    public class RetrieveSiteData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String str = null;
            try {
                str = getHtml("https://develop.backendless.com/***REMOVED***/console/fcfdrgddsebccdkjfamuhppaasnowqluooks/files/view/web/privacypolicy.htm");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return str;
        }

        @Override
        protected void onPostExecute(String result) {
            // this is the end of the css being displayed that needs to be removed to display the
            // Html properly
            String pattern = "none;}";
            //get the index of the pattern
            int end = result.lastIndexOf(pattern);
            // add the length to get the end of it
            end = end + pattern.length();
            // make a sub string without it
            result = result.substring(end, result.length());
            AlertDialog.Builder builder = new AlertDialog.Builder(SignalsMapActivity.this);
            builder.setTitle(R.string.privacy_policy_dialog_title)
                    .setMessage(Html.fromHtml(result))
                    .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mSharedPreferences.edit().putBoolean(ACCEPTED_TERMS_CONDITIONS, true).apply();
                            initFragment();
                            scheduleBackgroundChecks();
                        }
                    })
                    .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, final int i) {
                            mSharedPreferences.edit().putBoolean(ACCEPTED_TERMS_CONDITIONS, false).commit();
                            logOut();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}
