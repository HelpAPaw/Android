package org.helpapaw.helpapaw.signalsmap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.Toolbar;

import android.widget.Toast;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseActivity;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

import io.branch.referral.Branch;

public class SignalsMapActivity extends BaseActivity {

    private SignalsMapFragment mSignalsMapFragment;
    private int numberOfTitleClicks = 0;
    private boolean restoringActivity = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        reinitFragment();
    }

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

        setupBranchSDK();

        if (!restoringActivity) {
            initFragment();
        }

        setupEnvironmentSwitching();
    }

    private void setupBranchSDK() {
        Branch.sessionBuilder(this).withCallback((referringParams, error) -> {
            if (error == null) {
                if (referringParams != null) {
                    String signalId = referringParams.optString("signalId");
                    if (!signalId.equals("")) {
                        mSignalsMapFragment.setFocusedSignalId(signalId);
                    }
                }
            } else {
                Injection.getCrashLogger().recordException(new Throwable(error.toString()));
            }
        }).withData(this.getIntent().getData()).init();
    }

    private void setupEnvironmentSwitching() {
        binding.toolbarTitle.setOnClickListener(v -> {
            numberOfTitleClicks++;
            if (numberOfTitleClicks >= 7) {
                switchEnvironment();
                numberOfTitleClicks = 0;
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
            commonInitFragment();
        }
    }

    private void reinitFragment() {
        commonInitFragment();
    }

    private void commonInitFragment() {
        Intent intent = getIntent();
        if (intent.hasExtra(Signal.KEY_SIGNAL_ID)) {
            mSignalsMapFragment = SignalsMapFragment.newInstance(intent.getStringExtra(Signal.KEY_SIGNAL_ID));
            intent.removeExtra(Signal.KEY_SIGNAL_ID);
        } else {
            mSignalsMapFragment = SignalsMapFragment.newInstance();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.grp_content_frame, mSignalsMapFragment);
        transaction.commit();
    }

    private void replaceFragment() {
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
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof SignalsMapFragment) {
                    ((SignalsMapFragment) fragment).onBackPressed();
                }
            }
        }
    }

    public Toolbar getToolbar() {
        return binding.toolbar;
    }

    private void switchEnvironment() {
        //Reregister device token with new notification channel
        Injection.getPushNotificationsRepositoryInstance().unregisterDeviceToken();
        PawApplication.setIsTestEnvironment(!PawApplication.getIsTestEnvironment());
        Injection.getPushNotificationsRepositoryInstance().registerDeviceToken();

        binding.toolbarTitle.setText(getToolbarTitle());
        reinitFragment();
        Toast.makeText(this, "Environment switched", Toast.LENGTH_LONG).show();
    }
}
