package org.helpapaw.helpapaw.data.repositories;

import org.helpapaw.helpapaw.data.models.Notification;

import java.util.List;

public interface ReceivedNotificationsRepository {

    List<Notification> getAll();

    void deleteAll();

    void saveNotification(Notification notification);
}
