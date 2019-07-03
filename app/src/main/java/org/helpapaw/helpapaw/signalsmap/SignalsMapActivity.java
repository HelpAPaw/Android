package org.helpapaw.helpapaw.signalsmap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseActivity;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.services.BackgroundCheckJobService;

import java.util.List;

public class SignalsMapActivity extends BaseActivity {

    private SignalsMapFragment mSignalsMapFragment;
    private int numberOfTitleClicks = 0;
    private boolean restoringActivity = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            restoringActivity = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!restoringActivity) {
            initFragment();
        }
        scheduleBackgroundChecks();

        setupEnvironmentSwitching();

        if (userManager.isLoggedIn()) {
            userManager.getHasAcceptedPrivacyPolicy(new UserManager.GetUserPropertyCallback() {
                @Override
                public void onSuccess(Object hasAcceptedPrivacyPolicy) {
                    try {
                        Boolean accepted = (Boolean) hasAcceptedPrivacyPolicy;
                        if (!accepted) {
                            logOut();
                        }
                    }
                    catch (Exception ignored) {}
                }

                @Override
                public void onFailure(String message) {
                    // Do nothing
                }
            });
        }
    }

    private void setupEnvironmentSwitching() {
        binding.toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOfTitleClicks++;
                if (numberOfTitleClicks >= 7) {
                    switchEnvironment();
                    numberOfTitleClicks = 0;
                }
            }
        });
    }

    @Override
    protected String getToolbarTitle() {
        String title = getString(R.string.app_name);

        if (PawApplication.getIsTestEnvironment()) {
            title += " (TEST)";
        }

        return title;
    }

    private void initFragment() {
        if (mSignalsMapFragment == null) {
            if (getIntent().hasExtra(Signal.KEY_SIGNAL_ID)) {
                Intent intent = getIntent();
                mSignalsMapFragment = SignalsMapFragment.newInstance(intent.getStringExtra(Signal.KEY_SIGNAL_ID));
                intent.removeExtra(Signal.KEY_SIGNAL_ID);
            } else {
                mSignalsMapFragment = SignalsMapFragment.newInstance();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.grp_content_frame, mSignalsMapFragment);
            transaction.commit();
        }
    }

    private void reinitFragment() {
        mSignalsMapFragment = SignalsMapFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.grp_content_frame, mSignalsMapFragment);
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

    private void switchEnvironment() {
        PawApplication.setIsTestEnvironment(!PawApplication.getIsTestEnvironment());
        binding.toolbarTitle.setText(getToolbarTitle());
        reinitFragment();
        Toast.makeText(this, "Environment switched", Toast.LENGTH_LONG).show();
    }
}
