package com.Sumeru.WearBus.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class NearbyPoiResponse implements Serializable {
    @SerializedName("code")
    public int code;

    @SerializedName("msg")
    public String msg;

    @SerializedName("count")
    public int count;

    @SerializedName("allpage")
    public int allpage;

    @SerializedName("nowpage")
    public int nowpage;

    @SerializedName("datas")
    public List<NearbyPoi> datas;
}

