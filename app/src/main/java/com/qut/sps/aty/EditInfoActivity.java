package com.qut.sps.aty;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.Integer.parseInt;

public class EditInfoActivity extends AppCompatActivity {

    private List<UsersInfo> userInfoList;
    private EditText editNickName;
    private RadioButton M;
    private RadioButton W;
    private EditText editAge;
    private EditText editTel;
    private TextView address;
    private TextView prof;

    private Button submit;
    private Button back;

    private CircleImageView userIcon;
    private LinearLayout aboutHead,address1,prof1;
    private String sex,mySex;
    private String nickName,myNickName;
    private String age,myAge;
    private String tel,myTel;
    private String place,myPlace;
    private String profess,myProfess;
    private String myIconUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        initView();
        init();
    }

    private void initView() {
        editNickName = (EditText) findViewById(R.id.nick_name);
        M = (RadioButton) findViewById(R.id.M);
        W = (RadioButton) findViewById(R.id.W);
        editAge = (EditText) findViewById(R.id.age);
        editTel = (EditText) findViewById(R.id.tel);
        address = (TextView) findViewById(R.id.address);
        prof = (TextView) findViewById(R.id.prof);

        submit = (Button) findViewById(R.id.title_right_btn);
        submit.setVisibility(View.VISIBLE);
        submit.setText("提交");

        ImageView imageView = (ImageView) findViewById(R.id.title_right_img);
        imageView.setVisibility(View.GONE);

        back=(Button) findViewById(R.id.title_back);
        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("编辑我的资料");

        aboutHead=(LinearLayout)findViewById(R.id.about_head);
        userIcon=(CircleImageView)findViewById(R.id.user_icon);
        address1=(LinearLayout)findViewById(R.id.address1);
        prof1=(LinearLayout)findViewById(R.id.prof1);
    }

    private void init() {


        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (EditInfoActivity.this).finish();

            }
        });

        address1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(EditInfoActivity.this,AddressActivity.class);
                startActivityForResult(intent,2);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToServiceAndLocal();

            }
        });

        prof1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(EditInfoActivity.this, ChooseProfessionActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        aboutHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(EditInfoActivity.this,UserIconActivity.class);
                startActivity(intent);
            }
        });

        queryAndShowUserPreviousInfo();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String returnData = data.getStringExtra("data_return");
                    prof.setText(returnData);
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    String returnData = data.getStringExtra("data_return");
                    address.setText(returnData);
                }
                break;

        }
    }

    /**
     * 查找并显示用户原来的信息
     */
    private void queryAndShowUserPreviousInfo() {
        userInfoList  = DataSupport.where("account=?", MainActivity.userAccount).find(UsersInfo.class);
        myPlace=null;
        if(userInfoList!=null){
            for(UsersInfo usersInfo:userInfoList) {

                myNickName=usersInfo.getNickName();
                mySex=usersInfo.getSex();
                myAge=String.valueOf(usersInfo.getAge());
                myTel=usersInfo.getTel();
                myIconUrl=usersInfo.getIconUrl();
                myProfess=usersInfo.getProfession();
                myPlace = usersInfo.getAddress();

                showInitIcon();
                showAnotherInitInfo();
            }
        }else{
            queryFromServer();
        }
    }

    /**
     * 显示原来的头像
     */
    private void showInitIcon() {
        Glide.with(this).load(HttpUtil.SPS_SOURCE_URL+myIconUrl).into(userIcon);
    }

    /**
     * 正确显示原来的信息
     */
    private void showAnotherInitInfo() {

        if(!myNickName.equals("null")){
            editNickName.setText(myNickName);
        }else {
            editNickName.setText("");
        }
        if(mySex.equals("女")){
            W.setChecked(true);
        }else if(mySex.equals("男")){
            M.setChecked(true);
        }else{
            W.setChecked(false);
            M.setChecked(false);

        }
        if(!myAge.equals("null")){
            editAge.setText(myAge);
        }else {
            editAge.setText("");
        }
        if(!myTel.equals("null")){
            editTel.setText(myTel);
        }else {
            editTel.setText("");
        }
        if(!myPlace.equals("null")){
            address.setText(myPlace);
        }else {
            address.setText("");
        }
        if(!myProfess.equals("null")){
            prof.setText(myProfess);
        }else {
            prof.setText("");
        }
    }
    /**
     * 从服务器查询信息
     */
    private void queryFromServer() {
        String url1 = Constant.SPS_URL + "GetUsersInforServlet";
        HttpUtil.sendOkHttpRequest(url1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                 Toast.makeText(EditInfoActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result;
                try {
                    result = Utility.handleUsersInfor(responseText);//进行json数据解析
                    if (result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                queryAndShowUserPreviousInfo();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新至服务器和本地数据库
     */
    public void saveToServiceAndLocal() {

        getInfoOfUser();

            if(nickName.length() < 20 && (tel.length() == 11 ||tel.length()==0)){
                saveToTwoPlace();
            }
            if(nickName.length()>20){

                popWarningAboutNickName();

            }
            if(tel.length()!=11 && tel.length() !=0){
                editTel.setError("电话号码不规范");
            }
    }

    /**
     * 得到用户编辑的信息
     */
    private void getInfoOfUser() {

        sex = null;
        nickName = editNickName.getText().toString().trim();
        age = editAge.getText().toString().trim();
        if(age.equals("")){
            editAge.setText("0");
        }
        age=editAge.getText().toString().trim();

        tel = editTel.getText().toString().trim();
        place=address.getText().toString().trim();
        profess = prof.getText().toString().trim();

        if (M.isChecked()) {

            sex = M.getText().toString().trim();
        } else if (W.isChecked()) {

            sex = W.getText().toString().trim();
        }else{

            sex="";
        }
    }

    /**
     * 保存到服务器
     */
    private void saveToTwoPlace() {
        RequestBody requestBody = new FormBody.Builder()
                .add("nickName", nickName)
                .add("sex", sex)
                .add("age", age)
                .add("tel", tel)
                .add("address",place)
                .add("profession", profess)
                .add("id", MainActivity.userId)
                .build();
        String url = Constant.SPS_URL + "ChangeUsersInfoServlet";
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                req();
            }

            private void req() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditInfoActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                handMessage(responseText);//进行json数据解析
            }

            private void handMessage(String responseText) {
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject object = new JSONObject(responseText);
                        String result = object.getString("result");
                        saveToLocal();
                        showResponse(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            private void showResponse(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditInfoActivity.this, response, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }
    /**
     * 保存到本地数据库
     */
    private void saveToLocal() {
        UsersInfo userData=new UsersInfo();
        userData.setNickName(nickName);
        userData.setSex(sex);
        userData.setAge(parseInt(age));
        userData.setTel(tel);
        userData.setAddress(place);
        userData.setProfession(profess);
        userData.updateAll("account=?",MainActivity.userAccount);
    }

    /**
     * 昵称不规范时弹出提示框
     */
    private void popWarningAboutNickName() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(EditInfoActivity.this);
        dialog.setTitle("注意咯！！！");
        dialog.setIcon(R.drawable.img6);
        dialog.setMessage("昵称长度超过20个字符");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        userInfoList  = DataSupport.where("id=?", MainActivity.userId).find(UsersInfo.class);
        myPlace=null;
        if(userInfoList!=null) {
            for (UsersInfo usersInfo : userInfoList) {
                myIconUrl = usersInfo.getIconUrl();
                showInitIcon();
            }
        }

    }

}
