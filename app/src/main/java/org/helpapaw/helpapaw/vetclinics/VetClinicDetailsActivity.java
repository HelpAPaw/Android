package org.helpapaw.helpapaw.vetclinics;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.databinding.ActivityVetClinicDetailsBinding;


public class VetClinicDetailsActivity extends AppCompatActivity {

    public final static String VET_CLINIC_KEY = "vetClinicKey";
    ActivityVetClinicDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vet_clinic_details);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.txt_vet_clinic_details_title));
        }

        if (null == savedInstanceState) {

            if (getIntent() != null) {
                VetClinic vetClinic = getIntent().getParcelableExtra(VET_CLINIC_KEY);
                //TODO: show error
                if (vetClinic == null) {
                    finish();
                }
                VetClinicDetailsFragment fragment = VetClinicDetailsFragment.newInstance(vetClinic);
                initFragment(fragment);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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

    private void initFragment(Fragment vetClinicDetailsFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.grp_content_frame, vetClinicDetailsFragment);
        transaction.commit();
    }
}
