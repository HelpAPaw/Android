package org.helpapaw.helpapaw.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by Alex on 11/11/2017.
 * This class is used to access the 'signals' table from the database
 */
@Dao
public interface SignalDao {
    @Query("SELECT * FROM signals")
    List<Signal> getAll();

    @Query("SELECT * FROM signals where signal_id = :signal_id")
    List<Signal> getSignal(String signal_id);

    @Query("SELECT * FROM signals WHERE signal_id IN (:signal_ids)")
    List<Signal> getSignals(String[] signal_ids);

    @Insert
    void insertAll(Signal... signals);

    @Delete
    void delete(Signal signal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSignal(Signal signal);
}
