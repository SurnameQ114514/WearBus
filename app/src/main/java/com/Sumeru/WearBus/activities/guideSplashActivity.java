// 完整的 SplashActivity.java
package com.Sumeru.WearBus.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

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
            "1.公交板块\n可搜索线路，2km附近站点",
            "2.去哪里：公交线路规划，需要完整输入目的地的名称",
            "3.设置板块\n可以修改所在城市及再次查看用户教程",
            "提示:线路数据库工作量巨大，所以如果你所在的城市线路查询不到或是个别线路不存在请加入qq群并at开发者；还有就是手动输入城市的时候请务必将名称输入完整，问就是开发者太懒了喵",
            "提示2:县级市可能要手动输入",
            "提示3:数据库太老了，将就着用，以后可能会换掉",
            "4.敬请期待 欢迎加入我们的QQ群150895672"
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