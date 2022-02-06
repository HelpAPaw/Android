package org.helpapaw.helpapaw.settings;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.utils.Injection;

public class SettingsPresenter extends Presenter<SettingsContract.View> implements SettingsContract.UserActionsListener {

    private double SCALE_COEFFICIENT_B;
    private double SCALE_COEFFICIENT_A;

    private final ISettingsRepository settingsRepository;
    private int radius;
    private int timeout;
    private int signalTypes;
    private int language;

    SettingsPresenter(SettingsContract.View view) {
        super(view);
        settingsRepository = Injection.getSettingsRepositoryInstance();
    }

    @Override
    public void initialize() {
        radius = settingsRepository.getRadius();
        timeout = settingsRepository.getTimeout();
        signalTypes = settingsRepository.getSignalTypes();

        int radiusMin = getContext().getResources().getInteger(R.integer.radius_value_min);
        int radiusMax = getContext().getResources().getInteger(R.integer.radius_value_max);
        SCALE_COEFFICIENT_B = Math.log((float) radiusMax / radiusMin)/(radiusMax - radiusMin);
        SCALE_COEFFICIENT_A = radiusMax /Math.exp(SCALE_COEFFICIENT_B* radiusMax);

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
    public void onLanguageChange(int languageIndex) {
        this.language = languageIndex;
        settingsRepository.saveLanguage(languageIndex);
    }

    @Override
    public void onCloseSettingsScreen() {
        //No need to call it here because it is called when signalsMapFragment resumes and obtains location
        //Injection.getPushNotificationsRepositoryInstance().updateDeviceInfoInCloud(null, radius, timeout);
    }

    public int getSignalTypes() {
        return signalTypes;
    }

    int scaleLogarithmic(final int unscaled) {
        return (int) (SCALE_COEFFICIENT_A * Math.exp(SCALE_COEFFICIENT_B*unscaled));
    }

    int unscaleLogarithmic(int scaled) {
        return (int) ((Math.log(scaled/SCALE_COEFFICIENT_A))/SCALE_COEFFICIENT_B);
    }

}
