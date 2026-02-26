package com.Sumeru.WearBus.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.database.BusDatabase;
import com.Sumeru.WearBus.database.BusLineDao;
import com.Sumeru.WearBus.models.BusLineDetail;
import com.Sumeru.WearBus.models.BusLineMapping;
import com.Sumeru.WearBus.network.ApiService;
import com.Sumeru.WearBus.network.RetrofitClient;
import com.Sumeru.WearBus.utils.CityManager;
import com.Sumeru.WearBus.utils.SecureKeyManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Bus extends AppCompatActivity {
    private static final String TAG = "BusLineApp";
    private EditText etSearch;
    private Button btnSearch;
    private Button btnNearbyStations;

    private CityManager cityManager;
    private BusLineDao busLineDao;

    // 使用SecureKeyManager获取API密钥（更安全的方式）
    private String getDevId() {
        return SecureKeyManager.getApiDevId(this);
    }
    
    private String getDevKey() {
        return SecureKeyManager.getApiDevKey(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.businterface);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnNearbyStations = findViewById(R.id.btn_nearby_stations);

        // 初始化城市管理与线路映射 DAO
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

            // 从本地数据库中查找「城市 + 线路号」对应的线路UUID
            BusLineMapping mapping = busLineDao.getByCityAndLineNumber(cityUuid, lineNumber);
            if (mapping == null || TextUtils.isEmpty(mapping.lineUuid)) {
                Toast.makeText(this, "数据库匹配失败，请联系开发者！", Toast.LENGTH_LONG).show();
                return;
            }

            searchBusLine(mapping.lineUuid);
        });

        btnNearbyStations.setOnClickListener(v -> {
            Intent intent = new Intent(Bus.this, NearbyStationsActivity.class);
            startActivity(intent);
        });
    }

    private void searchBusLine(String lineUuid) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在查询线路信息...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String devId = getDevId();
        String devKey = getDevKey();
        
        Log.d(TAG, "请求参数: id=" + devId + ", key=" + devKey + ", uuid=" + lineUuid);

        ApiService apiService = RetrofitClient.getApiService();
        Call<BusLineDetail> call = apiService.getBusLineDetail(
                devId,
                devKey,
                lineUuid
        );

        call.enqueue(new Callback<BusLineDetail>() {
            @Override
            public void onResponse(Call<BusLineDetail> call, Response<BusLineDetail> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    BusLineDetail busDetail = response.body();
                    if (busDetail != null) {
                        // 调试日志：输出关键信息
                        Log.d(TAG, "API响应: code=" + busDetail.getCode() + ", msg=" + busDetail.getMsg());
                        Log.d(TAG, "线路名称: " + busDetail.getSafeLinename());
                        Log.d(TAG, "站点数量: " + busDetail.getSafeStation().size());

                        Intent intent = new Intent(Bus.this, BusLineDetailActivity.class);
                        intent.putExtra("bus_detail", busDetail);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Bus.this, "未获取到线路数据", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "响应体为空");
                    }
                } else {
                    Toast.makeText(Bus.this, "线路查询失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "响应失败: code=" + response.code());
                }
            }

            @Override
            public void onFailure(Call<BusLineDetail> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(Bus.this, "网络出错了喵QAQ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "网络请求失败", t);
            }
        });
    }
}