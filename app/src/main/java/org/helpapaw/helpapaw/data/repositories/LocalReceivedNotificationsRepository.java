package org.helpapaw.helpapaw.data.repositories;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;

import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.db.NotificationsDatabase;

import java.util.List;

public class LocalReceivedNotificationsRepository implements ReceivedNotificationsRepository{

    private NotificationsDatabase notificationsDatabase;

    public LocalReceivedNotificationsRepository() {
        notificationsDatabase = NotificationsDatabase.getDatabase(getContext());
    }

    @Override
    public List<Notification> getAll() {
        return notificationsDatabase.notificationDao().getAll();
    }

    @Override
    public void deleteAll() {
        notificationsDatabase.notificationDao().deleteAll();
    }

    @Override
    public void saveNotification(Notification notification) {
        notificationsDatabase.notificationDao().saveNotification(notification);
    }
}
