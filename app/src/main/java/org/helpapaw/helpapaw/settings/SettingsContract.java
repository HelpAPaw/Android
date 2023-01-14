package org.helpapaw.helpapaw.settings;

public interface SettingsContract {
    interface View {
        void setRadius(int radius);

        void setTimeout(int timeout);

        void setSignalTypes(int signalTypes);

        void setLanguage(String languageCode);
    }

    interface UserActionsListener {
        void initialize();

        void onRadiusChange(int radius);

        void onTimeoutChange(int timeout);

        void onSignalTypesChange(int signalTypes);

        void onLanguageChange(String languageCode);

        void onCloseSettingsScreen();
    }
}
