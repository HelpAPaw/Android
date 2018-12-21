package org.helpapaw.helpapaw.data.repositories;

public interface ISettingsRepository {
    void saveRadius(int radius);

    void saveTimeout(int timeout);

    int getRadius();

    int getTimeout();

    double getLastShownLatitude();

    void setLastShownLatitude(double latitude);

    double getLastShownLongitude();

    void setLastShownLongitude(double longitude);

    float getLastShownZoom();

    void setLastShownZoom(float zoom);

    void clearLocationData();
}
