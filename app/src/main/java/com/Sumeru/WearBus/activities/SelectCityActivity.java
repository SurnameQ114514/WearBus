package com.Sumeru.WearBus.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.database.BusDatabase;
import com.Sumeru.WearBus.models.City;
import com.Sumeru.WearBus.utils.CityManager;

/**
 * 首次进入应用时的城市选择页：
 *  - 支持自动定位城市并保存
 *  - 支持手动输入城市名称并保存
 */
public class SelectCityActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 2001;
    private static final long LOCATE_TIMEOUT_MS = 12000L; // 12 秒超时

    private CityManager cityManager;
    private TextView tvLocatedCity;
    private EditText etCityName;
    private ProgressBar progressLocating;
    private TextView tvLocateStep;
    private Handler stepHandler;
    private Runnable stepUpdater;
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        cityManager = new CityManager(this);

        tvLocatedCity = findViewById(R.id.tv_located_city);
        etCityName = findViewById(R.id.et_city_name);
        progressLocating = findViewById(R.id.progress_locating);
        tvLocateStep = findViewById(R.id.tv_locate_step);
        Button btnLocate = findViewById(R.id.btn_auto_locate);
        Button btnSave = findViewById(R.id.btn_save_city);

        stepHandler = new Handler(Looper.getMainLooper());
        stepUpdater = new Runnable() {
            @Override
            public void run() {
                if (progressLocating != null && progressLocating.getVisibility() == View.VISIBLE) {
                    String step = cityManager.getLastLocateDebug();
                    if (tvLocateStep != null) {
                        tvLocateStep.setText(TextUtils.isEmpty(step) ? "正在准备定位..." : step);
                    }
                    stepHandler.postDelayed(this, 600);
                }
            }
        };

        // 显示当前已选城市（若有）；仅首次使用（未选过城市）时进入页面自动定位
        String currentUuid = cityManager.getCurrentCityUuid();
        if (!TextUtils.isEmpty(currentUuid)) {
            City currentCity = BusDatabase.getInstance(this).cityDao().getCityByUuid(currentUuid);
            if (currentCity != null) {
                etCityName.setText(currentCity.name);
            }
        } else {
            checkPermissionAndLocate();
        }

        btnLocate.setOnClickListener(v -> checkPermissionAndLocate());
        btnSave.setOnClickListener(this::onSaveCityClicked);
    }

    private void checkPermissionAndLocate() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            startAutoLocate();
        }
    }

    private void startAutoLocate() {
        Toast.makeText(this, "正在努力定位中...", Toast.LENGTH_SHORT).show();
        tvLocatedCity.setText("正在努力定位...");
        if (progressLocating != null) {
            progressLocating.setVisibility(View.VISIBLE);
        }
        if (tvLocateStep != null) {
            tvLocateStep.setText("正在准备定位喵...");
        }
        if (stepHandler != null) {
            if (stepUpdater != null) {
                stepHandler.removeCallbacks(stepUpdater);
                stepHandler.post(stepUpdater);
            }
            if (timeoutRunnable != null) {
                stepHandler.removeCallbacks(timeoutRunnable);
            }
            timeoutRunnable = () -> {
                if (progressLocating != null && progressLocating.getVisibility() == View.VISIBLE) {
                    progressLocating.setVisibility(View.GONE);
                    String debug = cityManager.getLastLocateDebug();
                    tvLocatedCity.setText("定位超时，请检查是否开启系统定位服务，或手动选择城市");
                    if (tvLocateStep != null) {
                        tvLocateStep.setText(TextUtils.isEmpty(debug)
                                ? "定位超时：未能从系统获取位置"
                                : debug);
                    }
                }
            };
            stepHandler.postDelayed(timeoutRunnable, LOCATE_TIMEOUT_MS);
        }
        cityManager.autoLocateCity((uuid, cityName) -> {
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (progressLocating != null) {
                    progressLocating.setVisibility(View.GONE);
                }
                if (stepHandler != null) {
                    if (stepUpdater != null) {
                        stepHandler.removeCallbacks(stepUpdater);
                    }
                    if (timeoutRunnable != null) {
                        stepHandler.removeCallbacks(timeoutRunnable);
                    }
                }
                if (TextUtils.isEmpty(cityName)) {
                    String debug = cityManager.getLastLocateDebug();
                    tvLocatedCity.setText("自动定位失败：" + (TextUtils.isEmpty(debug) ? "未知原因" : debug));
                    Toast.makeText(this, "定位失败了喵QAQ，请手动选择城市吧", Toast.LENGTH_LONG).show();
                    return;
                }

                etCityName.setText(cityName);

                if (!TextUtils.isEmpty(uuid)) {
                    tvLocatedCity.setText("已定位并保存城市：" + cityName + "，如需修改可手动输入后再保存");
                    Toast.makeText(this, "喵～已定位到：" + cityName, Toast.LENGTH_SHORT).show();
                } else {
                    tvLocatedCity.setText("已定位到城市：" + cityName + "，但未在数据库找到对应配置，请确认名称后再保存");
                    Toast.makeText(this, "已定位到：" + cityName + "，请确认后点击保存", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void onSaveCityClicked(View view) {
        String cityName = etCityName.getText().toString().trim();
        if (TextUtils.isEmpty(cityName)) {
            Toast.makeText(this, "请输入城市名称", Toast.LENGTH_SHORT).show();
            return;
        }

        City city = BusDatabase.getInstance(this)
                .cityDao()
                .getCityByName(cityName);

        if (city == null || TextUtils.isEmpty(city.uuid)) {
            Toast.makeText(this, "未在本地城市列表中找到该城市", Toast.LENGTH_LONG).show();
            return;
        }

        cityManager.saveCityPreference(city.uuid);
        Toast.makeText(this, "城市已设置为：" + city.name, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            boolean anyGranted = false;
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_GRANTED) {
                    anyGranted = true;
                    break;
                }
            }
            if (anyGranted) {
            startAutoLocate();
            }
        }
    }
}

