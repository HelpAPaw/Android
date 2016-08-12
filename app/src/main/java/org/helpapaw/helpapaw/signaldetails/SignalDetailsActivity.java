package org.helpapaw.helpapaw.signaldetails;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.ActivitySignalDetailsBinding;

import java.util.List;

public class SignalDetailsActivity extends AppCompatActivity {

    public final static String SIGNAL_KEY = "signalKey";
    ActivitySignalDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signal_details);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.txt_signal_details_title));
        }

        if (null == savedInstanceState) {

            if (getIntent() != null) {
                Signal signal = getIntent().getParcelableExtra(SIGNAL_KEY);
                SignalDetailsFragment fragment = SignalDetailsFragment.newInstance(signal);
                initFragment(fragment);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    private void initFragment(Fragment signalsDetailsFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.grp_content_frame, signalsDetailsFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof SignalDetailsFragment) {
                    ((SignalDetailsFragment) fragment).onBackPressed();
                }
            }
        }
    }

}
