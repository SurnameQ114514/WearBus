package com.Sumeru.WearBus.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.adapters.RouteListAdapter;
import com.Sumeru.WearBus.models.RoutePlanResponse;
import com.Sumeru.WearBus.network.ApiService;
import com.Sumeru.WearBus.network.RetrofitClient;
import com.Sumeru.WearBus.utils.SecureKeyManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * 「去哪里」：输入目的地，从当前位置规划公交/地铁路线（使用 apihz 公交规划 API）
 */
public class WhereToActivity extends AppCompatActivity {

    private static final String TAG = "WhereToActivity";
    private static final int REQ_LOCATION = 4001;
    // 使用SecureKeyManager获取API密钥（更安全的方式）
    private String getDevId() {
        return SecureKeyManager.getApiDevId(this);
    }
    
    private String getDevKey() {
        return SecureKeyManager.getApiDevKey(this);
    }
    private static final long LOCATE_TIMEOUT_MS = 15_000L;

    private EditText etDestination;
    private View btnPlan;
    private ProgressBar progress;
    private TextView tvHint;
    private RecyclerView rvRoutes;
    private TextView tvEmpty;

    private RouteListAdapter adapter;
    private LocationManager locationManager;
    private Handler handler;
    private ExecutorService executor;
    private boolean locatingDone;
    private Location bestLocation;
    private LocationListener locationListener;
    private Runnable timeoutRunnable;
    private long lastRequestId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_to);

        etDestination = findViewById(R.id.et_destination);
        btnPlan = findViewById(R.id.btn_plan);
        progress = findViewById(R.id.progress_where_to);
        tvHint = findViewById(R.id.tv_hint);
        rvRoutes = findViewById(R.id.rv_routes);
        tvEmpty = findViewById(R.id.tv_empty);

        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteListAdapter();
        rvRoutes.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        executor = Executors.newSingleThreadExecutor();

        btnPlan.setOnClickListener(v -> startPlan());
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        if (executor != null && !executor.isShutdown()) executor.shutdown();
        super.onDestroy();
    }

    private void startPlan() {
        String destText = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";
        if (destText.isEmpty()) {
            Toast.makeText(this, "请完整输入你想去的地方喵", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        tvHint.setVisibility(View.VISIBLE);
        tvHint.setText("正在帮你找位置喵～");
        rvRoutes.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        if (!checkLocationPermission()) {
            setLoading(false);
            tvHint.setVisibility(View.GONE);
            return;
        }
        startGetLocation(destText);
    }

    private boolean checkLocationPermission() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String dest = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";
            if (!dest.isEmpty()) {
                setLoading(true);
                tvHint.setText("正在获取当前位置...");
                startGetLocation(dest);
            }
        } else if (requestCode == REQ_LOCATION) {
            setLoading(false);
            tvHint.setVisibility(View.GONE);
            Toast.makeText(this, "需要位置权限才能规划路线", Toast.LENGTH_SHORT).show();
        }
    }

    private void startGetLocation(String destText) {
        locatingDone = false;
        bestLocation = null;

        if (locationManager == null) {
            showError("系统不支持定位");
            return;
        }
        boolean gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean netOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsOn && !netOn) {
            setLoading(false);
            tvHint.setVisibility(View.GONE);
            showLocationDisabledDialog();
            return;
        }

        Location last = getLastKnown();
        if (last != null) {
            locatingDone = true;
            tvHint.setText("正在解析目的地并规划路线...");
            geocodeAndRequest(destText, last.getLatitude(), last.getLongitude());
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (locatingDone) return;
                if (bestLocation == null || (location.hasAccuracy() && (!bestLocation.hasAccuracy() || location.getAccuracy() < bestLocation.getAccuracy()))) {
                    bestLocation = location;
                }
                if (location.hasAccuracy() && location.getAccuracy() <= 150f) {
                    locatingDone = true;
                    stopLocationUpdates();
                    tvHint.setText("正在解析目的地并规划路线...");
                    geocodeAndRequest(destText, location.getLatitude(), location.getLongitude());
                }
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(@NonNull String provider) {}
            @Override public void onProviderDisabled(@NonNull String provider) {}
        };

        try {
            if (gpsOn) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener, Looper.getMainLooper());
            if (netOn) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener, Looper.getMainLooper());
        } catch (SecurityException e) {
            showError("定位权限异常");
            return;
        }

        timeoutRunnable = () -> {
            if (locatingDone) return;
            locatingDone = true;
            stopLocationUpdates();
            if (bestLocation != null) {
                tvHint.setText("正在解析目的地并规划路线...");
                geocodeAndRequest(destText, bestLocation.getLatitude(), bestLocation.getLongitude());
            } else {
                showError("定位超时，请到室外或开启 GPS 后重试");
            }
        };
        handler.postDelayed(timeoutRunnable, LOCATE_TIMEOUT_MS);
    }

    private Location getLastKnown() {
        if (locationManager == null) return null;
        long now = System.currentTimeMillis();
        Location best = null;
        try {
            Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gps != null && now - gps.getTime() < 5 * 60 * 1000 && gps.hasAccuracy() && gps.getAccuracy() <= 200f) best = gps;
            Location net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (net != null && now - net.getTime() < 60 * 1000) {
                if (best == null || (net.hasAccuracy() && best.hasAccuracy() && net.getAccuracy() < best.getAccuracy())) best = net;
                else if (best == null) best = net;
            }
        } catch (SecurityException ignored) {}
        return best;
    }

    private void stopLocationUpdates() {
        if (timeoutRunnable != null) handler.removeCallbacks(timeoutRunnable);
        if (locationManager != null && locationListener != null) {
            try { locationManager.removeUpdates(locationListener); } catch (SecurityException ignored) {}
        }
    }

    private void geocodeAndRequest(String destText, double originLat, double originLng) {
        final long currentRequestId = ++lastRequestId;
        executor.execute(() -> {
            double destLat = 0, destLng = 0;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocationName(destText, 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);
                    destLat = a.getLatitude();
                    destLng = a.getLongitude();
                }
            } catch (IOException ignored) {}

            final double fDestLat = destLat;
            final double fDestLng = destLng;
            runOnUiThread(() -> {
                // 只处理最新请求的结果
                if (currentRequestId != lastRequestId) {
                    return;
                }
                if (fDestLat == 0 && fDestLng == 0) {
                    showError("无法解析目的地「" + destText + "」，请尝试更详细的地址或地标");
                    return;
                }
                requestTransit(originLat, originLng, fDestLat, fDestLng);
            });
        });
    }

    /**
     * 调用 apihz 公交规划接口
     */
    private void requestTransit(double originLat, double originLng, double destLat, double destLng) {
        final long currentRequestId = lastRequestId;
        // 接口要求经纬度顺序：starlon=起点经度，starlat=起点纬度，endlon=终点经度，endlat=终点纬度
        double startLon = originLng;
        double startLat = originLat;
        double endLon = destLng;
        double endLat = destLat;

        Log.d(TAG, "请求公交路线(apihz): startLon=" + startLon + ", startLat=" + startLat
                + ", endLon=" + endLon + ", endLat=" + endLat);

        String devId = getDevId();
        String devKey = getDevKey();
        
        ApiService api = RetrofitClient.getApiService();
        // linetype=1, type=0 按示例默认
        Call<RoutePlanResponse> call = api.getRoutePlan(devId, devKey, startLon, startLat, endLon, endLat, 1, 0);

        call.enqueue(new Callback<RoutePlanResponse>() {
            @Override
            public void onResponse(@NonNull Call<RoutePlanResponse> call, @NonNull Response<RoutePlanResponse> response) {
                // 只处理最新请求的结果
                if (currentRequestId != lastRequestId) {
                    return;
                }
                setLoading(false);
                tvHint.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    String msg = "请求失败: HTTP " + response.code();
                    showError(msg);
                    return;
                }
                RoutePlanResponse body = response.body();
                Log.d(TAG, "apihz 公交返回 code=" + body.code
                        + ", routeCount=" + (body.datas != null ? body.datas.size() : 0));

                if (body.code != 200) {
                    showError("路线规划接口返回错误，code=" + body.code);
                    return;
                }
                if (body.datas == null || body.datas.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("未找到公交/地铁路线，可尝试更换目的地或起点");
                    rvRoutes.setVisibility(View.GONE);
                    return;
                }
                tvEmpty.setVisibility(View.GONE);
                rvRoutes.setVisibility(View.VISIBLE);
                adapter.setRoutes(body.datas);
            }

            @Override
            public void onFailure(@NonNull Call<RoutePlanResponse> call, @NonNull Throwable t) {
                // 只处理最新请求的结果
                if (currentRequestId != lastRequestId) {
                    return;
                }
                setLoading(false);
                tvHint.setVisibility(View.GONE);
                Log.e(TAG, "apihz 公交请求失败", t);
                showError("网络错误: " + (t.getMessage() != null ? t.getMessage() : "请检查网络"));
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPlan.setEnabled(!loading);
    }

    private void showLocationDisabledDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("神秘错误");
        builder.setMessage("杂鱼~定位服务被禁用喵～请在系统设置中开启定位服务喵！");
        builder.setPositiveButton("前往设置", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "打不开定位设置喵～请手动前往：设置 → 定位服务", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(msg);
        rvRoutes.setVisibility(View.GONE);
    }
}
