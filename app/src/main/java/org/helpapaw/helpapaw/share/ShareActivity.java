package org.helpapaw.helpapaw.share;

import static org.helpapaw.helpapaw.base.PawApplication.APP_OPENINGS_TO_ASK_FOR_SHARE;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityShareBinding;
import org.helpapaw.helpapaw.utils.SharingUtils;

/**
 * Created by Niya on 15/09/2021.
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

        binding.btnShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharingUtils.resetCounter(ShareActivity.this, APP_OPENINGS_TO_ASK_FOR_SHARE + 1);
                SharingUtils.shareApp(ShareActivity.this);
            }
        });
    }
}
