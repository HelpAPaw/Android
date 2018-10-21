package org.helpapaw.helpapaw.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseActivity;

import static org.helpapaw.helpapaw.base.PawApplication.TEST_VERSION;

public class SettingsActivity extends BaseActivity {

    private SettingsFragment mSettingsFragment;

    @Override
    protected void onStart() {
        super.onStart();
        initFragment();
    }

    private void initFragment() {
        if (mSettingsFragment == null) {
            mSettingsFragment = SettingsFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.grp_content_frame, mSettingsFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getToolbarTitle() {
        String title = getString(R.string.text_settings);

        if (TEST_VERSION) {
            title += " (TEST VERSION)";
        }

        return title;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }
}
