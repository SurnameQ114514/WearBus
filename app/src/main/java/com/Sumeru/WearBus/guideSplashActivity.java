// 完整的 SplashActivity.java
package com.Sumeru.WearBus;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class guideSplashActivity extends AppCompatActivity {
    private TextView tvContent;
    private TextView tvConsole;
    private ScrollView svScroll;
    private View cursorView;
    private Handler handler = new Handler();
    private int currentCharIndex = 0;
    private int currentParagraph = 0;

    // 分段文本数组
    private final String[] paragraphs = {
            "欢迎使用腕上公交，这里是用户教程",
            "1.公交板块\n内有附近站点和搜索板块",
            "2.设置板块\n目前有用户教程",
            "3.敬请期待 欢迎加入我们的QQ群150895672"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 初始化视图
        tvContent = findViewById(R.id.tv_print_content);
        tvConsole = findViewById(R.id.tv_console);
        svScroll = findViewById(R.id.sv_scroll);
        cursorView = findViewById(R.id.cursor_view);

        // 启动光标动画
        startCursorAnimation();

        // 设置跳过点击事件

        // 开始段落打印动画
        startParagraphAnimation();
    }

    // 光标闪烁动画
    private void startCursorAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.2f, 1.0f);
        animator.setDuration(600);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            cursorView.setAlpha((Float) animation.getAnimatedValue());
        });
        animator.start();
    }

    private void startParagraphAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentCharIndex < paragraphs[currentParagraph].length()) {
                    // 获取下一个字符
                    char nextChar = paragraphs[currentParagraph].charAt(currentCharIndex);

                    // 添加到当前文本
                    StringBuilder sb = new StringBuilder(tvContent.getText());
                    sb.append(nextChar);
                    tvContent.setText(sb.toString());

                    // 同时添加到控制台
                    String consoleText = tvConsole.getText().toString();
                    tvConsole.setText(consoleText + nextChar);

                    // 自动滚动到底部
                    svScroll.post(() -> svScroll.fullScroll(ScrollView.FOCUS_DOWN));

                    currentCharIndex++;

                    // 继续下一个字符（50ms间隔）
                    handler.postDelayed(this, 50);
                } else {
                    // 当前段落完成
                    tvConsole.append("\n"); // 控制台换行
                    currentCharIndex = 0;

                    // 段落结束后的延迟（2秒）
                    handler.postDelayed(() -> {
                        if (currentParagraph < paragraphs.length - 1) {
                            // 清屏并开始下一段落
                            tvContent.setText("");
                            currentParagraph++;

                            // 控制台添加分隔线
                            tvConsole.append("--------------------------------\n");

                            // 开始打印下一段落
                            handler.post(this);
                        } else {
                            // 全部完成，延迟1秒后跳转
                            handler.postDelayed(() -> navigateToMain(), 1000);
                        }
                    }, 2000);
                }
            }
        }, 1000); // 初始延迟
    }

    private void navigateToMain() {
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(guideSplashActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}