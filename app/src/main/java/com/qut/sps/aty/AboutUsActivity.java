package com.qut.sps.aty;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qut.sps.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustStatusBar();
        setContentView(R.layout.activity_about_us);

        ImageView back=(ImageView)findViewById(R.id.back);
        TextView description=(TextView)findViewById(R.id.description);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (AboutUsActivity.this).finish();
            }
        });
        description.setText("    集舞蹈，音乐，聊天于一体的软件，目前整合了多重风格的歌曲、舞蹈，" +
                "既有能满足老年人的广场舞视频，又有能满足青年人酷炫style的视频，" +
                "最重要的是能摒弃传统团体舞带来的噪声问题");
    }

    private void adjustStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
