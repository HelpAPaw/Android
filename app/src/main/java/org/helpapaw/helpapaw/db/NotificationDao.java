package org.helpapaw.helpapaw.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.helpapaw.helpapaw.data.models.Notification;

import java.util.List;

/**
 * Created by niya on 05/10/2021.
 * This class is used to access the 'signals' table from the database
 */
@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY dateReceived DESC")
    List<Notification> getAll();

    @Query("DELETE FROM notifications")
    void deleteAll();

    @Insert
    void insertAll(Notification... notifications);

    @Delete
    void delete(Notification notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveNotification(Notification notification);
}
