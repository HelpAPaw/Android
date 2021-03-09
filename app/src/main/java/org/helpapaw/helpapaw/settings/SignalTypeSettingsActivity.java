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

    private static final int REQUEST_CHANGE_SIGNAL_TYPES = 1;

    private boolean[] signalTypeSelection;
    private String[] signalTypes;
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
        signalTypes = getResources().getStringArray(R.array.signal_types_items);
        if (signalTypeSelection == null) {
            signalTypeSelection = intent.getBooleanArrayExtra("selected_types");
        }
        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalTypes, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);
    }

    @Override
    public void onBackPressed() {
        saveSelectedTypes();
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHANGE_SIGNAL_TYPES) {
            if (resultCode == RESULT_OK) {
                boolean[] selectedTypesValue = data.getBooleanArrayExtra("selected_types");
                if(selectedTypesValue == null ) {
                    signalTypeSelection = new boolean[signalTypes.length];
                    for (int i = 0; i < signalTypeSelection.length; i++) {
                        signalTypeSelection[i] = true;
                    }
                } else {
                    signalTypeSelection = selectedTypesValue;
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        saveSelectedTypes();
        return true;
    }

    private void saveSelectedTypes() {
        customAdapter.setSignalTypeSelectionToCurrent();
        signalTypeSelection = customAdapter.getSignalTypeSelection();

        Intent resultIntent = new Intent(this, SettingsActivity.class);
        resultIntent.putExtra("selected_types", signalTypeSelection);
        setResult(RESULT_OK, resultIntent);

        startActivityForResult(resultIntent, 90);
        finish();
    }
}
