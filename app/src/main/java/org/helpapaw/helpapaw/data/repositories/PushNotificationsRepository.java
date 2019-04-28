package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

public interface PushNotificationsRepository {
    void registerDeviceForToken();
    void saveNewDeviceLocation(Location location);
    void pushNewSignalNotification(final String tickerText, final String message, final String signalId, final double latitude, final double longitude);
}
