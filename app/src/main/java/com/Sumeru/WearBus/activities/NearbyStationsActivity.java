package com.Sumeru.WearBus.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.adapters.NearbyStationAdapter;
import com.Sumeru.WearBus.models.NearbyPoi;
import com.Sumeru.WearBus.models.NearbyPoiResponse;
import com.Sumeru.WearBus.network.ApiService;
import com.Sumeru.WearBus.network.RetrofitClient;
import com.Sumeru.WearBus.utils.SecureKeyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearbyStationsActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 3001;
    private static final int RADIUS_M = 2000;
    private static final long LOCATE_TIMEOUT_MS = 20_000L;   // 20 秒内尽量等到高精度
    private static final float ACCEPTABLE_ACCURACY_M = 100f; // 明确要求 ≤100m 的精度

    // 使用SecureKeyManager获取API密钥（更安全的方式）
    private String getDevId() {
        return SecureKeyManager.getApiDevId(this);
    }
    
    private String getDevKey() {
        return SecureKeyManager.getApiDevKey(this);
    }

    private ProgressBar progress;
    private TextView tvStep;
    private RecyclerView rv;
    private TextView tvEmpty;
    private Button btnLoadMore;

    private NearbyStationAdapter adapter;

    private LocationManager locationManager;
    private Handler handler;
    private boolean locatingDone = false;
    private Location bestLocation = null;
    private LocationListener listener;
    private Runnable timeoutRunnable;

    private Location lastQueryLocation;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int allPage = 1;
    private final List<NearbyPoi> currentStations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_stations);

        progress = findViewById(R.id.progress_loading);
        tvStep = findViewById(R.id.tv_step);
        rv = findViewById(R.id.rv_stations);
        tvEmpty = findViewById(R.id.tv_empty);
        btnLoadMore = findViewById(R.id.btn_load_more);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NearbyStationAdapter(new ArrayList<>(), poi -> {
            NearbyStationDetailActivity.start(this, poi);
        });
        rv.setAdapter(adapter);

        btnLoadMore.setOnClickListener(v -> loadNextPage());

        handler = new Handler(Looper.getMainLooper());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        checkPermissionAndStart();
    }

    private void checkPermissionAndStart() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
            return;
        }
        startLocate();
    }

    private void startLocate() {
        showLoading("正在获取位置...");
        locatingDone = false;
        bestLocation = null;

        if (locationManager == null) {
            showError("系统不支持位置服务");
            return;
        }

        boolean gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean netOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsOn && !netOn) {
            showError("系统定位服务未开启，请先打开定位");
            return;
        }

        // 先尝试 lastKnown（很新且准才用）
        Location last = getGoodLastKnown(gpsOn, netOn);
        if (last != null) {
            showLoading("已获取到位置，正在查询站点...");
            fetchStations(last);
            return;
        }

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (locatingDone) return;
                bestLocation = pickBetter(bestLocation, location);
                if (location.hasAccuracy() && location.getAccuracy() <= ACCEPTABLE_ACCURACY_M) {
                    locatingDone = true;
                    stopUpdates();
                    showLoading("已获取到较准位置，正在查询站点...");
                    fetchStations(location);
                } else {
                    showLoading("定位中（精度约" + (location.hasAccuracy() ? (int) location.getAccuracy() : -1) + "米）...");
                }
            }

            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(@NonNull String provider) {}
            @Override public void onProviderDisabled(@NonNull String provider) {}
        };

        try {
            if (gpsOn) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, listener, Looper.getMainLooper());
            }
            if (netOn) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, listener, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            showError("请求定位失败：" + e.getClass().getSimpleName());
            return;
        }

        timeoutRunnable = () -> {
            if (locatingDone) return;
            locatingDone = true;
            stopUpdates();
            if (bestLocation != null) {
                showLoading("定位超时，使用当前最佳位置查询...");
                fetchStations(bestLocation);
            } else {
                showError("定位超时，请到室外或开启GPS后重试");
            }
        };
        handler.postDelayed(timeoutRunnable, LOCATE_TIMEOUT_MS);
    }

    private void stopUpdates() {
        if (timeoutRunnable != null) handler.removeCallbacks(timeoutRunnable);
        if (locationManager != null && listener != null) {
            try {
                locationManager.removeUpdates(listener);
            } catch (SecurityException ignored) {}
        }
    }

    private Location getGoodLastKnown(boolean gpsOn, boolean netOn) {
        long now = System.currentTimeMillis();
        Location best = null;
        if (gpsOn) {
            best = tryLastKnown(LocationManager.GPS_PROVIDER, now, 5 * 60 * 1000L);
        }
        if (netOn) {
            Location net = tryLastKnown(LocationManager.NETWORK_PROVIDER, now, 60 * 1000L);
            best = pickBetter(best, net);
        }
        // 必须“够准”才用 lastKnown
        if (best != null && best.hasAccuracy() && best.getAccuracy() <= ACCEPTABLE_ACCURACY_M) return best;
        return null;
    }

    private Location tryLastKnown(String provider, long now, long maxAge) {
        try {
            Location loc = locationManager.getLastKnownLocation(provider);
            if (loc == null) return null;
            long t = loc.getTime();
            if (t > 0 && now - t > maxAge) return null;
            return loc;
        } catch (SecurityException e) {
            return null;
        }
    }

    private Location pickBetter(Location a, Location b) {
        if (b == null) return a;
        if (a == null) return b;
        // 精度优先，其次时间
        if (a.hasAccuracy() && b.hasAccuracy()) {
            if (b.getAccuracy() + 50 < a.getAccuracy()) return b;
            if (a.getAccuracy() + 50 < b.getAccuracy()) return a;
        }
        return b.getTime() > a.getTime() ? b : a;
    }

    private void fetchStations(Location loc) {
        // 重置分页
        lastQueryLocation = loc;
        currentPage = 1;
        allPage = 1;
        currentStations.clear();
        adapter.update(new ArrayList<>());
        btnLoadMore.setVisibility(View.GONE);
        fetchStationsPage(1);
    }

    private void loadNextPage() {
        if (isLoading) return;
        if (lastQueryLocation == null) return;
        if (currentPage >= Math.min(allPage, 10)) return; // 接口最大 10 页
        fetchStationsPage(currentPage + 1);
    }

    private void fetchStationsPage(int page) {
        if (lastQueryLocation == null) return;
        isLoading = true;
        if (page == 1) {
            showLoading("正在查询2km内站点...");
        } else {
            Toast.makeText(this, "正在加载第" + page + "页呢～", Toast.LENGTH_SHORT).show();
        }

        String devId = getDevId();
        String devKey = getDevKey();
        
        ApiService api = RetrofitClient.getApiService();
        Call<NearbyPoiResponse> call = api.searchNearbyPoi(
                devId,
                devKey,
                "公交",
                RADIUS_M,
                lastQueryLocation.getLongitude(),
                lastQueryLocation.getLatitude(),
                page,
                2,   // 按你的建议，将 show 参数改为 2
                null  // 不限定 type，兼容 101/102，按返回的 poiType 和名字自行筛选
        );

        call.enqueue(new Callback<NearbyPoiResponse>() {
            @Override
            public void onResponse(Call<NearbyPoiResponse> call, Response<NearbyPoiResponse> response) {
                isLoading = false;
                if (!response.isSuccessful() || response.body() == null) {
                    showError("查询失败：" + response.code());
                    return;
                }
                NearbyPoiResponse body = response.body();
                if (body.code != 200) {
                    showError("接口返回错误：" + body.code + (TextUtils.isEmpty(body.msg) ? "" : (" " + body.msg)));
                    return;
                }
                allPage = body.allpage > 0 ? body.allpage : 1;
                currentPage = body.nowpage > 0 ? body.nowpage : page;

                List<NearbyPoi> list = body.datas != null ? body.datas : new ArrayList<>();
                // 兼容两种情况：
                // - poiType=102：公交站点，一般会带 stationData（有线路）
                // - poiType=101：名字里包含“公交”，也当作公交站点，但多数没有线路信息
                List<NearbyPoi> newStations = new ArrayList<>();
                for (NearbyPoi p : list) {
                    if (p == null) continue;
                    if (TextUtils.isEmpty(p.name)) continue;
                    boolean isBusType = "102".equals(p.poiType);
                    boolean nameLikeStation = p.name.contains("公交");
                    if (!isBusType && !nameLikeStation) continue;
                    newStations.add(p);
                }

                // 去重合并（hotPointID 优先，其次 name+lonlat）
                mergeDedup(currentStations, newStations);
                sortByDistance(currentStations);

                showList(currentStations);
                updateLoadMoreButton();
            }

            @Override
            public void onFailure(Call<NearbyPoiResponse> call, Throwable t) {
                isLoading = false;
                showError("网络错误：" + t.getMessage());
            }
        });
    }

    private void mergeDedup(List<NearbyPoi> base, List<NearbyPoi> incoming) {
        if (incoming == null || incoming.isEmpty()) return;
        if (base == null) return;

        HashMap<String, Boolean> existingKeys = new HashMap<>();
        for (NearbyPoi e : base) {
            if (e == null) continue;
            String key1 = e.hotPointID != null ? e.hotPointID : "";
            String key2 = (e.name != null ? e.name : "") + "|" + (e.lonlat != null ? e.lonlat : "");
            if (!key1.isEmpty()) {
                existingKeys.put("id:" + key1, true);
            }
            existingKeys.put("loc:" + key2, true);
        }

        for (NearbyPoi p : incoming) {
            if (p == null) continue;
            String key1 = p.hotPointID != null ? p.hotPointID : "";
            String key2 = (p.name != null ? p.name : "") + "|" + (p.lonlat != null ? p.lonlat : "");
            
            boolean exists = false;
            if (!key1.isEmpty() && existingKeys.containsKey("id:" + key1)) {
                exists = true;
            } else if (existingKeys.containsKey("loc:" + key2)) {
                exists = true;
            }
            
            if (!exists) {
                base.add(p);
                if (!key1.isEmpty()) {
                    existingKeys.put("id:" + key1, true);
                }
                existingKeys.put("loc:" + key2, true);
            }
        }
    }

    private void updateLoadMoreButton() {
        int maxPage = Math.min(allPage, 10);
        boolean canLoadMore = currentPage < maxPage;
        btnLoadMore.setVisibility(canLoadMore ? View.VISIBLE : View.GONE);
        if (canLoadMore) {
            btnLoadMore.setText("加载更多（第" + (currentPage + 1) + "/" + maxPage + "页）");
        }
    }

    private void sortByDistance(List<NearbyPoi> list) {
        Collections.sort(list, Comparator.comparingDouble(this::distanceToMetersSafe));
    }

    private double distanceToMetersSafe(NearbyPoi p) {
        if (p == null || p.distance == null) return Double.MAX_VALUE;
        String d = p.distance.trim().toLowerCase();
        try {
            if (d.endsWith("m")) {
                return Double.parseDouble(d.replace("m", "").trim());
            }
            if (d.endsWith("km")) {
                return Double.parseDouble(d.replace("km", "").trim()) * 1000.0;
            }
        } catch (NumberFormatException ignored) {}
        return Double.MAX_VALUE;
    }

    private void showLoading(String step) {
        progress.setVisibility(View.VISIBLE);
        tvStep.setVisibility(View.VISIBLE);
        tvStep.setText(step);
        rv.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.GONE);
    }

    private void showList(List<NearbyPoi> stations) {
        progress.setVisibility(View.GONE);
        tvStep.setVisibility(View.GONE);
        if (stations == null || stations.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("2km内未查询到站点");
            rv.setVisibility(View.GONE);
            btnLoadMore.setVisibility(View.GONE);
            return;
        }
        adapter.update(stations);
        rv.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        progress.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(msg);
        tvStep.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        stopUpdates();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean anyGranted = false;
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_GRANTED) {
                    anyGranted = true;
                    break;
                }
            }
            if (anyGranted) {
                startLocate();
            } else {
                showError("未获得定位权限，无法查询附近站点");
            }
        }
    }
}

