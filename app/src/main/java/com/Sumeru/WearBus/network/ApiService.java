package com.Sumeru.WearBus.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.Sumeru.WearBus.models.BusLineDetail;
import com.Sumeru.WearBus.models.NearbyPoiResponse;
import com.Sumeru.WearBus.models.RoutePlanResponse;

public interface ApiService {
    @GET("jiaotong/gongjiao2.php")
    Call<BusLineDetail> getBusLineDetail(
            @Query("id") String devId,
            @Query("key") String devKey,
            @Query("uuid") String lineUuid
    );

    @GET("other/diming.php")
    Call<NearbyPoiResponse> searchNearbyPoi(
            @Query("id") String devId,
            @Query("key") String devKey,
            @Query("words") String words,
            @Query("radius") int radius,
            @Query("lon") double lon,
            @Query("lat") double lat,
            @Query("page") int page,
            @Query("show") int show,
            @Query("type") String type
    );

    /**
     * 公交路线规划
     * 示例：
     * https://cn.apihz.cn/api/jiaotong/gongjiao.php?id=...&key=...&starlon=121.4279&starlat=31.20872&endlon=121.313079&endlat=31.195667&linetype=1&type=0
     */
    @GET("jiaotong/gongjiao.php")
    Call<RoutePlanResponse> getRoutePlan(
            @Query("id") String devId,
            @Query("key") String devKey,
            @Query("starlon") double startLon,
            @Query("starlat") double startLat,
            @Query("endlon") double endLon,
            @Query("endlat") double endLat,
            @Query("linetype") int lineType,
            @Query("type") int type
    );
}
