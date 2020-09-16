package com.qut.sps.aty;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.R;
import com.qut.sps.util.Constant;
import com.qut.sps.util.MyApplication;

public class SettingActivity extends AppCompatActivity {

    private LinearLayout changePwd;
    private LinearLayout changeInfo;
    private LinearLayout path;
    private Switch aSwitch;
    private Button back;
    public static boolean InternerOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();

        init();
    }

    private void initView() {
        changePwd = (LinearLayout) findViewById(R.id.change_pwd);
        changeInfo = (LinearLayout) findViewById(R.id.change_info);
        path = (LinearLayout) findViewById(R.id.path);
        aSwitch = (Switch) findViewById(R.id.net_choose);
        back = (Button) findViewById(R.id.title_back);
        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("设置");
        ImageView titleImage = (ImageView) findViewById(R.id.title_right_img);
        titleImage.setVisibility(View.GONE);
    }

    private void init() {

        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        if(InternerOk){
            aSwitch.setChecked(true);
            preferences.putBoolean(Constant.INTERNET_OK,true);
        }else{
            aSwitch.setChecked(false);
            preferences.putBoolean(Constant.INTERNET_OK,false);
        }
        preferences.apply();

        aSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("smartapplocker", MODE_PRIVATE).edit();
                editor.putBoolean("status", aSwitch.isChecked());
                editor.commit();

                SharedPreferences prefs = getSharedPreferences("smartapplocker", MODE_PRIVATE);
                boolean switchState = prefs.getBoolean("status", false);

                if (switchState) {
                    InternerOk = true;
                    Toast.makeText(SettingActivity.this, "非wifi环境下，请注意流量", Toast.LENGTH_SHORT).show();
                } else {
                    InternerOk = false;
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (SettingActivity.this).finish();
            }
        });

        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        changeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, EditInfoActivity.class);
                startActivity(intent);
            }
        });
        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popPathDialog();
            }
        });
    }

    /**
     * 弹出对话框
     */
    private void popPathDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
        dialog.setTitle("缓存路径");
        long a = longGetFreeBytes() / (1024 * 1024 * 1024);
        dialog.setIcon(R.drawable.img5);
        dialog.setMessage("存储卡：" + Environment.getExternalStorageDirectory().getPath() + "\n存储空间：" + a + "G");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();

    }

    /**
     * 获得存储空间的大小
     *
     * @return
     */
    public long longGetFreeBytes() {
        String filePath = Environment.getExternalStorageDirectory().getPath();
        StatFs stat = new StatFs(filePath);
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }
}
