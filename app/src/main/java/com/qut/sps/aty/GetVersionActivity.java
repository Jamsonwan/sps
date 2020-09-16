package com.qut.sps.aty;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.R;

public class GetVersionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_version);

        Button back=(Button) findViewById(R.id.title_back);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (GetVersionActivity.this).finish();
            }
        });

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("检测更新");

        ImageView titleImg = (ImageView) findViewById(R.id.title_right_img);
        titleImg.setVisibility(View.GONE);

        Button updateVersion=(Button) findViewById(R.id.update);
        TextView exp=(TextView)findViewById(R.id.explain);
        updateVersion.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(GetVersionActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
            }
        });

        exp.setText("版权所有sps保留一切权利");

    }
}
