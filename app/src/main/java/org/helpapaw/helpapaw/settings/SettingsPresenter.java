package org.helpapaw.helpapaw.settings;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.utils.Injection;

public class SettingsPresenter extends Presenter<SettingsContract.View> implements SettingsContract.UserActionsListener {

    private ISettingsRepository settingsRepository;
    private int radius;
    private int timeout;
    private int signalTypes;

    SettingsPresenter(SettingsContract.View view) {
        super(view);
        settingsRepository = Injection.getSettingsRepositoryInstance();
    }

    @Override
    public void initialize() {
        radius = settingsRepository.getRadius();
        timeout = settingsRepository.getTimeout();
        signalTypes = settingsRepository.getSignalTypes();

        getView().setRadius(radius);
        getView().setTimeout(timeout);
        getView().setSignalTypes(signalTypes);

        settingsRepository.clearLocationData();
    }

    @Override
    public void onRadiusChange(int radius) {
        this.radius = radius;
        settingsRepository.saveRadius(radius);
    }

    @Override
    public void onTimeoutChange(int timeout) {
        this.timeout = timeout;
        settingsRepository.saveTimeout(timeout);
    }

    @Override
    public void onSignalTypesChange(int signalTypes) {
        this.signalTypes = signalTypes;
        settingsRepository.saveSignalTypes(signalTypes);
    }

    @Override
    public void onCloseSettingsScreen() {
        //No need to call it here because it is called when signalsMapFragment resumes and obtains location
        //Injection.getPushNotificationsRepositoryInstance().updateDeviceInfoInCloud(null, radius, timeout);
    }

    public int getSignalTypes() {
        return signalTypes;
    }
}
