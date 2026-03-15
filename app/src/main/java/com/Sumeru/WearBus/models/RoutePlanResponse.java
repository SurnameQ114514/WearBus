package com.Sumeru.WearBus.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * apihz 公交路线规划接口 gongjiao.php 返回数据模型
 *
 * 示例：
 * {
 *   "code": 200,
 *   "datas": [
 *     {
 *       "lineName": "地铁10号线 |",
 *       "segments": [ ... ]
 *     }
 *   ]
 * }
 */
public class RoutePlanResponse {

    @SerializedName("code")
    public int code;

    @SerializedName("datas")
    public List<RoutePlan> datas;

    /** 单个方案（整条线路） */
    public static class RoutePlan {
        @SerializedName("lineName")
        public String lineName;

        @SerializedName("segments")
        public List<Segment> segments;
    }

    /** 某个方案中的一段（两站之间） */
    public static class Segment {
        @SerializedName("stationStart")
        public Station stationStart;

        @SerializedName("stationEnd")
        public Station stationEnd;

        /** 该段耗时（分钟） */
        @SerializedName("segmentTimes")
        public int segmentTimes;
    }

    /** 站点信息 */
    public static class Station {
        @SerializedName("name")
        public String name;

        @SerializedName("uuid")
        public String uuid;

        /** "lon,lat" */
        @SerializedName("lonlat")
        public String lonlat;
    }
}

