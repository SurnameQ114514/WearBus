package com.Sumeru.WearBus.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.Sumeru.WearBus.models.BusLineMapping;

@Dao
public interface BusLineDao {

    /**
     * 根据「城市UUID + 线路号」查找对应的线路UUID
     */
    @Query("SELECT * FROM bus_lines WHERE cityUuid = :cityUuid AND lineNumber = :lineNumber LIMIT 1")
    BusLineMapping getByCityAndLineNumber(String cityUuid, String lineNumber);

    /**
     * 插入或更新一条映射记录（便于后续批量导入或维护）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BusLineMapping mapping);
}

