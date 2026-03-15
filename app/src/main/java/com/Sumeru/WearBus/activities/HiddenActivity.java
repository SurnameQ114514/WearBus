package com.Sumeru.WearBus.activities;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

import java.io.IOException;
public class HiddenActivity extends AppCompatActivity {

    private Button btnBack;
    private Button btnNotice;
    private HorizontalScrollView chapterScroll;
    private LinearLayout chapterSelector;
    private LinearLayout chapter1, chapter2, chapter3, chapter4, chapter5;
    private TextView tvNovelTitle, tvNovelText;
    private ImageButton btnPlay;
    private TextView tvBgmTitle;
    private TextView tvMessage;
    private Handler handler = new Handler();
    private RelativeLayout noticeOverlay;
    private View noticeDialog;
    private Dialog noticeAlertDialog;
    private boolean isAnimating = true;

    // 消息序列
    private String[] messages = {
            "佩戴耳机以获得最佳体验",
            "\"Innocent Snow\" written by Sumeru"
    };
    private int messageIndex = 0;

    // 小说内容存储
    private String[][] novelContents = {
            {
                "Chapter 0",
                "A tiny girl stood vigil between her parents' sickbeds, her small frame illuminated by sunlight streaming through the window. The sterile room felt strangely peaceful in this golden moment, just her and the breathing rhythm of machines keeping time. She settled by the windowsill, watching sparrows quarrel in budding trees - their vibrant chorus a stark contrast to the chemical stillness around her. The doctors' promise from last week still warmed her cheeks: two more months. Just two more months until park picnics and bedtime stories and...\n\n\"Mom?\" Her voice cracked the silence. No flutter of eyelids answered. A cold realization slithered up her spine as she stared at her mother's motionless chest. \"DOCTOR!\"\n\nRunning footsteps echoed down the corridor. White coats swarmed the room, their urgent whispers dissolving into mechanical beeps. When they pulled the white sheets upward, the fabric made that terrible rustling sound she'd remember forever.\n\nRunning footsteps echoed down the corridor. White coats swarmed the room, their urgent whispers dissolving into mechanical beeps. When they pulled the white sheets upward, the fabric made that terrible rustling sound she'd remember forever.\n\nNight found her wandering familiar streets made alien by loss. Their apartment door creaked its usual greeting to an emptiness that swallowed sound. She collapsed onto the family bed still smelling of antiseptic and Dad's aftershave. The tears came then - great heaving sobs that shook her small frame until exhaustion claimed her.",
            },
            {
                "Chapter 1",
                "A girl in black is sitting in a cafe, let in hours before business, slouching in the quiet. The stream from her cup rises and fogs the glass beside her. A cold morning ---- she thinks.\n" +
                        "Hoshi watches her own breath dance with coffee steam. The barista's mercy still tastes bitter -- orphan's privilege, letting her linger before dawn. Outside, snowflakes spin like the gears of her mother's pocketwatch, the one she still winds daily despite cracked glass.\n" +
                        "Her gloved finger traces an ancient stain on the café's counter. Eight years since the accident, yet the school gates still coiled steel serpents in daylight. She counts her footsteps ascending the stairs (seventeen, always seventeen), boots leaving fossil imprints in virgin snow.\n\"Attention.\"\nChalk screeched. At the lectern stood winter's bride.\n" +
                        "Inhori's hair was the white of funeral silk untouched by flames. Her fingers, curled around the chalkboard's edge, glowed like milk poured into moonlight. Hoshi's mantra (\"Alone is safe\") crystallized when the transfer student turned -- those eyes.\n" +
                        "Not ice, but the pale fire of streetlamps devouring snowflakes.\n" +
                        "\"Nice to meet you,\" Inhori said, tilting her head as if examining a preserved butterfly. Her voice held the weight of snow-laden pines."
            },
            {
                "Chapter 2",
                "?????????????????"
            },
            {
                "Chapter 3",
                "?????????????????"
            },
            {
                "Chapter 4",
                "?????????????????"
            }
    };

    // 每个章节对应的BGM
    private String[] bgmList = {
            "rain_city.mp3",
            "alone.mp3",
            "leader.mp3",
            "waterless.mp3",
            "Innocent Snow.mp3"
    };

    // 每个BGM对应的曲师名
    private String[] bgmComposers = {
            "CsLrisEto",
            "CsLrisEto",
            "CsLrisEto",
            "CsLrisEto",
            "Yoko-cx"
    };

    // 每个BGM对应的中文显示名
    private String[] bgmDisplayNames = {
            "雨之城",
            "孤独",
            "领主",
            "水没",
            "Innocent Snow"
    };

    private int currentBgmIndex = 0;
    private int currentChapterIndex = 0;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_hidden);
            
            // 设置自动横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            
            initMediaPlayer();
            initViews();
            setupListeners();
            updateNovelContent(0);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果初始化失败，直接返回
            finish();
        }
    }

    private void initViews() {
        try {
            btnBack = findViewById(R.id.btn_back);
            chapterScroll = findViewById(R.id.chapter_scroll);
            chapterSelector = findViewById(R.id.chapter_selector);
            chapter1 = findViewById(R.id.chapter_1);
            chapter2 = findViewById(R.id.chapter_2);
            chapter3 = findViewById(R.id.chapter_3);
            chapter4 = findViewById(R.id.chapter_4);
            chapter5 = findViewById(R.id.chapter_5);
            tvNovelTitle = findViewById(R.id.tv_novel_title);
            tvNovelText = findViewById(R.id.tv_novel_text);
            btnPlay = findViewById(R.id.btn_play);
            tvBgmTitle = findViewById(R.id.tv_bgm_title);
            tvMessage = findViewById(R.id.tv_message);
            btnNotice = findViewById(R.id.btn_notice);
            
            // 初始化注意事项弹窗
            initNoticeDialog();
            
            // 初始化BGM显示
            updateBgmDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initNoticeDialog() {
        try {
            noticeAlertDialog = new Dialog(this);
            noticeAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            noticeAlertDialog.setContentView(R.layout.dialog_notice);
            noticeAlertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            noticeAlertDialog.getWindow().setLayout(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            noticeAlertDialog.getWindow().setDimAmount(0.7f);
            
            Button btnClose = noticeAlertDialog.findViewById(R.id.btn_close_notice);
            btnClose.setOnClickListener(v -> hideNoticeDialog());
            
            noticeAlertDialog.setOnShowListener(dialogInterface -> {
                noticeAlertDialog.findViewById(R.id.notice_dialog_root).setAlpha(0f);
                noticeAlertDialog.findViewById(R.id.notice_dialog_root).animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .start();
            });

            noticeAlertDialog.setOnCancelListener(dialogInterface -> hideNoticeDialog());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "弹窗初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showNoticeDialog() {
        try {
            if (noticeAlertDialog != null) {
                noticeAlertDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideNoticeDialog() {
        try {
            if (noticeAlertDialog != null && noticeAlertDialog.isShowing()) {
                noticeAlertDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 音乐播放完成后自动重新播放
                mp.start();
                isPlaying = true;
                updateBgmDisplay();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void skipAnimation() {
        try {
            // 停止消息序列显示
            messageIndex = messages.length;
            
            // 立即显示阅读器界面
            showReaderInterface();
            
            isAnimating = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playBgm(int chapterIndex) {
        try {
            if (mediaPlayer == null) {
                initMediaPlayer();
            }
            
            // 如果当前音乐正在播放，先停止并重置
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            } else if (currentBgmIndex == chapterIndex && !isPlaying && mediaPlayer.getDuration() > 0) {
                // 如果是同一首音乐且处于暂停状态，且已经准备好数据，直接继续播放
                mediaPlayer.start();
                isPlaying = true;
                updateBgmDisplay();
                return;
            } else {
                // 否则重置播放器准备新音乐
                mediaPlayer.reset();
            }
            
            String musicFile = bgmList[chapterIndex];
            AssetFileDescriptor afd = getAssets().openFd(musicFile);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            updateBgmDisplay();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "音乐播放失败了喵TAT: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "播放出错了喵～", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseBgm() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                updateBgmDisplay();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            // 返回按钮
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            // 注意事项按钮
            if (btnNotice != null) {
                btnNotice.setOnClickListener(v -> {
                    showNoticeDialog();
                });
            }

            // 章节选择
            if (chapter1 != null) chapter1.setOnClickListener(v -> selectChapter(0));
            if (chapter2 != null) chapter2.setOnClickListener(v -> selectChapter(1));
            if (chapter3 != null) chapter3.setOnClickListener(v -> selectChapter(2));
            if (chapter4 != null) chapter4.setOnClickListener(v -> selectChapter(3));
            if (chapter5 != null) chapter5.setOnClickListener(v -> selectChapter(4));

            // BGM播放按钮
            if (btnPlay != null) {
                btnPlay.setOnClickListener(v -> toggleBgm());
            }

            // 点击屏幕跳过动画
            findViewById(android.R.id.content).setOnClickListener(v -> {
                if (isAnimating) {
                    skipAnimation();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startFadeInAnimation() {
        try {
            // 开始显示消息序列
            showNextMessage();
        } catch (Exception e) {
            // 动画启动失败，不影响其他功能
            e.printStackTrace();
        }
    }

    private void showNextMessage() {
        if (messageIndex < messages.length) {
            // 显示当前消息
            String message = messages[messageIndex];
            if (tvMessage != null) {
                tvMessage.setText(message);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setAlpha(0.0f);
                
                // 淡入动画
                tvMessage.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .withEndAction(() -> {
                        // 显示2秒后淡出
                        handler.postDelayed(() -> {
                            // 淡出动画
                            tvMessage.animate()
                                .alpha(0.0f)
                                .setDuration(1000)
                                .withEndAction(() -> {
                                    messageIndex++;
                                    // 显示下一条消息
                                    showNextMessage();
                                })
                                .start();
                        }, 2000);
                    })
                    .start();
            }
        } else {
            // 消息序列显示完成，显示阅读器主界面
            showReaderInterface();
        }
    }

    private void showReaderInterface() {
        try {
            // 确保所有视图可见
            View chapterScroll = findViewById(R.id.chapter_scroll);
            View novelContent = findViewById(R.id.novel_content);
            View bgmPlayer = findViewById(R.id.bgm_player);
            Button btnBack = findViewById(R.id.btn_back);
            Button btnNotice = findViewById(R.id.btn_notice);
            
            // 确保视图可见
            if (chapterScroll != null) chapterScroll.setVisibility(View.VISIBLE);
            if (novelContent != null) novelContent.setVisibility(View.VISIBLE);
            if (bgmPlayer != null) bgmPlayer.setVisibility(View.VISIBLE);
            if (btnBack != null) btnBack.setVisibility(View.VISIBLE);
            if (btnNotice != null) btnNotice.setVisibility(View.VISIBLE);
            
            // 隐藏消息视图
            if (tvMessage != null) tvMessage.setVisibility(View.GONE);
            
            // 初始设置为透明
            if (chapterScroll != null) chapterScroll.setAlpha(0.0f);
            if (novelContent != null) novelContent.setAlpha(0.0f);
            if (bgmPlayer != null) bgmPlayer.setAlpha(0.0f);
            if (btnBack != null) btnBack.setAlpha(0.0f);
            if (btnNotice != null) btnNotice.setAlpha(0.0f);
            
            // 使用ViewPropertyAnimator实现淡入动画
            if (chapterScroll != null) {
                chapterScroll.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
            }
            if (novelContent != null) {
                novelContent.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
            }
            if (bgmPlayer != null) {
                bgmPlayer.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
            }
            if (btnBack != null) {
                btnBack.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
            }
            if (btnNotice != null) {
                btnNotice.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .withEndAction(() -> {
                        isAnimating = false;
                    })
                    .start();
            }
        } catch (Exception e) {
            // 动画启动失败，不影响其他功能
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在onResume中启动动画，确保Activity完全可见
        startFadeInAnimation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 在onStart中自动播放音乐
        handler.postDelayed(() -> {
            if (!isPlaying && mediaPlayer != null) {
                playBgm(currentBgmIndex);
            }
        }, 500);
    }

    private void selectChapter(int chapterIndex) {
        currentChapterIndex = chapterIndex;
        currentBgmIndex = chapterIndex;
        updateNovelContent(chapterIndex);
        playBgm(chapterIndex);
    }

    private void updateNovelContent(int chapterIndex) {
        try {
            if (chapterIndex >= 0 && chapterIndex < novelContents.length) {
                if (tvNovelTitle != null) {
                    tvNovelTitle.setText(novelContents[chapterIndex][0]);
                }
                if (tvNovelText != null) {
                    tvNovelText.setText(novelContents[chapterIndex][1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleBgm() {
        try {
            if (isPlaying) {
                pauseBgm();
            } else {
                playBgm(currentBgmIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBgmDisplay() {
        try {
            if (tvBgmTitle != null) {
                String musicName = bgmDisplayNames[currentBgmIndex];
                String composer = bgmComposers[currentBgmIndex];
                if (isPlaying) {
                    tvBgmTitle.setText("正在播放: " + musicName + " - " + composer);
                } else {
                    tvBgmTitle.setText("已暂停: " + musicName + " - " + composer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 修改小说内容的方法
    public void updateNovelContent(int chapterIndex, int contentIndex, String newTitle, String newContent) {
        try {
            if (chapterIndex >= 0 && chapterIndex < novelContents.length) {
                novelContents[chapterIndex][0] = newTitle;
                novelContents[chapterIndex][1] = newContent;
                if (chapterIndex == currentChapterIndex) {
                    updateNovelContent(chapterIndex);
                }
                Toast.makeText(this, "内容更新成功喵～", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "更新失败了喵QAQ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 获取小说内容的方法
    public String[] getNovelContent(int chapterIndex) {
        try {
            if (chapterIndex >= 0 && chapterIndex < novelContents.length) {
                return novelContents[chapterIndex];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


