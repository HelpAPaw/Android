package org.helpapaw.helpapaw.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.helpapaw.helpapaw.data.models.Notification;

/**
 * Created by Niya on 05/10/2021.
 *
 */

@Database(entities = {Notification.class}, version = 5)
public abstract class NotificationsDatabase extends RoomDatabase {
    private static NotificationsDatabase INSTANCE;

    public abstract NotificationDao notificationDao();

    public static NotificationsDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            // allowMainThreadQueries should not be used, it is added so the query can be executed in
            // the main thread, now we know that it is quick enough and works for now, but should be refactored!!!
            INSTANCE = Room.databaseBuilder(context, NotificationsDatabase.class, "notifications_db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
