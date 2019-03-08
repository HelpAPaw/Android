package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

public interface PushNotificationsRepository {
    void registerDeviceForToken();
    void saveNewDeviceLocation(Location location);
    void pushNotification(String tickerText, String contentTitle,
                          String contentText, String message, double latitude, double longitude);
}
