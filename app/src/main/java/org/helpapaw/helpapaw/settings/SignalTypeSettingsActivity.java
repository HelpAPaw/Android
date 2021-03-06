package org.helpapaw.helpapaw.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;

import org.helpapaw.helpapaw.databinding.ActivitySignalTypeSettingsBinding;
import org.helpapaw.helpapaw.filtersignal.SignalTypeCustomAdapter;


public class SignalTypeSettingsActivity extends AppCompatActivity {

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

        ListView signalTypeListView = findViewById(R.id.signal_type_list_view);
        String[] signalTypes = getResources().getStringArray(R.array.signal_types_items);
        signalTypeSelection = new boolean[signalTypes.length]; // TODO this should be taken from the DB
        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalTypes, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);
    }

    @Override
    public void onBackPressed() {
        saveSelectedTypes();
//        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        saveSelectedTypes();
//        NavUtils.navigateUpFromSameTask(this);
        return true;
    }

    private void saveSelectedTypes() {
        customAdapter.setSignalTypeSelectionToCurrent();
        signalTypeSelection = customAdapter.getSignalTypeSelection();

        Intent resultIntent = new Intent(this, SettingsActivity.class);
        resultIntent.putExtra("selected_types", signalTypeSelection);
        setResult(RESULT_OK, resultIntent);
        startActivity(resultIntent);
        finish();
//        NavUtils.navigateUpFromSameTask(this);
    }
}
