package org.helpapaw.helpapaw.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by Alex on 11/11/2017.
 */
@Dao
public interface SignalDao {
    @Query("SELECT * FROM signals")
    List<Signal> getAll();

    @Query("SELECT * FROM signals where signal_id = :signal_id")
    public List<Signal> getSignal(long signal_id);

    @Insert
    void insertAll(Signal... signals);

    @Delete
    void delete(Signal signal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSignal(Signal signal);
}
