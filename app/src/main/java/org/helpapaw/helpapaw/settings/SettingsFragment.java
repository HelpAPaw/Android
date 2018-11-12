package org.helpapaw.helpapaw.settings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.databinding.FragmentSettingsBinding;

import java.util.Locale;

public class SettingsFragment extends BaseFragment implements SettingsContract.View {

    private static final int RADIUS_VALUE_MIN = 1;
    private static final int TIMEOUT_VALUE_MIN = 1;

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

        settingsPresenter = new SettingsPresenter(this, getActivity().getPreferences(Context.MODE_PRIVATE));
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
    }

    public SeekBar.OnSeekBarChangeListener onRadiusSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < RADIUS_VALUE_MIN) {
                    seekBar.setProgress(RADIUS_VALUE_MIN);
                }
                else {
                    actionsListener.onRadiusChange(progress);
                    updateRadius(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
                    actionsListener.onTimeoutChange(progress);
                    updateTimeout(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
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
}
