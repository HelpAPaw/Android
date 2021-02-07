package org.helpapaw.helpapaw.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import org.helpapaw.helpapaw.data.models.Signal;

/**
 * Created by Alex on 11/11/2017.
 *
 */

@Database(entities = {Signal.class}, version = 3)
public abstract class SignalsDatabase extends RoomDatabase {
    private static SignalsDatabase INSTANCE;

    public abstract SignalDao signalDao();

    public static SignalsDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            // allowMainThreadQueries should not be used, it is added so the query can be executed in
            // the main thread, now we know that it is quick enough and works for now, but should be refactored!!!
            INSTANCE = Room.databaseBuilder(context, SignalsDatabase.class, "signals_db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
