package org.helpapaw.helpapaw.signalsmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseActivity;

public class SignalsMapActivity extends BaseActivity {

    SignalsMapFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == savedInstanceState) {
             fragment = SignalsMapFragment.newInstance();
            initFragment(fragment);
        }
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_name);
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

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else {
            fragment.onBackPressed();
        }
    }
}
