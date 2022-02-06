package org.helpapaw.helpapaw.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityLanguageSettingsBinding;
import org.helpapaw.helpapaw.filtersignal.SignalTypeCustomAdapter;


public class LanguageSettingsActivity extends AppCompatActivity {

    protected static final int REQUEST_CHANGE_LANGUAGE = 2;
    public static final String EXTRA_SELECTED_LANGUAGE = "selected_language";

    private boolean[] languageSelection;
    private SignalTypeCustomAdapter customAdapter;

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
            binding.toolbarTitle.setText(getString(R.string.string_signal_type_settings_title));
        }

        Intent intent = this.getIntent();

        ListView languageListView = findViewById(R.id.language_list_view);
        String[] languageStrings = getResources().getStringArray(R.array.languages_items);
        if (languageSelection == null) {
            languageSelection = intent.getBooleanArrayExtra(EXTRA_SELECTED_LANGUAGE);
        }
        customAdapter = new SignalTypeCustomAdapter(languageListView.getContext(), languageStrings, languageSelection);
        languageListView.setAdapter(customAdapter);
    }

    @Override
    public void onBackPressed() {
        saveSelectedTypes();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        saveSelectedTypes();
        finish();
        return true;
    }

    private void saveSelectedTypes() {
        languageSelection = customAdapter.getCurrentSignalTypeSelection();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_LANGUAGE, languageSelection);
        setResult(RESULT_OK, resultIntent);
    }
}
