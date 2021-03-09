package org.helpapaw.helpapaw.settings;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
    }

    private void initialize() {
        if (mSettingsFragment == null) {
            mSettingsFragment = SettingsFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.grp_content_frame, mSettingsFragment);
            transaction.commit();
        }
    }
}
