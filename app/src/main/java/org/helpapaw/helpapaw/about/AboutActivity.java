package org.helpapaw.helpapaw.about;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import org.helpapaw.helpapaw.BuildConfig;
import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityAboutBinding;
import org.helpapaw.helpapaw.utils.SharingUtils;

/**
 * Created by Alex on 10/29/2017.
 */

public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.string_about_title));
        }

        binding.tvAboutVersion.setText(BuildConfig.VERSION_NAME);

        binding.btnAboutContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharingUtils.contactSupport(AboutActivity.this);
            }
        });
    }
}
