package org.helpapaw.helpapaw.settings;

public interface SettingsContract {
    interface View {
        void setRadius(int radius);

        void setTimeout(int timeout);
    }

    interface UserActionsListener {
        void initialize();

        void onRadiusChange(int radius);

        void onTimeoutChange(int timeout);
    }
}
