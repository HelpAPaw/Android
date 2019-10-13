package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface PushNotificationsRepository {
    enum SignalUpdate {
        NEW_STATUS,
        NEW_COMMENT
    }

    void registerDeviceToken();
    void unregisterDeviceToken();
    void updateDeviceInfoInCloud(final Location location, final Integer radius, final Integer timeout);
    void pushNewSignalNotification(final Signal signal, final double latitude, final double longitude);
    public void pushSignalUpdatedNotification(final Signal signal, final List<Comment> currentComments, final PushNotificationsRepository.SignalUpdate signalUpdate, final int newStatus, final String newComment);
}
