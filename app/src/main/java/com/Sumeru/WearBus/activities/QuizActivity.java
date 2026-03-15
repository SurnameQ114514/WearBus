package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuizProgress;
    private TextView tvQuestion;
    private RadioGroup rgOptions;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private Button btnSubmit;
    
    private int currentQuestionIndex = 0;
    private int wrongAnswers = 0;
    private long additionalTime = 0;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // 题库
    private Question[] questions = {
        new Question(
            "WearBus 可能会收集以下哪些信息？",
            new String[]{"设备信息", "位置信息", "使用记录", "以上都是"},
            3 // D
        ),
        new Question(
            "关于用户行为规范，以下哪项是正确的？",
            new String[]{"可以将账号借给朋友", "不可以利用应用从事商业活动", "应遵守法律法规，不从事违法活动", "可以随意修改应用代码"},
            2 // C
        ),
        new Question(
            "WearBus 使用位置权限的主要目的是什么？",
            new String[]{"监控用户行踪", "查询周边站点和实时公交", "推送广告", "没有实际用途"},
            1 // B
        ),
        new Question(
            "关于隐私保护，以下说法正确的是？",
            new String[]{"我们会出售个人信息给第三方", "互联网环境 100% 安全", "建议不要分享敏感个人信息", "儿童可以随意使用应用"},
            2 // C
        ),
        new Question(
            "如果答题错误，会发生什么？",
            new String[]{"直接通过", "需要重新阅读协议并增加 5 秒阅读时间", "应用会卸载", "没有任何影响"},
            1 // B
        )
    };
    
    // 开发者识别问题（第 6 题）
    private Question developerQuestion = new Question(
        "如果您是开发者，请继续-->",
        new String[]{"我是普通用户，直接跳过", "我是开发者，我想了解项目", "我是开发者，已加入 QQ 群"},
        1 // B
    );
    
    // GPL v3 相关问题（第 7-9 题）
    private Question[] gplQuestions = {
        new Question(
            "WearBus 基于以下哪种开源协议？",
            new String[]{"MIT", "Apache 2.0", "GNU GPL v3", "BSD 3-Clause"},
            2 // C
        ),
        new Question(
            "关于 GPL v3 协议，以下说法正确的是？",
            new String[]{
                "完全禁止商用",
                "允许商用，但修改后必须开源并使用相同协议",
                "可以闭源商用",
                "商用不需要遵守任何规则"
            },
            1 // B
        ),
        new Question(
            "GPL v3 协议的核心要求是什么？",
            new String[]{
                "保持开源和自由",
                "禁止商用",
                "必须收费",
                "只能用于学习"
            },
            0 // A
        )
    };
    
    // 最后的灵魂拷问（第 10 题）
    private Question finalQuestion = new Question(
        "你是否知道对开源软件进行商用是极为不道德的？",
        new String[]{"是，我了解并尊重开源精神", "不是，我觉得可以随便商用"},
        0 // A
    );
    
    private boolean isDeveloperMode = false;
    private int currentStage = 0; // 0=普通问题，1=开发者问题，2=GPL 问题，3=灵魂拷问
    
    private static class Question {
        String questionText;
        String[] options;
        int correctAnswer;
        
        Question(String questionText, String[] options, int correctAnswer) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        
        // 获取额外时间（如果有答错的题目）
        wrongAnswers = getIntent().getIntExtra("wrong_answers", 0);
        additionalTime = getIntent().getLongExtra("additional_time", 0);
        
        initViews();
        loadQuestion(currentQuestionIndex);
    }
    
    private void initViews() {
        tvQuizProgress = findViewById(R.id.tv_quiz_progress);
        tvQuestion = findViewById(R.id.tv_question);
        rgOptions = findViewById(R.id.rg_options);
        rbOptionA = findViewById(R.id.rb_option_a);
        rbOptionB = findViewById(R.id.rb_option_b);
        rbOptionC = findViewById(R.id.rb_option_c);
        rbOptionD = findViewById(R.id.rb_option_d);
        btnSubmit = findViewById(R.id.btn_submit);
    }
    
    private void loadQuestion(int index) {
        if (index >= questions.length) {
            // 前 5 题答完，询问是否是开发者
            loadDeveloperQuestion();
            return;
        }
        
        Question q = questions[index];
        tvQuizProgress.setText(String.format("第 %d/%d 题", index + 1, questions.length));
        tvQuestion.setText(q.questionText);
        
        rbOptionA.setText("A. " + q.options[0]);
        rbOptionB.setText("B. " + q.options[1]);
        rbOptionC.setText("C. " + q.options[2]);
        rbOptionD.setText("D. " + q.options[3]);
        
        rbOptionA.setVisibility(View.VISIBLE);
        rbOptionB.setVisibility(View.VISIBLE);
        rbOptionC.setVisibility(View.VISIBLE);
        rbOptionD.setVisibility(View.VISIBLE);
        
        rgOptions.clearCheck();
        
        // 确保使用普通问题的监听器
        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "请先选择一个答案喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int selectedOption = getSelectedOptionIndex(selectedId);
            
            if (selectedOption == q.correctAnswer) {
                // 答对了
                currentQuestionIndex++;
                loadQuestion(currentQuestionIndex);
            } else {
                // 答错了
                wrongAnswers++;
                additionalTime += 5000;
                Toast.makeText(this, "答错了喵～需要重新阅读协议并增加 5 秒阅读时间", Toast.LENGTH_LONG).show();
                
                handler.postDelayed(() -> {
                    Intent intent = new Intent(QuizActivity.this, AgreementActivity.class);
                    intent.putExtra("wrong_answers", wrongAnswers);
                    intent.putExtra("additional_time", additionalTime);
                    startActivity(intent);
                    finish();
                }, 2000);
            }
        });
    }
    
    private void loadDeveloperQuestion() {
        tvQuizProgress.setText("第 6/9 题");
        tvQuestion.setText(developerQuestion.questionText);
        
        rbOptionA.setText("A. " + developerQuestion.options[0]);
        rbOptionB.setText("B. " + developerQuestion.options[1]);
        rbOptionC.setText("C. " + developerQuestion.options[2]);
        
        rbOptionA.setVisibility(View.VISIBLE);
        rbOptionB.setVisibility(View.VISIBLE);
        rbOptionC.setVisibility(View.VISIBLE);
        rbOptionD.setVisibility(View.GONE);
        
        rgOptions.clearCheck();
        
        // 设置第 6 题的点击事件
        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "请先选择一个答案喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int selectedOption = getSelectedOptionIndex(selectedId);
            handleDeveloperAnswer(selectedOption);
        });
    }
    
    private void showGPLIntroduction() {
        tvQuizProgress.setText("开发者须知");
        tvQuestion.setText("📖 WearBus 是基于 GNU GPL v3 开源协议的开源项目。\n\n" +
                "GPL v3 要求：\n" +
                "• 修改后必须开源\n" +
                "• 必须使用相同协议\n" +
                "• 保留原作者署名\n" +
                "• 不得限制用户的自由\n\n" +
                "📄 详细文档：https://www.gnu.org/licenses/gpl-3.0.html\n\n" +
                "请尊重开源精神，继续答题喵～");
        
        rbOptionA.setText("我了解了，继续答题");
        rbOptionB.setText("退出");
        rbOptionC.setText("查看 GPL v3 官方文档");
        rbOptionD.setVisibility(View.GONE);
        
        rgOptions.clearCheck();
        
        // 临时修改点击事件
        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "请选择喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedId == rbOptionA.getId()) {
                // 开始 GPL 问题
                currentStage = 2;
                currentQuestionIndex = 0;
                loadGPLQuestion();
            } else if (selectedId == rbOptionC.getId()) {
                // 打开 GPL v3 官方文档
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://www.gnu.org/licenses/gpl-3.0.html"));
                    startActivity(intent);
                    Toast.makeText(this, "已在浏览器中打开 GPL v3 文档喵～", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "无法打开浏览器喵～", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 退出
                finish();
            }
        });
    }
    
    private void loadGPLQuestion() {
        if (currentQuestionIndex >= gplQuestions.length) {
            // GPL 问题答完，进入灵魂拷问
            loadFinalQuestion();
            return;
        }
        
        Question q = gplQuestions[currentQuestionIndex];
        tvQuizProgress.setText(String.format("GPL 知识 %d/%d", currentQuestionIndex + 1, gplQuestions.length));
        tvQuestion.setText(q.questionText);
        
        rbOptionA.setText("A. " + q.options[0]);
        rbOptionB.setText("B. " + q.options[1]);
        rbOptionC.setText("C. " + q.options[2]);
        rbOptionD.setText("D. " + q.options[3]);
        
        rbOptionA.setVisibility(View.VISIBLE);
        rbOptionB.setVisibility(View.VISIBLE);
        rbOptionC.setVisibility(View.VISIBLE);
        rbOptionD.setVisibility(View.VISIBLE);
        
        rgOptions.clearCheck();
        
        // 设置 GPL 问题的点击事件
        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "请先选择一个答案喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int selectedOption = getSelectedOptionIndex(selectedId);
            
            if (selectedOption == q.correctAnswer) {
                // 答对了
                currentQuestionIndex++;
                loadGPLQuestion();
            } else {
                // 答错了
                wrongAnswers++;
                additionalTime += 5000;
                Toast.makeText(this, "答错了喵～GPL 知识需要加强！重新阅读协议并增加 5 秒", Toast.LENGTH_LONG).show();
                
                handler.postDelayed(() -> {
                    Intent intent = new Intent(QuizActivity.this, AgreementActivity.class);
                    intent.putExtra("wrong_answers", wrongAnswers);
                    intent.putExtra("additional_time", additionalTime);
                    startActivity(intent);
                    finish();
                }, 2000);
            }
        });
    }
    
    private void loadFinalQuestion() {
        tvQuizProgress.setText("最后的拷问");
        tvQuestion.setText(finalQuestion.questionText);
        
        rbOptionA.setText("A. " + finalQuestion.options[0]);
        rbOptionB.setText("B. " + finalQuestion.options[1]);
        rbOptionC.setVisibility(View.GONE);
        rbOptionD.setVisibility(View.GONE);
        
        rgOptions.clearCheck();
        
        // 设置灵魂拷问的点击事件
        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "请选择喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            
            handleFinalAnswer(selectedId == rbOptionA.getId());
        });
    }
    
    private void handleDeveloperAnswer(int selectedOption) {
        // 根据选择设置开发者模式
        if (selectedOption != 0) {
            isDeveloperMode = true;
        }
        
        // 如果是普通用户（选项 A），直接进入主界面
        if (selectedOption == 0) {
            SharedPreferences prefs = getSharedPreferences("wearbus_prefs", MODE_PRIVATE);
            prefs.edit()
                 .putBoolean("agreement_accepted", true)
                 .putInt("wrong_answers", wrongAnswers)
                 .putInt("developer_mode", 0)
                 .apply();
            
            Toast.makeText(this, "感谢配合喵～欢迎使用 WearBus！", Toast.LENGTH_SHORT).show();
            
            handler.postDelayed(() -> {
                Intent intent = new Intent(QuizActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 1500);
        } else {
            // 开发者，进入 GPL 介绍
            currentStage = 2;
            showGPLIntroduction();
        }
    }
    
    private void handleFinalAnswer(boolean isMoral) {
        SharedPreferences prefs = getSharedPreferences("wearbus_prefs", MODE_PRIVATE);
        prefs.edit()
             .putBoolean("agreement_accepted", true)
             .putInt("wrong_answers", wrongAnswers)
             .putInt("developer_mode", isDeveloperMode ? 1 : 0)
             .putBoolean("respects_opensource", isMoral)
             .apply();
        
        if (isMoral) {
            // 尊重开源精神，显示欢迎信息
            Toast.makeText(this, "欢迎加入开源社区！一起维护开源精神喵～", Toast.LENGTH_SHORT).show();
        } else {
            // 不尊重开源精神，跳转到知乎问题
            Toast.makeText(this, "请理解开源精神后再来喵～", Toast.LENGTH_LONG).show();
            
            handler.postDelayed(() -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://www.zhihu.com/question/536099947"));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }, 1500);
            return;
        }
        
        handler.postDelayed(() -> {
            Intent intent = new Intent(QuizActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1500);
    }
    
    private int getSelectedOptionIndex(int radioButtonId) {
        if (radioButtonId == rbOptionA.getId()) return 0;
        if (radioButtonId == rbOptionB.getId()) return 1;
        if (radioButtonId == rbOptionC.getId()) return 2;
        if (radioButtonId == rbOptionD.getId()) return 3;
        return -1;
    }
}
