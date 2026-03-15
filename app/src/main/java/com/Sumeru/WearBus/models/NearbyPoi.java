package com.Sumeru.WearBus.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NearbyPoi implements Serializable {
    @SerializedName("address")
    public String address;

    @SerializedName("distance")
    public String distance;

    @SerializedName("phone")
    public String phone;

    @SerializedName("poiType")
    public String poiType; // 101 POI, 102 公交站点

    @SerializedName("name")
    public String name;

    @SerializedName("source")
    public String source;

    @SerializedName("hotPointID")
    public String hotPointID;

    @SerializedName("lonlat")
    public String lonlat; // "lon,lat"

    @SerializedName("province")
    public String province;

    @SerializedName("city")
    public String city;

    @SerializedName("county")
    public String county;

    @SerializedName("typeName")
    public String typeName;

    @SerializedName("typeCode")
    public String typeCode;

    @SerializedName("stationData")
    public JsonElement stationData;

    public boolean isBusStation() {
        return "102".equals(poiType) || (stationData != null && !stationData.isJsonNull());
    }

    public List<NearbyStationLine> getStationLines() {
        List<NearbyStationLine> out = new ArrayList<>();
        if (stationData == null || stationData.isJsonNull()) return out;
        if (stationData.isJsonArray()) {
            for (JsonElement e : stationData.getAsJsonArray()) {
                if (e != null && e.isJsonObject()) {
                    NearbyStationLine line = NearbyStationLine.fromJson(e.getAsJsonObject());
                    if (line != null) out.add(line);
                }
            }
        } else if (stationData.isJsonObject()) {
            NearbyStationLine line = NearbyStationLine.fromJson(stationData.getAsJsonObject());
            if (line != null) out.add(line);
        }
        return out;
    }

    public String getSafeName() {
        return name != null && !name.isEmpty() ? name : "未知站点";
    }

    public String getSafeDistance() {
        return distance != null ? distance : "";
    }

    public String getSafeAddress() {
        return address != null ? address : "";
    }

    public String getLineSummary(int maxCount) {
        List<NearbyStationLine> lines = getStationLines();
        if (lines.isEmpty()) return "线路信息未知";
        StringBuilder sb = new StringBuilder();
        int count = Math.min(maxCount, lines.size());
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append("、");
            sb.append(lines.get(i).getSafeLineName());
        }
        if (lines.size() > maxCount) sb.append(" 等").append(lines.size()).append("条");
        return sb.toString();
    }

    public Double getLon() {
        double[] ll = parseLonLat();
        return ll == null ? null : ll[0];
    }

    public Double getLat() {
        double[] ll = parseLonLat();
        return ll == null ? null : ll[1];
    }

    private double[] parseLonLat() {
        if (lonlat == null) return null;
        String[] parts = lonlat.split(",");
        if (parts.length != 2) return null;
        try {
            return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

