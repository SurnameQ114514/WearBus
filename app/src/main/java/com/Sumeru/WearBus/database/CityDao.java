package com.Sumeru.WearBus.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.Sumeru.WearBus.models.City;

import java.util.List;

@Dao
public interface CityDao {
    @Query("SELECT * FROM cities WHERE name = :name LIMIT 1")
    City getCityByName(String name);

    @Query("SELECT * FROM cities WHERE uuid = :uuid LIMIT 1")
    City getCityByUuid(String uuid);

    /** 获取全部城市（用于按坐标计算最近城市） */
    @Query("SELECT * FROM cities")
    List<City> getAllCities();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(City city);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<City> cities);
}