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
        String[] languageNames = getResources().getStringArray(R.array.language_names);
        String[] languageCodes = getResources().getStringArray(R.array.language_codes);

        String selectedLanguageCode = intent.getStringExtra(EXTRA_SELECTED_LANGUAGE);

        customAdapter = new LanguageCustomAdapter(
                languageListView.getContext(),
                languageNames,
                languageCodes,
                selectedLanguageCode
        );
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
        String languageSelectionCode = customAdapter.getCurrentLanguageSelectionCode();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_LANGUAGE, languageSelectionCode);
        setResult(RESULT_OK, resultIntent);
    }
}
