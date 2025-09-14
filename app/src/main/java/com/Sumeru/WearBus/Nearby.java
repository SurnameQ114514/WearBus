package com.Sumeru.WearBus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Nearby extends AppCompatActivity implements LocationListener {
    private TextView nowAddress;
    private TextView detailAddress;  // 新增：显示详细地址信息的TextView
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearbyinterface);

        // 初始化视图
        nowAddress = findViewById(R.id.tv_nowAddress);
        detailAddress = findViewById(R.id.tv_detailAddress);  // 关联布局中新增的TextView

        if (checkLocationPermission()) {
            initLocation();
        }
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 检查GPS状态
        if (!isGpsEnabled()) {
            Toast.makeText(this, "请开启位置服务喵~", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        // 混合定位策略
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000, 10, this);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 10, this);
        }
    }

    private boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationManager.removeUpdates(this);
        executor.execute(() -> reverseGeocode(location));
    }

    private void reverseGeocode(Location location) {
        Geocoder geocoder = new Geocoder(Nearby.this, Locale.CHINA); // 🔥 使用中文环境

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses == null || addresses.isEmpty()) {
                runOnUiThread(() -> {
                    nowAddress.setText("无法获取地址喵~");
                    detailAddress.setText("");
                });
                return;
            }

            Address address = addresses.get(0);
            String fullAddress = buildFullAddress(address);     // 🔥 获取完整地址
            String detailedInfo = buildDetailedAddress(address); // 🔥 获取详细地址

            runOnUiThread(() -> {
                nowAddress.setText(fullAddress);        // 显示完整地址
                detailAddress.setText(detailedInfo);    // 显示详细地址信息
            });

        } catch (IOException e) {
            Log.e("Geocoder", "地址解析错误: " + e.getMessage());
            runOnUiThread(() -> {
                nowAddress.setText("地址解析失败喵...");
                detailAddress.setText("网络错误~杂鱼~");
            });
        }
    }

    // 🔥 构建完整地址（短格式）
    private String buildFullAddress(Address address) {
        // 按顺序拼接：省、市、区、街道
        return safeAppend(
                address.getAdminArea(),
                address.getLocality(),
                address.getSubLocality(),
                address.getThoroughfare()
        );
    }

    // 🔥 构建详细地址信息（长格式）
    private String buildDetailedAddress(Address address) {
        StringBuilder sb = new StringBuilder();

        // 经纬度信息
        sb.append("坐标: ")
                .append(String.format("%.6f", address.getLatitude()))
                .append(", ")
                .append(String.format("%.6f", address.getLongitude()))
                .append("\n");

        // 行政区划信息
        if (address.getSubAdminArea() != null) {
            sb.append("区域: ").append(address.getSubAdminArea()).append("\n");
        }

//        // 街道信息
//        if (address.getThoroughfare() != null) {
//            sb.append("街道: ").append(address.getThoroughfare()).append("\n");
//        }

        // 特征名（如建筑物名称）
        if (address.getFeatureName() != null) {
            sb.append("位置: ").append(address.getFeatureName());
        }

        return sb.toString();
    }

    // 🔥 安全拼接非空值
    private String safeAppend(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part);
            }
        }
        return sb.length() > 0 ? sb.toString() : "未知位置";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        executor.shutdown();
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}
}

