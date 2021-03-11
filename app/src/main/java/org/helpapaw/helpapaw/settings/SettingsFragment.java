package org.helpapaw.helpapaw.settings;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.databinding.FragmentSettingsBinding;

import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends BaseFragment implements SettingsContract.View {

    private static final int RADIUS_VALUE_MIN = 1;
    private static final int TIMEOUT_VALUE_MIN = 1;
    private static final int REQUEST_CHANGE_SIGNAL_TYPES = 1;

    int selectedTypesForDb = Integer.MAX_VALUE;

    FragmentSettingsBinding binding;
    SettingsPresenter settingsPresenter;
    SettingsContract.UserActionsListener actionsListener;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    protected Presenter getPresenter() {
        return settingsPresenter;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        settingsPresenter = new SettingsPresenter(this);
        settingsPresenter.setView(this);
        actionsListener = settingsPresenter;

        actionsListener.initialize();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(binding.toolbar);
            ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(true);
                supportActionBar.setDisplayShowTitleEnabled(false);
                binding.toolbarTitle.setText(getString(R.string.text_settings));
            }
        }

        binding.radiusValue.setOnSeekBarChangeListener(onRadiusSeekBarChangeListener());
        binding.timeoutValue.setOnSeekBarChangeListener(onTimeoutSeekBarChangeListener());
        binding.signalTypeSetting.setOnClickListener(onSelectedSignalTypeChangeListener());

        Intent intent = getActivity().getIntent();
        onActivityResult(REQUEST_CHANGE_SIGNAL_TYPES, RESULT_OK, intent);
    }

    @Override
    public void onDestroyView() {
        actionsListener.onCloseSettingsScreen();

        super.onDestroyView();
    }

    public SeekBar.OnSeekBarChangeListener onRadiusSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < RADIUS_VALUE_MIN) {
                    seekBar.setProgress(RADIUS_VALUE_MIN);
                }
                else {
                    updateRadius(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actionsListener.onRadiusChange(seekBar.getProgress());
            }
        };
    }

    public SeekBar.OnSeekBarChangeListener onTimeoutSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < TIMEOUT_VALUE_MIN) {
                    seekBar.setProgress(TIMEOUT_VALUE_MIN);
                }
                else {
                    updateTimeout(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actionsListener.onTimeoutChange(seekBar.getProgress());
            }
        };
    }

    public View.OnClickListener onSelectedSignalTypeChangeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SignalTypeSettingsActivity.class);
                intent.putExtra("selected_types", convertIntegerToBooleanArray(selectedTypesForDb));

                startActivityForResult(intent, REQUEST_CHANGE_SIGNAL_TYPES);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHANGE_SIGNAL_TYPES) {
            if (resultCode == RESULT_OK) {
                boolean[] selectedTypesValue = data.getBooleanArrayExtra("selected_types");
                if (selectedTypesValue == null ) {
                    selectedTypesForDb = settingsPresenter.getSignalTypes();
                    boolean[] selectedTypesDbValue = convertIntegerToBooleanArray(selectedTypesForDb);
                    binding.signalTypeSetting.setText(selectedTypesToString(selectedTypesDbValue));
                } else {
                    String signalTypesStr = selectedTypesToString(selectedTypesValue);
                    binding.signalTypeSetting.setText(signalTypesStr);

                    selectedTypesForDb = convertBooleanArrayToInt(selectedTypesValue);
                }
                actionsListener.onSignalTypesChange(selectedTypesForDb);
            }
        }
    }

    @Override
    public void setRadius(int radius) {
        binding.radiusValue.setProgress(radius);
        updateRadius(radius);
    }

    @Override
    public void setTimeout(int timeout) {
        binding.timeoutValue.setProgress(timeout);
        updateTimeout(timeout);
    }

    @Override
    public void setSignalTypes(int signalTypes) {
        String signalTypesStr = selectedTypesToString(convertIntegerToBooleanArray(signalTypes));
        binding.signalTypeSetting.setText(signalTypesStr);
    }

    private void updateRadius(int value) {
        if (value == 1) {
            String result = String.format(Locale.getDefault(), getString(R.string.radius_output_single), value);
            binding.radiusOutput.setText(result);
        } else {
            String result = String.format(Locale.getDefault(), getString(R.string.radius_output), value);
            binding.radiusOutput.setText(result);
        }
    }

    private void updateTimeout(int value) {
        if (value == 1) {
            String result = String.format(Locale.getDefault(), getString(R.string.timeout_output_single), value);
            binding.timeoutOutput.setText(result);
        } else {
            String result = String.format(Locale.getDefault(), getString(R.string.timeout_output), value);
            binding.timeoutOutput.setText(result);
        }
    }

    private int convertBooleanArrayToInt(boolean[] arr) {
        int n = 0;
        for (boolean b : arr) {
            n = (n << 1) | (b ? 1 : 0);
        }
        return n;
    }

    private boolean[] convertIntegerToBooleanArray(int n) {
        String[] signalTypes = getResources().getStringArray(R.array.signal_types_items);
        boolean[] booleanArr = new boolean[signalTypes.length];

        for (int i = 0; i < signalTypes.length; i++) {
            if ((n & (1 << i)) > 0) {
                booleanArr[signalTypes.length - 1 - i] = true;
            } else {
                booleanArr[signalTypes.length - 1 - i] = false;
            }
        }

        return booleanArr;
    }

    private String selectedTypesToString(boolean[] selectedSignalTypes) {
        String selectedTypesToString = "";
        String[] signalTypes = getResources().getStringArray(R.array.signal_types_items);

        if (allSelected(selectedSignalTypes)) {
            selectedTypesToString = "All signal types";
        } else if (noneSelected(selectedSignalTypes)) {
            selectedTypesToString = "None";
        } else {
            for (int i = 0; i < selectedSignalTypes.length; i++) {
                if (selectedSignalTypes[i]) {
                    selectedTypesToString = selectedTypesToString + signalTypes[i] + ", ";
                }
            }
            selectedTypesToString = selectedTypesToString.substring(0, selectedTypesToString.length() - 2);
        }

        return selectedTypesToString;
    }

    private boolean allSelected(boolean[] selectedSignalTypes) {
        for (boolean selectedSignalType : selectedSignalTypes) {
            if (!selectedSignalType) {
                return false;
            }
        }
        return true;
    }

    private boolean noneSelected(boolean[] selectedSignalTypes) {
        for (boolean selectedSignalType : selectedSignalTypes) {
            if (selectedSignalType) {
                return false;
            }
        }
        return true;
    }
}
