package com.example.counter;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView countTextView;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        countTextView = findViewById(R.id.countTextView);

        // 设置初始文本大小为屏幕高度的 2/3
        setInitialTextSize();

        View rootLayout = findViewById(R.id.rootLayout);
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                countTextView.setText(String.valueOf(count));
                adjustTextSize(); // 调整文本大小
            }
        });
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
}