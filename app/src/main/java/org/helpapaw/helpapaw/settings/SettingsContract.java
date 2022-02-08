package org.helpapaw.helpapaw.settings;

import android.app.Activity;

public interface SettingsContract {
    interface View {
        void setRadius(int radius);

        void setTimeout(int timeout);

        void setSignalTypes(int signalTypes);

        void setLanguage(int languageIndex);
    }

    interface UserActionsListener {
        void initialize();

        void onRadiusChange(int radius);

        void onTimeoutChange(int timeout);

        void onSignalTypesChange(int signalTypes);

        void onLanguageChange(Activity activity, int languageIndex);

        void onCloseSettingsScreen();
    }
}
