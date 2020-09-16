package com.qut.sps.aty;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qut.sps.R;


public class AddFriendActivity extends AppCompatActivity {

    private LinearLayout y_byAccount;
    private LinearLayout y_byNickName;
    private ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_add_friend);

        initView();
        accountListener();
        nameListener();
        backListener();
    }
    private void backListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void nameListener() {
        y_byNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFriendActivity.this,SearchFriendActivity.class);
                intent.putExtra("data","昵称");
                startActivity(intent);
            }
        });
    }

    private void accountListener() {
        y_byAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFriendActivity.this,SearchFriendActivity.class);
                intent.putExtra("data","账号");
                startActivity(intent);
            }
        });
    }

    private void initView() {
        back = (ImageView)findViewById(R.id.y_back);
        y_byAccount=(LinearLayout)findViewById(R.id.y_findbyid);
        y_byNickName=(LinearLayout)findViewById(R.id.y_findbyname);
    }

}

