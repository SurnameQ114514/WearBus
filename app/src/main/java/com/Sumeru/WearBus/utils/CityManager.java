package com.Sumeru.WearBus.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.Sumeru.WearBus.database.BusDatabase;
import com.Sumeru.WearBus.models.City;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CityManager {
    private static final String PREFS_NAME = "CityPrefs";
    private static final String KEY_CITY_UUID = "city_uuid";
    private static final String KEY_FIRST_LAUNCH = "first_launch";

    // 最近一次位置：只在新且准的时候用，避免跨城偏差（如误判到云浮）
    private static final long MAX_LAST_AGE_GPS_MS = 2 * 60 * 1000L;     // GPS 缓存最多 2 分钟
    private static final long MAX_LAST_AGE_NETWORK_MS = 30 * 1000L;     // 网络缓存最多 30 秒
    private static final float MAX_LAST_ACCURACY_M = 150f;              // 只接受 150 米内精度（尽量满足 ≤100m，稍作余量）

    private final Context context;
    private final LocationManager locationManager;
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();
    private volatile String lastLocateDebug = "";
    private volatile List<City> cachedCities = null;
    private volatile long cacheTimestamp = 0;
    private static final long CACHE_VALID_DURATION = 5 * 60 * 1000L;

    public CityManager(Context context) {
        this.context = context.getApplicationContext();
        locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public String getLastLocateDebug() {
        return lastLocateDebug;
    }

    private boolean hasAnyLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setDebug(String msg) {
        lastLocateDebug = msg;
        Log.d("CityManager", msg);
    }
    public String getCityUuid(String cityName) {
        if (cityName == null || cityName.isEmpty()) return "";
        
        List<City> cities = getCachedCities();
        if (cities == null || cities.isEmpty()) return "";
        
        for (City city : cities) {
            if (cityName.equals(city.name)) return city.uuid;
            if (!cityName.endsWith("市") && cityName.equals(city.name + "市")) return city.uuid;
            if (cityName.endsWith("县") && cityName.replace("县", "市").equals(city.name)) return city.uuid;
        }
        
        Log.w("CityManager", "City not found in database, name=" + cityName);
        return "";
    }

    // 检查首次启动
    public boolean isFirstLaunch() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    /**
     * 根据坐标在本地城市列表中查找最近的城市（不依赖网络逆地理，更稳定）
     */
    private City findNearestCity(Location location) {
        List<City> cities = getCachedCities();
        if (cities == null || cities.isEmpty()) return null;
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        City nearest = null;
        double minDistSq = Double.MAX_VALUE;
        for (City c : cities) {
            double dlat = c.latitude - lat;
            double dlng = c.longitude - lng;
            double distSq = dlat * dlat + dlng * dlng;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = c;
            }
        }
        return nearest;
    }

    private List<City> getCachedCities() {
        long now = System.currentTimeMillis();
        if (cachedCities != null && (now - cacheTimestamp) < CACHE_VALID_DURATION) {
            return cachedCities;
        }
        List<City> cities = BusDatabase.getInstance(context).cityDao().getAllCities();
        if (cities != null && !cities.isEmpty()) {
            cachedCities = cities;
            cacheTimestamp = now;
        }
        return cities;
    }

    /**
     * 从 Address 中按「从细到粗」收集候选地名，便于优先匹配县级市再地级市
     * 顺序：区/县/县级市(subLocality, subAdminArea) → 地级市(locality) → 省(adminArea)
     */
    private List<String> extractCandidateCityNames(Address address) {
        List<String> out = new java.util.ArrayList<>();
        String a = address.getSubLocality();
        if (a != null && !a.trim().isEmpty()) out.add(a.trim());
        a = address.getSubAdminArea();
        if (a != null && !a.trim().isEmpty() && !out.contains(a.trim())) out.add(a.trim());
        a = address.getLocality();
        if (a != null && !a.trim().isEmpty() && !out.contains(a.trim())) out.add(a.trim());
        a = address.getAdminArea();
        if (a != null && !a.trim().isEmpty()) {
            if (a.endsWith("省") || a.endsWith("市")) out.add(a.trim());
        }
        return out;
    }

    /**
     * 根据 Location 做逆地理得到城市名并回调；失败或无匹配时按坐标取最近城市
     */
    private void doGeocodeAndNotify(Location location, OnCityLocatedListener listener) {
        geocodeExecutor.execute(() -> {
            String displayName = "";
            String uuid = "";

            try {
                Geocoder geocoder = new Geocoder(context, Locale.CHINA);
                if (!Geocoder.isPresent()) {
                    setDebug("系统不支持逆地理解析，正在按坐标选择最近城市...");
                } else {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            3);

                    if (addresses != null && !addresses.isEmpty()) {
                        String matchedName = "";
                        for (Address addr : addresses) {
                            for (String candidate : extractCandidateCityNames(addr)) {
                                String u = getCityUuid(candidate);
                                if (!u.isEmpty()) {
                                    uuid = u;
                                    matchedName = candidate;
                                    break;
                                }
                            }
                            if (!uuid.isEmpty()) break;
                        }
                        if (!uuid.isEmpty()) {
                            saveCityPreference(uuid);
                            City c = BusDatabase.getInstance(context).cityDao().getCityByUuid(uuid);
                            displayName = c != null ? c.name : matchedName;
                            setDebug("已根据位置解析到城市：" + displayName);
                        } else {
                            setDebug("解析到的地名在本地数据中无匹配，正在按坐标选择最近城市...");
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("CityManager", "Geocoder error", e);
                setDebug("逆地理解析失败，正在按坐标选择最近城市...");
            }

            if (!uuid.isEmpty()) {
                listener.onCityLocated(uuid, displayName);
                return;
            }

            City nearest = findNearestCity(location);
            if (nearest != null) {
                saveCityPreference(nearest.uuid);
                listener.onCityLocated(nearest.uuid, nearest.name);
                setDebug("已按坐标选出最近城市：" + nearest.name);
            } else {
                setDebug("本地城市列表为空，无法根据坐标匹配城市");
                listener.onCityLocated("", "");
            }
        });
    }

    // 自动定位逻辑：优先用缓存位置，无缓存时请求新位置
    public void autoLocateCity(OnCityLocatedListener listener) {
        if (!hasAnyLocationPermission()) {
            setDebug("未获得定位权限，请在系统设置中授予位置信息访问权限");
            listener.onCityLocated("", "");
            return;
        }

        if (locationManager == null) {
            setDebug("系统不支持位置服务");
            listener.onCityLocated("", "");
            return;
        }

        // 1. 优先使用“足够新、精度在可接受范围内”的最近一次位置（NETWORK / GPS）
        Location best = null;
        long now = System.currentTimeMillis();

        best = pickBetterLocation(
                best,
                safeLastKnown(LocationManager.NETWORK_PROVIDER, now),
                now
        );

        best = pickBetterLocation(
                best,
                safeLastKnown(LocationManager.GPS_PROVIDER, now),
                now
        );

        if (best != null) {
            setDebug("已获取最近一次位置（provider=" + best.getProvider() + "），正在解析城市...");
            doGeocodeAndNotify(best, listener);
            return;
        }

        // 2. 没有合格的最近位置记录：请求一次新的定位结果（优先 GPS，其次 NETWORK）
        String provider = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        if (provider == null) {
            setDebug("系统位置服务已关闭，请在设置中打开定位");
            listener.onCityLocated("", "");
            return;
        }

        // 3. 请求实时定位：同时监听 GPS/网络，优先采用精度较好的结果，减少跨城漂移
        setDebug("正在获取实时定位（GPS 优先），请稍候...");
        requestFreshLocationWithAccuracy(listener);
    }

    private static final long REQUEST_LOCATION_TIMEOUT_MS = 18_000L; // 18 秒超时
    private static final float ACCEPTABLE_ACCURACY_M = 1500f;        // 1.5 公里内认为可用，便于县级市

    private void requestFreshLocationWithAccuracy(OnCityLocatedListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        final Object lock = new Object();
        final boolean[] completed = { false };
        final Location[] bestLocation = { null };

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location == null) return;
                synchronized (lock) {
                    if (completed[0]) return;
                    bestLocation[0] = pickBetterLocation(bestLocation[0], location, System.currentTimeMillis());
                    boolean goodEnough = location.hasAccuracy() && location.getAccuracy() <= ACCEPTABLE_ACCURACY_M;
                    if (goodEnough) {
                        completed[0] = true;
                        handler.removeCallbacksAndMessages(null);
                        try {
                            locationManager.removeUpdates(this);
                        } catch (SecurityException ignored) {}
                        setDebug("已获取到较准位置（" + location.getProvider() + "，精度约" + (int) location.getAccuracy() + "米），正在解析城市...");
                        doGeocodeAndNotify(location, listener);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        Runnable timeoutRunnable = () -> {
            synchronized (lock) {
                if (completed[0]) return;
                completed[0] = true;
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (SecurityException ignored) {}
                if (bestLocation[0] != null) {
                    setDebug("定位超时前收到位置（精度可能一般），正在解析城市...");
                    doGeocodeAndNotify(bestLocation[0], listener);
                } else {
                    setDebug("未能获取到位置，请到室外或开启 GPS 后重试");
                    listener.onCityLocated("", "");
                }
            }
        };

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 2000, 10, locationListener, Looper.getMainLooper());
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener, Looper.getMainLooper());
            }
            handler.postDelayed(timeoutRunnable, REQUEST_LOCATION_TIMEOUT_MS);
        } catch (SecurityException e) {
            setDebug("请求系统定位失败：" + e.getClass().getSimpleName());
            listener.onCityLocated("", "");
        }
    }

    /**
     * 安全获取指定 provider 的最近一次位置；网络缓存极易漂移，只接受很新且较准的
     */
    private Location safeLastKnown(String provider, long now) {
        if (locationManager == null) return null;
        try {
            if (!locationManager.isProviderEnabled(provider)) return null;
            Location loc = locationManager.getLastKnownLocation(provider);
            if (loc == null) return null;

            long maxAge = LocationManager.GPS_PROVIDER.equals(provider)
                    ? MAX_LAST_AGE_GPS_MS
                    : MAX_LAST_AGE_NETWORK_MS;
            long t = loc.getTime();
            if (t > 0 && now - t > maxAge) return null;
            if (loc.hasAccuracy() && loc.getAccuracy() > MAX_LAST_ACCURACY_M) return null;
            return loc;
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * 在两个候选位置中选出更合适的一个：
     * - 更新的优先
     * - 在时间相近时，精度更高（accuracy 更小）的优先
     */
    private Location pickBetterLocation(Location currentBest, Location candidate, long now) {
        if (candidate == null) return currentBest;
        if (currentBest == null) return candidate;

        long timeDelta = candidate.getTime() - currentBest.getTime();
        boolean isSignificantlyNewer = timeDelta > 2 * 60 * 1000L;   // 候选比当前新 2 分钟以上
        boolean isSignificantlyOlder = timeDelta < -2 * 60 * 1000L;  // 候选比当前旧 2 分钟以上

        if (isSignificantlyNewer) return candidate;
        if (isSignificantlyOlder) return currentBest;

        boolean hasAccBest = currentBest.hasAccuracy();
        boolean hasAccCand = candidate.hasAccuracy();

        if (hasAccBest && hasAccCand) {
            float accDelta = candidate.getAccuracy() - currentBest.getAccuracy();
            if (accDelta < -50f) { // 候选精度好很多
                return candidate;
            }
            if (Math.abs(accDelta) <= 50f && timeDelta > 0) {
                // 精度差不多，谁更新用谁
                return candidate;
            }
            return currentBest;
        }

        // 只有一个有 accuracy 时，优先有精度的
        if (hasAccCand && !hasAccBest) return candidate;
        if (hasAccBest && !hasAccCand) return currentBest;

        // 都没精度信息时，谁更新用谁
        return timeDelta > 0 ? candidate : currentBest;
    }

    // 保存城市设置
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void saveCityPreference(String uuid) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_CITY_UUID, uuid);
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }

    // 获取当前城市UUID
    public String getCurrentCityUuid() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CITY_UUID, "");
    }
    public interface OnCityLocatedListener {
        void onCityLocated(String uuid, String cityName);
    }
}