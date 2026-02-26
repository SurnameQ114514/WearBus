package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

public class AboutActivity extends AppCompatActivity {

    private int clickCount = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews();
    }

    private void initViews() {
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvVersion = findViewById(R.id.tv_version);
        TextView tvDeveloper = findViewById(R.id.tv_developer);
        TextView tvContact = findViewById(R.id.tv_contact);
        TextView tvLicense = findViewById(R.id.tv_license);
        Button btnBack = findViewById(R.id.btn_back);

        tvAppName.setText("腕上公交");
        
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("版本: " + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("版本: 未知");
        }
        
        tvDeveloper.setText("开发者: Sumeru");
        tvContact.setText("联系方式:\n微信: DreamerQ2022\nQQ: 2016319616");
        tvLicense.setText("开源协议: GPL-3.0 License\n版权所有 © 2026 Sumeru");

        tvVersion.setOnClickListener(v -> {
            clickCount++;
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> clickCount = 0, 1000);
            
            if (clickCount == 8) {
                try {
                    Intent intent = new Intent(AboutActivity.this, HiddenActivity.class);
                    startActivity(intent);
                    clickCount = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    // 启动失败时显示提示
                    android.widget.Toast.makeText(AboutActivity.this, "启动失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    clickCount = 0;
                }
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}