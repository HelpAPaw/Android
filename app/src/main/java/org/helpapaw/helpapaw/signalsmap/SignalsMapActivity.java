package org.helpapaw.helpapaw.signalsmap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseActivity;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

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

        if (!restoringActivity) {
            initFragment();
        }

        setupEnvironmentSwitching();
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
            replaceFragment();
        }
        else {
            Uri intentData = intent.getData();
            if (intentData != null) {
                String targetUrlString = intentData.getQueryParameter("target_url");
                if (targetUrlString != null) {
                    Uri targetUrl = Uri.parse(targetUrlString);
                    if (targetUrl != null) {
                        String linkUrlString = targetUrl.getQueryParameter("link");
                        if (linkUrlString != null) {
                            Uri linkUrl = Uri.parse(linkUrlString);
                            //TODO: DRY
                            if (linkUrl != null) {
                                String signalId = linkUrl.getQueryParameter("signal");
                                if (signalId != null) {
                                    mSignalsMapFragment = SignalsMapFragment.newInstance(signalId);
                                    replaceFragment();
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        if (pendingDynamicLinkData != null) {
                            Uri deepLink = pendingDynamicLinkData.getLink();
                            if (deepLink != null) {
                                String signalId = deepLink.getQueryParameter("signal");
                                if (signalId != null) {
                                    mSignalsMapFragment = SignalsMapFragment.newInstance(signalId);
                                    replaceFragment();
                                }
                            }
                        }

                        if (mSignalsMapFragment == null) {
                            mSignalsMapFragment = SignalsMapFragment.newInstance();
                            replaceFragment();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(SignalsMapActivity.this.getClass().getSimpleName(), "getDynamicLink:onFailure", e);
                        mSignalsMapFragment = SignalsMapFragment.newInstance();
                        replaceFragment();
                    }
                });
        }
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
