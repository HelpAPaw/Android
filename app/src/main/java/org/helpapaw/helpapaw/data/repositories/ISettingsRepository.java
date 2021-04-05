package org.helpapaw.helpapaw.data.repositories;

public interface ISettingsRepository {
    void saveRadius(int radius);

    void saveTimeout(int timeout);

    void saveSignalTypes(int signalTypes);

    int getRadius();

    int getTimeout();

    int getSignalTypes();

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
}
