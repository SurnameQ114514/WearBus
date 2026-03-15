package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
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

        tvAppName.setText("WearBus");
        
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("版本: " + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("版本: 未知");
        }
        
        tvDeveloper.setText("开发者: Sumeru");
        tvContact.setText("联系方式:\n微信: DreamerQ2022\nQQ: 2016319616");
        
        // 设置开源协议为可点击链接
        String licenseText = "开源协议: GNU GPL v3.0\n版权所有 © 2026 Sumeru";
        SpannableString spannableString = new SpannableString(licenseText);
        
        // 找到 "GNU GPL v3.0" 的位置
        int start = licenseText.indexOf("GNU GPL v3.0");
        int end = start + "GNU GPL v3.0".length();
        
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://www.gnu.org/licenses/gpl-3.0.html"));
                startActivity(intent);
            }
        };
        
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLicense.setText(spannableString);
        tvLicense.setMovementMethod(LinkMovementMethod.getInstance());

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
    }
}
