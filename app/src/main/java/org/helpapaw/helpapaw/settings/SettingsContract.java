package org.helpapaw.helpapaw.settings;

public interface SettingsContract {
    interface View {}

    interface UserActionsListener {
        void onRadiusChange(int radius);

        void onTimeoutChange(int timeout);
    }
}
