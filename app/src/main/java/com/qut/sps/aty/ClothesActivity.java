package com.qut.sps.aty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.util.HttpUtil;


public class ClothesActivity extends AppCompatActivity {

    public static final String NAME = "name";
    public static final String TEL = "tel";
    public static final String DESCRIPTION = "description";
    public static final String URL = "url";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothes);

        Intent intent = getIntent();
        String clothesUrl = intent.getStringExtra(URL);
        String clothesName = intent.getStringExtra(NAME);
        String clothesTel = intent.getStringExtra(TEL);
        String clothes_description = intent.getStringExtra(DESCRIPTION);

        ImageView clothes = (ImageView) findViewById(R.id.clothes_imageView);
        TextView imageContext = (TextView) findViewById(R.id.clothes_description);
        TextView iamgePhone = (TextView) findViewById(R.id.clothes_tel);
        TextView imageName = (TextView) findViewById(R.id.cloth_real_name);

        initTitleView();

        Glide.with(this).load(HttpUtil.SPS_SOURCE_URL+clothesUrl).into(clothes);
        imageName.setText(clothesName);
        iamgePhone.setText(clothesTel);
        imageContext.setText(clothes_description);

    }

    private void initTitleView() {
        Button titleBack = (Button) findViewById(R.id.title_back);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageView titleImage = (ImageView) findViewById(R.id.title_right_img);
        titleImage.setVisibility(View.GONE);

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("商品详情");
    }

}
