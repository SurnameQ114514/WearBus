package com.Sumeru.WearBus.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.database.BusDatabase;
import com.Sumeru.WearBus.database.BusLineDao;
import com.Sumeru.WearBus.models.BusLineDetail;
import com.Sumeru.WearBus.models.BusLineMapping;
import com.Sumeru.WearBus.network.ApiService;
import com.Sumeru.WearBus.network.RetrofitClient;
import com.Sumeru.WearBus.utils.CityManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Bus extends BaseActivity {
    private EditText etSearch;
    private Button btnSearch;
    private Button btnNearbyStations;

    private CityManager cityManager;
    private BusLineDao busLineDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.businterface);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnNearbyStations = findViewById(R.id.btn_nearby_stations);

        cityManager = new CityManager(this);
        busLineDao = BusDatabase.getInstance(this).busLineDao();

        btnSearch.setOnClickListener(v -> {
            String lineNumber = etSearch.getText().toString().trim();
            if (TextUtils.isEmpty(lineNumber)) {
                Toast.makeText(this, "请输入公交线路号", Toast.LENGTH_SHORT).show();
                return;
            }

            String cityUuid = cityManager.getCurrentCityUuid();
            if (TextUtils.isEmpty(cityUuid)) {
                Toast.makeText(this, "当前尚未选择城市，请先完成城市选择", Toast.LENGTH_LONG).show();
                return;
            }

            // 先尝试从本地数据库查询
            new Thread(() -> {
                BusLineMapping mapping = busLineDao.getByCityAndLineNumber(cityUuid, lineNumber);
                if (mapping != null && !TextUtils.isEmpty(mapping.lineUuid)) {
                    // 有本地缓存，直接用UUID查询
                    runOnUiThread(() -> fetchBusLineFromApi(lineNumber, mapping.lineUuid));
                } else {
                    // 没有缓存，提示用户
                    runOnUiThread(() -> {
                        Toast.makeText(Bus.this, "该线路未收录，请联系管理员添加", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        btnNearbyStations.setOnClickListener(v -> {
            Intent intent = new Intent(Bus.this, NearbyStationsActivity.class);
            startActivity(intent);
        });
    }

    private void fetchBusLineFromApi(String lineNumber, String lineUuid) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("查询中...");
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService();
        Call<BusLineDetail> call = apiService.getBusLineDetail(getDevId(), getDevKey(), lineUuid);

        call.enqueue(new Callback<BusLineDetail>() {
            @Override
            public void onResponse(Call<BusLineDetail> call, Response<BusLineDetail> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    BusLineDetail busLineDetail = response.body();
                    
                    if (busLineDetail.getCode() != 200) {
                        String errMsg = busLineDetail.getMsg();
                        Toast.makeText(Bus.this, errMsg != null ? errMsg : "查询失败", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(Bus.this, BusLineDetailActivity.class);
                    intent.putExtra("bus_number", lineNumber);
                    intent.putExtra("bus_detail", busLineDetail);
                    startActivity(intent);
                } else {
                    Toast.makeText(Bus.this, "未找到该线路", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BusLineDetail> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(Bus.this, "查询失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
