package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

public interface PushNotificationsRepository {
    void registerDeviceToken();
    void unregisterDeviceToken();
    void updateDeviceInfoInCloud(final Location location, final Integer radius, final Integer timeout);
    void pushNewSignalNotification(final String tickerText, final String message, final String signalId, final double latitude, final double longitude);
}
