package com.Sumeru.WearBus.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 公交线路映射表：
 *  - cityUuid  : 城市唯一ID（对应 City.uuid）
 *  - lineNumber: 用户在当前城市看到/输入的公交线路号（如 1、10路、K1 等）
 *  - lineUuid  : 接口使用的线路UUID
 */
@Entity(tableName = "bus_lines")
public class BusLineMapping {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String cityUuid;

    public String lineNumber;

    public String lineUuid;
}

