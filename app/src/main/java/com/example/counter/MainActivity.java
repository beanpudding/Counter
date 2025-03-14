package com.example.counter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView countTextView;
    private int count = 0;
    private static final String PREFS_NAME = "ClickCounterPrefs";
    private static final String KEY_COUNT = "count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        countTextView = findViewById(R.id.countTextView);

        // 设置初始文本大小为屏幕高度的 2/3
        setInitialTextSize();
        // 从 SharedPreferences 恢复计数
        restoreCount();

        View rootLayout = findViewById(R.id.rootLayout);
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementCount();
            }
        });

        rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认清零")
                        .setMessage("是否将计数清零？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetCount();
                                Toast.makeText(MainActivity.this, "计数已清零", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
                return true; // 返回 true 表示已处理长按事件
            }
        });
    }

    // 监听音量加键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            incrementCount(); // 按下音量加键时增加计数
            return true; // 返回 true 表示已处理按键事件
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            decrementCount(); // 按下音量减键时减小计数
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
    private void incrementCount() {
        setCount(count + 1);
    }

    // 减小计数
    private void decrementCount() {
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
}