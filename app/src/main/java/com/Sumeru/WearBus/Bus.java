package com.Sumeru.WearBus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Bus extends AppCompatActivity {
    private static final String TAG = "BusLineApp";
    private EditText etSearch;
    private Button btnSearch;

    // 更新为正确的开发者ID和密钥
    private static final String DEV_ID = "10008097";
    private static final String DEV_KEY = "3a26b84060a6e08290e04410246c4f95";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.businterface);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(v -> {
            String lineUuid = etSearch.getText().toString().trim();
            if (!lineUuid.isEmpty()) {
                searchBusLine(lineUuid);
            } else {
                Toast.makeText(this, "请输入公交线路UUID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchBusLine(String lineUuid) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在查询线路信息...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Log.d(TAG, "请求参数: id=" + DEV_ID + ", key=" + DEV_KEY + ", uuid=" + lineUuid);

        ApiService apiService = RetrofitClient.getApiService();
        Call<BusLineDetail> call = apiService.getBusLineDetail(
                DEV_ID,
                DEV_KEY,
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
                Toast.makeText(Bus.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "网络请求失败", t);
            }
        });
    }
}