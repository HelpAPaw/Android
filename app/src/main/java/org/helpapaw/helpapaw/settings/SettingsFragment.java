package org.helpapaw.helpapaw.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.databinding.FragmentSettingsBinding;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.Locale;

import static android.app.Activity.RESULT_OK;

import static org.helpapaw.helpapaw.data.repositories.BackendlessPushNotificationsRepository.MAX_PADDING;
import static org.helpapaw.helpapaw.data.repositories.BackendlessPushNotificationsRepository.SIGNAL_TYPES_SIZE;
import static org.helpapaw.helpapaw.settings.SignalTypeSettingsActivity.EXTRA_SELECTED_TYPES;
import static org.helpapaw.helpapaw.settings.SignalTypeSettingsActivity.REQUEST_CHANGE_SIGNAL_TYPES;

public class SettingsFragment extends BaseFragment implements SettingsContract.View {

    private static final int RADIUS_VALUE_MIN = 1;
    private static final int RADIUS_VALUE_MAX = 1000;
    private static final int TIMEOUT_VALUE_MIN = 1;

    private static double SCALE_COEFFICIENT_B = Math.log(RADIUS_VALUE_MAX/RADIUS_VALUE_MIN)/(RADIUS_VALUE_MAX-RADIUS_VALUE_MIN);
    private static double SCALE_COEFFICIENT_A = RADIUS_VALUE_MAX/Math.exp(SCALE_COEFFICIENT_B*RADIUS_VALUE_MAX);

    private int currentlySelectedTypesInt = Integer.MAX_VALUE;
    private String[] signalTypeStrings;

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
        binding.radiusValue.setMax(RADIUS_VALUE_MAX);
        
        signalTypeStrings = getResources().getStringArray(R.array.signal_types_items);

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
        binding.signalTypeSetting.setOnClickListener(onSelectedSignalTypesClickListener());
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
                    seekBar.setProgress(progress);
                    updateRadius(scaleLogarithmic(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actionsListener.onRadiusChange(scaleLogarithmic(seekBar.getProgress()));
            }
        };
    }

    private int scaleLogarithmic(final int unscaled) {
        int scaled = (int) (SCALE_COEFFICIENT_A * Math.exp(SCALE_COEFFICIENT_B*unscaled));

        return scaled;
    }

    private int unscaleLogarithmic(int scaled) {
        int unscaled = (int) ((Math.log(scaled/SCALE_COEFFICIENT_A))/SCALE_COEFFICIENT_B);

        return unscaled;
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

    public View.OnClickListener onSelectedSignalTypesClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SignalTypeSettingsActivity.class);
                intent.putExtra(EXTRA_SELECTED_TYPES,
                        Utils.convertIntegerToBooleanArray(currentlySelectedTypesInt, signalTypeStrings.length));

                startActivityForResult(intent, REQUEST_CHANGE_SIGNAL_TYPES);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHANGE_SIGNAL_TYPES) {
            if (resultCode == RESULT_OK) {
                boolean[] selectedTypesValue = data.getBooleanArrayExtra(EXTRA_SELECTED_TYPES);

                if (selectedTypesValue != null ) {
                    int selectedTypesInt = Utils.convertBooleanArrayToInt(selectedTypesValue);
                    int selectedTypesIntPadding = ((MAX_PADDING << SIGNAL_TYPES_SIZE) & MAX_PADDING) | selectedTypesInt;
                    setSignalTypes(selectedTypesIntPadding);

                    actionsListener.onSignalTypesChange(currentlySelectedTypesInt);
                }
            }
        }
    }

    @Override
    public void setRadius(int radius) {
        binding.radiusValue.setProgress(unscaleLogarithmic(radius));
        updateRadius(radius);
    }

    @Override
    public void setTimeout(int timeout) {
        binding.timeoutValue.setProgress(timeout);
        updateTimeout(timeout);
    }

    @Override
    public void setSignalTypes(int signalTypesInt) {
        currentlySelectedTypesInt = signalTypesInt;
        String signalTypesStr =
                Utils.selectedTypesToString(Utils.convertIntegerToBooleanArray(signalTypesInt, signalTypeStrings.length), signalTypeStrings);

        binding.signalTypeSetting.setText(signalTypesStr);
    }

    private void updateRadius(int value) {
        if (value == RADIUS_VALUE_MIN) {
            String result = String.format(Locale.getDefault(), getString(R.string.radius_output_single), value);
            binding.radiusOutput.setText(result);
        } else {
            String result = String.format(Locale.getDefault(), getString(R.string.radius_output), value);
            binding.radiusOutput.setText(result);
        }
    }

    private void updateTimeout(int value) {
        if (value == TIMEOUT_VALUE_MIN) {
            String result = String.format(Locale.getDefault(), getString(R.string.timeout_output_single), value);
            binding.timeoutOutput.setText(result);
        } else {
            String result = String.format(Locale.getDefault(), getString(R.string.timeout_output), value);
            binding.timeoutOutput.setText(result);
        }
    }
}
