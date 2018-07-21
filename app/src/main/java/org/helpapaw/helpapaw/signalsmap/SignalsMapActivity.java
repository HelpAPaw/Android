package org.helpapaw.helpapaw.signalsmap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
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

import java.util.List;

import static org.helpapaw.helpapaw.base.PawApplication.TEST_VERSION;

public class SignalsMapActivity extends BaseActivity {

    private SharedPreferences mSharedPreferences;
    private final static String ACCEPTED_TERMS_CONDITIONS = "ACCEPTED_TERMS_CONDITIONS";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getApplicationContext().getSharedPreferences(this.getClass().getSimpleName(), MODE_PRIVATE);

        if (mSharedPreferences.getBoolean(ACCEPTED_TERMS_CONDITIONS, false) || !userManager.isLoggedIn()) {
            if (null == savedInstanceState) {
                if (getIntent().hasExtra(Signal.KEY_FOCUSED_SIGNAL_ID)) {
                    initFragment(SignalsMapFragment.newInstance(getIntent().getStringExtra(Signal.KEY_FOCUSED_SIGNAL_ID)));
                } else {
                    initFragment(SignalsMapFragment.newInstance());
                }
            }
            scheduleBackgroundChecks();
        } else {

            final TextView message = new TextView(getApplicationContext());
            message.setPadding(50,1,1,1);
            final SpannableString s =
                    new SpannableString(getString(R.string.privacy_policy_url));
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.privacy_policy_dialog_title)
                    .setView(message)
            .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mSharedPreferences.edit().putBoolean(ACCEPTED_TERMS_CONDITIONS, true).apply();
                    if (null == savedInstanceState) {
                        if (getIntent().hasExtra(Signal.KEY_FOCUSED_SIGNAL_ID)) {
                            initFragment(SignalsMapFragment.newInstance(getIntent().getStringExtra(Signal.KEY_FOCUSED_SIGNAL_ID)));
                        } else {
                            initFragment(SignalsMapFragment.newInstance());
                        }
                    }
                    scheduleBackgroundChecks();
                }
            })
            .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, final int i) {
                    mSharedPreferences.edit().putBoolean(ACCEPTED_TERMS_CONDITIONS, false).commit();
                    SignalsMapActivity.this.logOut();
                }
            })
                    .setCancelable(false)
            .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected String getToolbarTitle() {
        String title = getString(R.string.app_name);

        if (TEST_VERSION) {
            title += " (TEST VERSION)";
        }

        return title;
    }

    private void initFragment(Fragment signalsMapFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.grp_content_frame, signalsMapFragment);
        transaction.commit();
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
}
