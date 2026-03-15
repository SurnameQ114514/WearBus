package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

public class AgreementActivity extends AppCompatActivity {

    private ScrollView scrollAgreement;
    private TextView tvAgreementContent;
    private TextView tvTimer;
    private TextView tvScrollTip;
    private Button btnAgree;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private long startTime;
    private static final long MIN_READ_TIME = 10000; // 10 秒
    private long additionalTime = 0;
    
    private boolean hasScrolledToBottom = false;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        
        // 获取额外时间（如果有答错的题目）
        additionalTime = getIntent().getLongExtra("additional_time", 0);
        
        initViews();
        loadAgreementContent();
        setupListeners();
        startTimer();
    }
    
    private void initViews() {
        scrollAgreement = findViewById(R.id.scroll_agreement);
        tvAgreementContent = findViewById(R.id.tv_agreement_content);
        tvTimer = findViewById(R.id.tv_timer);
        tvScrollTip = findViewById(R.id.tv_scroll_tip);
        btnAgree = findViewById(R.id.btn_agree);
    }
    
    private void loadAgreementContent() {
        String content = "欢迎使用 WearBus\n\n" +
                "【用户协议】\n\n" +
                "1. 服务说明\n" +
                "WearBus 是一款提供公交查询服务的应用，包括实时公交、线路规划、站点查询等功能。我们致力于为您提供准确的公交信息，但实际运营情况可能因交通状况、天气等因素有所变化。\n\n" +
                "2. 用户行为规范\n" +
                "您在使用本应用时，应遵守国家相关法律法规，不得利用本应用从事任何违法违规活动。您应对自己的账号安全负责，不得将账号提供给他人使用。\n\n" +
                "3. 知识产权声明\n" +
                "本应用的所有内容（包括但不限于文字、图片、音频、视频、软件、程序等）均受知识产权法保护，归 WearBus 团队或相关权利人所有。未经书面许可，不得复制、传播、修改或用于商业用途。\n\n" +
                "4. 免责声明\n" +
                "因网络、设备、系统或其他不可抗力因素导致的服务中断或数据丢失，WearBus 不承担责任。您在使用本应用过程中产生的间接损失（如时间、商业机会等），我们不承担赔偿责任。\n\n" +
                "5. 协议修改\n" +
                "我们有权根据需要修改本协议内容，修改后的协议将在应用内公布。如您继续使用本应用，即视为接受修改后的协议。\n\n" +
                "【隐私声明】\n\n" +
                "1. 信息收集\n" +
                "为了提供更好的服务，我们可能会收集以下信息：\n" +
                "• 设备信息（设备型号、系统版本、唯一设备标识符）\n" +
                "• 位置信息（用于提供周边站点和公交查询服务）\n" +
                "• 使用记录（查询历史、收藏线路等）\n\n" +
                "2. 信息使用\n" +
                "我们收集的信息仅用于：\n" +
                "• 提供和优化公交查询服务\n" +
                "• 改进用户体验\n" +
                "• 保障服务安全\n" +
                "我们不会将您的个人信息出售或提供给第三方用于商业营销。\n\n" +
                "3. 信息保护\n" +
                "我们采取严格的技术措施保护您的信息安全，防止数据泄露、丢失或被滥用。但请您理解，互联网环境并非 100% 安全，建议您不要通过本应用分享敏感个人信息。\n\n" +
                "4. 权限说明\n" +
                "本应用可能需要以下权限：\n" +
                "• 位置权限：用于查询周边站点和实时公交\n" +
                "• 网络权限：获取实时公交数据\n" +
                "• 存储权限：缓存线路数据以提升加载速度\n" +
                "您可以在系统设置中随时管理这些权限。\n\n" +
                "5. 儿童隐私\n" +
                "我们非常重视儿童隐私保护。如果您是未成年人，请在监护人指导下使用本应用。我们不会故意收集儿童的个人信息。\n\n" +
                "6. 隐私政策更新\n" +
                "我们可能会适时更新本隐私政策。更新后的政策将在应用内公布，重大变更会通过显著方式通知您。\n\n" +
                "【联系我们】\n\n" +
                "如您对本协议或隐私声明有任何疑问，欢迎通过以下方式联系我们：\n" +
                "• GitHub: github.com/SurnameQ114514/WearBus\n\n" +
                "感谢您选择 WearBus，祝您出行愉快！喵～";
        
        tvAgreementContent.setText(content);
    }
    
    private void setupListeners() {
        scrollAgreement.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View lastChild = scrollAgreement.getChildAt(scrollAgreement.getChildCount() - 1);
            int offset = lastChild.getBottom() - scrollAgreement.getHeight() - scrollAgreement.getScrollY();
            
            if (offset <= 0) {
                hasScrolledToBottom = true;
                tvScrollTip.setVisibility(View.GONE);
                checkEnableButton();
            } else {
                if (!hasScrolledToBottom) {
                    tvScrollTip.setVisibility(View.VISIBLE);
                }
            }
        });
        
        btnAgree.setOnClickListener(v -> {
            stopTimer();
            Intent intent = new Intent(AgreementActivity.this, QuizActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                long requiredTime = MIN_READ_TIME + additionalTime;
                long remaining = Math.max(0, requiredTime - elapsed);
                long seconds = remaining / 1000;
                
                if (remaining > 0) {
                    if (additionalTime > 0) {
                        tvTimer.setText(String.format("因答错题目，请再阅读 %d 秒", seconds));
                    } else {
                        tvTimer.setText(String.format("请阅读至少 %d 秒", seconds));
                    }
                    handler.postDelayed(this, 1000);
                } else {
                    if (additionalTime > 0) {
                        tvTimer.setText("✓ 额外阅读时间已达要求");
                    } else {
                        tvTimer.setText("✓ 阅读时间已达要求");
                    }
                    checkEnableButton();
                }
            }
        };
        handler.post(timerRunnable);
    }
    
    private void stopTimer() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
    
    private void checkEnableButton() {
        long elapsed = System.currentTimeMillis() - startTime;
        boolean timeReached = elapsed >= MIN_READ_TIME;
        btnAgree.setEnabled(hasScrolledToBottom && timeReached);
        
        if (hasScrolledToBottom && timeReached) {
            btnAgree.setText("我同意");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
