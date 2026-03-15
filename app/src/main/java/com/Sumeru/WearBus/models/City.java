package com.Sumeru.WearBus.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cities")
public class City {
    @PrimaryKey
    @NonNull
    public String uuid;

    public String name;
    public double latitude;
    public double longitude;
}