package com.Sumeru.WearBus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button userguide = findViewById(R.id.userguide);
        userguide.setOnClickListener(v -> {
            Intent intent=new Intent(SettingsActivity.this,guideSplashActivity.class);
            startActivity(intent);
        });
        // 判断是否为首次创建（避免旋转时重复添加）
        if (savedInstanceState == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment)
                    .commit();
        }

    }
}