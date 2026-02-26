package com.Sumeru.WearBus.utils;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.Sumeru.WearBus.database.BusLineInitializer;
import com.Sumeru.WearBus.database.CityInitializer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApplication extends Application {
    private static final ExecutorService initExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initExecutor.execute(() -> {
            CityInitializer.initIfNeeded(this);
            BusLineInitializer.initIfNeeded(this);
        });
    }
} 