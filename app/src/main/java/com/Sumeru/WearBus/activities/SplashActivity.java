package com.Sumeru.WearBus.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private TextView tvContent;
    private StringBuilder displayText = new StringBuilder();
    private int currentCharIndex = 0;
    private Handler handler = new Handler();
    private String fullText = "欢迎使用\nWearBus"; // 完整文本

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
                    // 跳转协议页面，添加短暂延迟让动画完成
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 检查是否已经同意过协议
                            android.content.SharedPreferences prefs = getSharedPreferences("wearbus_prefs", MODE_PRIVATE);
                            boolean agreementAccepted = prefs.getBoolean("agreement_accepted", false);
                            
                            if (agreementAccepted) {
                                // 已同意，直接跳转到主界面
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            } else {
                                // 未同意，跳转到协议页面
                                startActivity(new Intent(SplashActivity.this, AgreementActivity.class));
                            }
                            finish();
                        }
                    }, 200); // 200ms 延迟，让 finish() 动画完成
                }
            }
        }, 500); // 初始延迟500ms
    }
}