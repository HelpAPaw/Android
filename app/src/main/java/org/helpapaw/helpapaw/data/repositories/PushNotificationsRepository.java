package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface PushNotificationsRepository {
    void registerDeviceTokenIfNeeded();
    void registerDeviceToken();
    void unregisterDeviceToken();
    void updateDeviceInfoInCloud(final Location location, final Integer radius, final Integer timeout, final Integer signalTypes);
    void pushNewSignalNotification(final Signal signal);
    void pushNewCommentNotification(final Signal signal, final String newComment, final List<Comment> currentComments);
    void pushNewStatusNotification(final Signal signal, final int newStatus, final List<Comment> currentComments);
}
