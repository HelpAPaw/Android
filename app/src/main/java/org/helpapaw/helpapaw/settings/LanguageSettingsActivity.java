package org.helpapaw.helpapaw.settings;

import static org.helpapaw.helpapaw.settings.SettingsFragment.EXTRA_SELECTED_LANGUAGE;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityLanguageSettingsBinding;


public class LanguageSettingsActivity extends AppCompatActivity {

    private int languageSelection = -1;
    private LanguageCustomAdapter customAdapter;

    ActivityLanguageSettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_language_settings);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.txt_language_settings));
        }

        Intent intent = this.getIntent();

        ListView languageListView = findViewById(R.id.language_list_view);
        String[] languageStrings = getResources().getStringArray(R.array.languages_items);
        if (languageSelection == -1) {
            languageSelection = intent.getIntExtra(EXTRA_SELECTED_LANGUAGE, 0);
        }
        customAdapter = new LanguageCustomAdapter(languageListView.getContext(), languageStrings, languageSelection);
        languageListView.setAdapter(customAdapter);
    }

    @Override
    public void onBackPressed() {
        saveSelectedLanguage();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        saveSelectedLanguage();
        finish();
        return true;
    }

    private void saveSelectedLanguage() {
        languageSelection = customAdapter.getCurrentLanguageSelection();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_LANGUAGE, languageSelection);
        setResult(RESULT_OK, resultIntent);
    }
}
