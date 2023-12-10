package org.helpapaw.helpapaw.data.repositories;

import java.util.Date;

public interface ISettingsRepository {
    void saveRadius(int radius);

    void saveTimeout(int timeout);

    void saveSignalTypes(int signalTypes);

    void saveLanguage(String languageCode);

    int getRadius();

    int getTimeout();

    int getSignalTypes();

    String getLanguageCode();

    double getLastShownLatitude();

    void setLastShownLatitude(double latitude);

    double getLastShownLongitude();

    void setLastShownLongitude(double longitude);

    float getLastShownZoom();

    void setLastShownZoom(float zoom);

    void clearLocationData();

    void saveTokenToPreferences(String deviceToken);

    void deleteTokenFromPreferences();

    String getTokenFromPreferences();

    Date getLastDeviceTokenRefreshDate();

    void setLastDeviceTokenRefreshDate(Date date);

    boolean getHasShownForegroundLocationRationale();
    void setHasShownForegroundLocationRationale(boolean newValue);
    boolean getHasShownBackgroundLocationRationale();
    void setHasShownBackgroundLocationRationale(boolean newValue);
    boolean getHasDeniedForegroundLocationRationale();
    void setHasDeniedForegroundLocationRationale(boolean newValue);
    boolean getHasDeniedBackgroundLocationRationale();
    void setHasDeniedBackgroundLocationRationale(boolean newValue);
    boolean getHasShownHibernationExemptionDialog();
    void setHasShownHibernationExemptionDialog(boolean newValue);
}
