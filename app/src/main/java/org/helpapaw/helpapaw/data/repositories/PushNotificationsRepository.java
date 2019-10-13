package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface PushNotificationsRepository {
    void registerDeviceToken();
    void unregisterDeviceToken();
    void updateDeviceInfoInCloud(final Location location, final Integer radius, final Integer timeout);
    void pushNewSignalNotification(final Signal signal, final double latitude, final double longitude);
    void pushNewCommentNotification(final Signal signal, final String newComment, final List<Comment> currentComments);
    void pushNewStatusNotification(final Signal signal, final int newStatus, final List<Comment> currentComments);
}
