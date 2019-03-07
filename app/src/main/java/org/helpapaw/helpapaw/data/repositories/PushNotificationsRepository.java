package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

public interface PushNotificationsRepository {
    void registerDeviceForToken();
    void saveNewDeviceLocation(Location location);
}
