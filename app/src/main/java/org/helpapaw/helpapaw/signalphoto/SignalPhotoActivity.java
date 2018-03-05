package org.helpapaw.helpapaw.signalphoto;

import android.app.ActionBar;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.ActivitySignalPhotoBinding;

import java.util.List;

/**
 * Created by milen on 05/03/18.
 * Display a signal photo on full screen
 */

public class SignalPhotoActivity extends AppCompatActivity {

    public final static String SIGNAL_KEY = "signalKey";
    ActivitySignalPhotoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signal_photo);

        if (null == savedInstanceState) {

            if (getIntent() != null) {
                Signal                signal   = getIntent().getParcelableExtra(SIGNAL_KEY);
                SignalPhotoFragment fragment = SignalPhotoFragment.newInstance(signal);
                initFragment(fragment);
            }
        }
        hideSystemBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    private void initFragment(Fragment signalPhotoFragment) {
        FragmentManager     fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction     = fragmentManager.beginTransaction();
        transaction.add(R.id.grp_content_frame, signalPhotoFragment);
        transaction.commit();
    }

    private void hideSystemBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        try {
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }
        catch (Exception ex) {
            Log.e(getClass().getSimpleName(), "Could not hide action bar!");
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof SignalPhotoFragment) {
                    ((SignalPhotoFragment) fragment).onBackPressed();
                }
            }
        }
    }
}
