package org.helpapaw.helpapaw.data.repositories;

public interface ISettingsRepository {
    void saveRadius(int radius);

    void saveTimeout(int timeout);

    int getRadius();

    int getTimeout();

    double getLatitude();

    void setLatitude(double latitude);

    double getLongitude();

    void setLongitude(double longitude);

    float getZoom();

    void setZoom(float zoom);
}
