package org.helpapaw.helpapaw.settings;

import android.content.SharedPreferences;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.utils.Injection;

public class SettingsPresenter extends Presenter<SettingsContract.View> implements SettingsContract.UserActionsListener {

    private ISettingsRepository settingsRepository;

    public SettingsPresenter(SettingsContract.View view, SharedPreferences preferences) {
        super(view);
        settingsRepository = Injection.getSettingsRepository(preferences);
    }

    @Override
    public void onRadiusChange(int radius) {
        settingsRepository.saveRadius(radius);
    }

    @Override
    public void onTimeoutChange(int timeout) {
        settingsRepository.saveTimeout(timeout);
    }
}
