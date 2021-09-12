package org.helpapaw.helpapaw.share;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.BuildConfig;
import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityAboutBinding;
import org.helpapaw.helpapaw.databinding.ActivityShareBinding;
import org.helpapaw.helpapaw.utils.SharingUtils;

/**
 * Created by Alex on 10/29/2017.
 */

public class ShareActivity extends AppCompatActivity {
    ActivityShareBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_share);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.text_share));
        }

        binding.btnShareContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharingUtils.shareSupport(ShareActivity.this);
            }
        });
    }
}
