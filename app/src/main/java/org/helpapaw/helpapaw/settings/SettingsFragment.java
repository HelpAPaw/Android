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
import static org.helpapaw.helpapaw.settings.LanguageSettingsActivity.EXTRA_SELECTED_LANGUAGE;
import static org.helpapaw.helpapaw.settings.LanguageSettingsActivity.REQUEST_CHANGE_LANGUAGE;
import static org.helpapaw.helpapaw.settings.SignalTypeSettingsActivity.EXTRA_SELECTED_TYPES;
import static org.helpapaw.helpapaw.settings.SignalTypeSettingsActivity.REQUEST_CHANGE_SIGNAL_TYPES;


public class SettingsFragment extends BaseFragment implements SettingsContract.View {

    private int radiusMin;
    private int radiusMax;
    private int timeoutMin;
    private int currentlySelectedTypesInt = Integer.MAX_VALUE;
    private int currentlySelectedLanguage = 0;
    private String[] signalTypeStrings;
    private String[] languages;

    FragmentSettingsBinding binding;
    SettingsPresenter settingsPresenter;
    SettingsContract.UserActionsListener actionsListener;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        if (getContext() != null) {
            radiusMin = getContext().getResources().getInteger(R.integer.radius_value_min);
            radiusMax = getContext().getResources().getInteger(R.integer.radius_value_max);
            timeoutMin = getContext().getResources().getInteger(R.integer.timeout_value_min);
        }

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
        binding.radiusValue.setMax(radiusMax);
        
        signalTypeStrings = getResources().getStringArray(R.array.signal_types_items);
        languages = getResources().getStringArray(R.array.languages_items);

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
        binding.languageSetting.setOnClickListener(onChangeLanguageClickListener());
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
                if (progress < radiusMin) {
                    seekBar.setProgress(radiusMin);
                }
                else {
                    seekBar.setProgress(progress);
                    updateRadius(settingsPresenter.scaleLogarithmic(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actionsListener.onRadiusChange(settingsPresenter.scaleLogarithmic(seekBar.getProgress()));
            }
        };
    }

    public SeekBar.OnSeekBarChangeListener onTimeoutSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < timeoutMin) {
                    seekBar.setProgress(timeoutMin);
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

    public View.OnClickListener onChangeLanguageClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LanguageSettingsActivity.class);
                intent.putExtra(EXTRA_SELECTED_LANGUAGE,
                        Utils.convertIntegerToBooleanArray(0, languages.length));

                startActivityForResult(intent, REQUEST_CHANGE_LANGUAGE);
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
        binding.radiusValue.setProgress(settingsPresenter.unscaleLogarithmic(radius));
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

    @Override
    public void setLanguage(int languageIndex) {
        currentlySelectedLanguage = languageIndex;
        binding.languageSetting.setText(languages[languageIndex]);
    }

    private void updateRadius(int value) {
        String result;
        if (value == radiusMin) {
            result = String.format(Locale.getDefault(), getString(R.string.radius_output_single), value);
        } else {
            result = String.format(Locale.getDefault(), getString(R.string.radius_output), value);
        }
        binding.radiusOutput.setText(result);
    }

    private void updateTimeout(int value) {
        String result;
        if (value == timeoutMin) {
            result = String.format(Locale.getDefault(), getString(R.string.timeout_output_single), value);
        } else {
            result = String.format(Locale.getDefault(), getString(R.string.timeout_output), value);
        }
        binding.timeoutOutput.setText(result);
    }
}
