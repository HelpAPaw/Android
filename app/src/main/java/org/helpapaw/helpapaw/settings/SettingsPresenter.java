package org.helpapaw.helpapaw.settings;

import android.content.SharedPreferences;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.utils.Injection;

public class SettingsPresenter extends Presenter<SettingsContract.View> implements SettingsContract.UserActionsListener {

    private ISettingsRepository settingsRepository;
    private int radius;
    private int timeout;

    SettingsPresenter(SettingsContract.View view, SharedPreferences preferences) {
        super(view);
        settingsRepository = Injection.getSettingsRepository(preferences);
    }

    @Override
    public void initialize() {
        radius = settingsRepository.getRadius();
        timeout = settingsRepository.getTimeout();

        getView().setRadius(radius);
        getView().setTimeout(timeout);
    }

    @Override
    public void onRadiusChange(int radius) {
        this.radius = radius;
    }

    @Override
    public void onTimeoutChange(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void onCloseSettingsScreen() {
        settingsRepository.saveRadius(radius);
        settingsRepository.saveTimeout(timeout);
    }
}
