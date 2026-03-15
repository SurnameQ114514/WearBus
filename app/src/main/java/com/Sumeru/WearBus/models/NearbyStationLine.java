package com.Sumeru.WearBus.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NearbyStationLine implements Serializable {
    @SerializedName("stationUuid")
    public String stationUuid;

    @SerializedName("lineName")
    public String lineName;

    @SerializedName("uuid")
    public String uuid;

    public static NearbyStationLine fromJson(JsonObject obj) {
        if (obj == null) return null;
        NearbyStationLine l = new NearbyStationLine();
        if (obj.has("stationUuid") && !obj.get("stationUuid").isJsonNull()) {
            l.stationUuid = obj.get("stationUuid").getAsString();
        }
        if (obj.has("lineName") && !obj.get("lineName").isJsonNull()) {
            l.lineName = obj.get("lineName").getAsString();
        }
        if (obj.has("uuid") && !obj.get("uuid").isJsonNull()) {
            l.uuid = obj.get("uuid").getAsString();
        }
        return l;
    }

    public String getSafeLineName() {
        return lineName != null && !lineName.isEmpty() ? lineName : "未知线路";
    }
}

