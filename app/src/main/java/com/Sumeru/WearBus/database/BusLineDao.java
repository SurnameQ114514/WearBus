package com.Sumeru.WearBus.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.Sumeru.WearBus.models.BusLineMapping;

@Dao
public interface BusLineDao {

    @Query("SELECT * FROM bus_lines WHERE cityUuid = :cityUuid AND lineNumber = :lineNumber LIMIT 1")
    BusLineMapping getByCityAndLineNumber(String cityUuid, String lineNumber);

    @Query("SELECT * FROM bus_lines WHERE lineNumber = :lineNumber LIMIT 1")
    BusLineMapping getByLineNumber(String lineNumber);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BusLineMapping mapping);
}

