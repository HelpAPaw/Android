package org.helpapaw.helpapaw.settings;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;

import org.helpapaw.helpapaw.databinding.ActivitySignalTypeSettingsBinding;
import org.helpapaw.helpapaw.filtersignal.SignalTypeCustomAdapter;


public class SignalTypeSettingsActivity extends AppCompatActivity {

    protected static final int REQUEST_CHANGE_SIGNAL_TYPES = 1;
    public static final String EXTRA_SELECTED_TYPES = "selected_types";

    private boolean[] signalTypeSelection;
    private SignalTypeCustomAdapter customAdapter;

    ActivitySignalTypeSettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signal_type_settings);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.string_signal_type_settings_title));
        }

        Intent intent = this.getIntent();

        ListView signalTypeListView = findViewById(R.id.signal_type_list_view);
        String[] signalTypeStrings = getResources().getStringArray(R.array.signal_types_items);
        if (signalTypeSelection == null) {
            signalTypeSelection = intent.getBooleanArrayExtra(EXTRA_SELECTED_TYPES);
        }
        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalTypeStrings, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);
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
        signalTypeSelection = customAdapter.getCurrentSignalTypeSelection();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_TYPES, signalTypeSelection);
        setResult(RESULT_OK, resultIntent);
    }
}
