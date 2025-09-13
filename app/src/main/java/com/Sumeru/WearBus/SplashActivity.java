package com.Sumeru.WearBus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private TextView tvContent;
    private StringBuilder displayText = new StringBuilder();
    private int currentCharIndex = 0;
    private Handler handler = new Handler();
    private String fullText = "欢迎使用\n腕上公交"; // 完整文本

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash1);
        // 全屏配置省略...

        tvContent = findViewById(R.id.tv_print_content);
        startTypingAnimation();
    }

    // 逐字动画核心逻辑
    private void startTypingAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentCharIndex < fullText.length()) {
                    char nextChar = fullText.charAt(currentCharIndex);
                    displayText.append(nextChar);
                    tvContent.setText(displayText.toString());
                    currentCharIndex++;
                    handler.postDelayed(this, 50); // 调整速度：50ms/字
                } else {
                    // 跳转主界面
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 2000); // 初始延迟500ms
    }
}