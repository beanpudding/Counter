package com.example.counter;

import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView versionTextView;
    private int clickCount = 0;
    private long firstClickTime = 0;
    private int versionCode;
    private String versionName;
    private TextView countTextView;
    private LinearLayout floatingButtonsLayout;
    private int count = 0;
    private static final String PREFS_NAME = "ClickCounterPrefs";
    private static final String KEY_COUNT = "count";

    private Handler handler = new Handler(); // 用于延时操作
    private Runnable hideButtonsRunnable = new Runnable() {
        @Override
        public void run() {
            hideFloatingButtons(); // 5 秒后隐藏悬浮按钮
        }
    };

    private int getVersionCode() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1; // 返回 -1 表示获取失败
        }
    }
    private String getVersionName() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 获取版本名称和版本代码
        versionName = getVersionName();
        versionCode = getVersionCode();

        // 设置版本名称
        versionTextView = findViewById(R.id.versionTextView);
        versionTextView.setText("v" + versionName);
        versionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = SystemClock.elapsedRealtime();

                if (clickCount == 0) {
                    firstClickTime = currentTime;
                } else if (currentTime - firstClickTime > 3000) {
                    clickCount = 0;
                    firstClickTime = currentTime;
                }

                clickCount++;

                if (clickCount >= 10) {
                    versionTextView.setText("Version Code: " + versionCode);
                    clickCount = 0;
                }
            }
        });

        countTextView = findViewById(R.id.countTextView);
        floatingButtonsLayout = findViewById(R.id.floatingButtonsLayout);

        // 设置初始文本大小为屏幕高度的 2/3
        setInitialTextSize();
        // 从 SharedPreferences 恢复计数
        restoreCount();

        View rootLayout = findViewById(R.id.rootLayout);
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingButtonsLayout.getVisibility() == View.VISIBLE) {
                    hideFloatingButtons();
                }
                else {
                    increaseCount();
                }
            }
        });

        rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (floatingButtonsLayout.getVisibility() != View.VISIBLE) {
                    showFloatingButtons();
                }
                else {
                    handler.removeCallbacks(hideButtonsRunnable); // 移除之前的任务
                    handler.postDelayed(hideButtonsRunnable, 5000); // 重新启动 5 秒计时
                }
                return true; // 返回 true 表示已处理长按事件
            }
        });

        // 设置悬浮按钮的点击事件
        Button buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(hideButtonsRunnable); // 移除之前的任务
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.confirm_zeroing_alertDialog_title))
                        .setMessage(getString(R.string.confirm_zeroing_alertDialog_message))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetCount();
                                hideFloatingButtons();
                                Toast.makeText(MainActivity.this, getString(R.string.toast_count_cleared_to_zero), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.postDelayed(hideButtonsRunnable, 5000); // 重新启动 5 秒计时
                            }
                        })
                        .show();
            }
        });

        Button buttonDecrease = findViewById(R.id.buttonDecrease);
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(hideButtonsRunnable); // 移除之前的任务
                decreaseCount();
                handler.postDelayed(hideButtonsRunnable, 5000); // 重新启动 5 秒计时
            }
        });
    }

    // 监听音量加键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            increaseCount(); // 按下音量加键时增加计数
            return true; // 返回 true 表示已处理按键事件
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            decreaseCount(); // 按下音量减键时减小计数
            return true; // 返回 true 表示已处理按键事件
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count", count);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        count = savedInstanceState.getInt("count");
        countTextView.setText(String.valueOf(count));
    }

    // 设置初始文本大小为屏幕高度的 2/3
    private void setInitialTextSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        float textSizeInPx = (float) (screenHeight * 0.66);
        float textSizeInSp = textSizeInPx / getResources().getDisplayMetrics().scaledDensity;
        countTextView.setTextSize(textSizeInSp);
    }

    // 动态调整文本大小，确保数字完整显示
    private void adjustTextSize() {
        // 获取屏幕宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // 获取当前文本的宽度
        Paint paint = new Paint();
        float textSize = countTextView.getTextSize();
        paint.setTextSize(textSize);
        String text = countTextView.getText().toString();
        float textWidth = paint.measureText(text);

        // 如果文本宽度超过屏幕宽度，逐步减小文本大小
        while (textWidth > screenWidth-100) {
            textSize -= 10; // 每次减小 2px
            countTextView.setTextSize(textSize / getResources().getDisplayMetrics().scaledDensity);
            paint.setTextSize(textSize);
            textWidth = paint.measureText(text);
        }
//        Log.d("TextWidth", "Final text width: " + textWidth + " px");
    }

    private void setCount(int cnt) {
        count = cnt;
        countTextView.setText(String.valueOf(cnt));
        adjustTextSize(); // 调整文本大小
        saveCount(); // 保存计数
    }

    // 增加计数
    private void increaseCount() {
        setCount(count + 1);
    }

    // 减小计数
    private void decreaseCount() {
        if (count > 0) {
            setInitialTextSize();
            setCount(count - 1);
        }
    }

    // 清零计数
    private void resetCount() {
        setCount(0);
    }

    // 保存计数到 SharedPreferences
    private void saveCount() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_COUNT, count);
        editor.apply(); // 异步保存
    }

    // 从 SharedPreferences 恢复计数
    private void restoreCount() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setCount(preferences.getInt(KEY_COUNT, 0)); // 如果不存在，默认值为 0
        countTextView.setText(String.valueOf(count));
    }

    private void showFloatingButtons() {
        clickCount = 0;
        versionTextView.setText("v" + versionName);
        versionTextView.setVisibility(View.VISIBLE);
        floatingButtonsLayout.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        floatingButtonsLayout.startAnimation(slideIn);
        // 延迟 5 秒后隐藏悬浮按钮
        handler.removeCallbacks(hideButtonsRunnable); // 移除之前的任务
        handler.postDelayed(hideButtonsRunnable, 5000); // 重新启动 5 秒计时
    }

    private void hideFloatingButtons() {
        versionTextView.setVisibility(View.GONE);
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                floatingButtonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        floatingButtonsLayout.startAnimation(slideOut);
        // 移除未执行的延时任务
        handler.removeCallbacks(hideButtonsRunnable);
    }
}