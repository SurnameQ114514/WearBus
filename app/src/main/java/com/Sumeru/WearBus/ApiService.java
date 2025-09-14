package com.Sumeru.WearBus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("jiaotong/gongjiao2.php")
    Call<BusLineDetail> getBusLineDetail(
            @Query("id") String devId,
            @Query("key") String devKey,
            @Query("uuid") String lineUuid
    );
}