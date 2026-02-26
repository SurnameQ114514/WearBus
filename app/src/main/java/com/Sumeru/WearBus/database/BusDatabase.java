package com.Sumeru.WearBus.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.Sumeru.WearBus.models.BusLineMapping;
import com.Sumeru.WearBus.models.City;

@Database(entities = {City.class, BusLineMapping.class}, version = 2, exportSchema = false)
public abstract class BusDatabase extends RoomDatabase {
    private static volatile BusDatabase instance;

    // DAO 接口
    public abstract CityDao cityDao();

    // 公交线路映射 DAO
    public abstract BusLineDao busLineDao();

    // 单例模式
    public static synchronized BusDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            BusDatabase.class, "bus_database")
                    // 项目规模较小，允许在主线程访问数据库，简化使用
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}