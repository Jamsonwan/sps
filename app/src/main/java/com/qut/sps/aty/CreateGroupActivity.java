package com.qut.sps.aty;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.server.CreateGroupTask;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateGroupActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private static final int CODE_RESULT_REQUEST=3;
    private Uri imageUri;
    private File outputImage;
    private File saveFile;

    private Map<String,String> groupInfo;
    private CircleImageView circleImageView;
    private EditText editText;
    private Button button;

    private EMGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initView();
        initEvent();
    }

    private void initView() {
        circleImageView = (CircleImageView)findViewById(R.id.add_group_icon);
        editText = (EditText)findViewById(R.id.write_group_name);
        button = (Button)findViewById(R.id.submit_group);

        Button titleBack = (Button) findViewById(R.id.title_back);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("创建新团队");

        ImageView titleImage = (ImageView) findViewById(R.id.title_right_img);
        titleImage.setVisibility(View.GONE);
    }

    private void initEvent() {
        circleImageViewEvent();
        editTextEvent();
        buttonEvent();
    }


    private void circleImageViewEvent() {
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroupActivity.this);
                builder.setTitle("上传头像");
                builder.setItems(new String [] {"拍照上传","从相册选择"},
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                                case 0:
                                    if (ContextCompat.checkSelfPermission(CreateGroupActivity.this,
                                            Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                                        ActivityCompat.requestPermissions(CreateGroupActivity.this,
                                                new String[]{Manifest.permission.CAMERA},0);
                                    }else {
                                        e_TakePhoto();
                                    }
                                    break;
                                case 1:
                                    if (ContextCompat.checkSelfPermission(CreateGroupActivity.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                                        ActivityCompat.requestPermissions(CreateGroupActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                                    }else {
                                        e_OpenAlbum();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                builder.create().show();
            }
        });

    }

    private void e_TakePhoto(){//getExternalCacheDir()
        //将拍摄的照片显示出来
        outputImage = new File(getExternalCacheDir(),System.currentTimeMillis()+".jpg");
        try{
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT>=24){
            imageUri = FileProvider.getUriForFile(CreateGroupActivity.this,"com.qut.sps.fileprovider",outputImage);
        }else{
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    private void e_OpenAlbum(){
        Intent pickIntent = new Intent(Intent.ACTION_PICK,null);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent,CHOOSE_PHOTO);
    }

    private void buttonEvent() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(circleImageView.getDrawable().getCurrent().getConstantState().equals(getResources().getDrawable(R.drawable.dashed).getConstantState())||editText.length()<=0){

                    Toast.makeText(CreateGroupActivity.this,"请填写完整",Toast.LENGTH_SHORT).show();
                }else{
                    //判断是否重名
                    String url = HttpUtil.SPS_URL+"SearchGroupServlet";
                    RequestBody requestBody = new FormBody.Builder()
                            .add("groupName",editText.getText().toString())
                            .build();
                    HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CreateGroupActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String responseText = response.body().string();
                            if(!TextUtils.isEmpty(responseText)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(CreateGroupActivity.this,"该团队已存在,请重新命名!",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else {
                                //不存在该团队时，可以创建！
                                EMCreateGroup();
                            }
                        }
                    });
                }
            }
        });
    }

    private void editTextEvent() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()<1){
                    editText.setError("团名不得为空!");
                }else if(editable.length()>10){
                    editText.setError("不得超过十个字");
                }else if(!circleImageView.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.dashed).getConstantState())){
                    button.setBackgroundColor(Color.parseColor("#66BCFF"));
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK){
                      startPhotoZoom(imageUri,TAKE_PHOTO);
                }
                break;
           case CHOOSE_PHOTO:
                if (resultCode==RESULT_OK){
                   // imageUri = data.getData();
                    startPhotoZoom(data.getData(),CHOOSE_PHOTO);
                }
                break;
            case CODE_RESULT_REQUEST:
                if (data != null) {
                 setPicToView(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void startPhotoZoom(Uri uri,int flag) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        if(flag==CHOOSE_PHOTO){//从相册
            saveFile = new File(getExternalCacheDir(),System.currentTimeMillis()+".jpg");
        }else{
            saveFile = new File(outputImage.getPath());
        }
        intent.putExtra("output", Uri.fromFile(saveFile)); // 输出的图片路径
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); // 输出的图片格式
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);//取消人脸识别功能
        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            if (photo != null){
                Drawable drawable = new BitmapDrawable(getResources(), photo);
                circleImageView.setImageDrawable(drawable);
            }
       }
       //保证先写团队名，后上传头像按钮也能变色
        if(!circleImageView.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.dashed).getConstantState())
                && editText.length() > 0){
            button.setBackgroundColor(Color.parseColor("#66BCFF"));
        }
    }

    private void EMCreateGroup(){

        final String st1 = "创建团队失败!";
        final String groupName = editText.getText().toString().trim();
        SimpleDateFormat formatter =  new SimpleDateFormat("yyyy年MM月dd日");
        Date curDate =   new  Date(System.currentTimeMillis());//获取当前时间
        final String    str   =  "本团队创建于"+formatter.format(curDate);
        final String[] members = new String[0];
        final String reason =EMClient.getInstance().getCurrentUser() + "Invite to join the group"+ groupName;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMGroupOptions option = new EMGroupOptions();
                    option.maxUsers = 200;
                    option.inviteNeedConfirm = true;
                    option.style = EMGroupStyle.EMGroupStylePublicJoinNeedApproval;

                    group=EMClient.getInstance().groupManager().createGroup(groupName,str, members, reason, option);
                    //开始提交

                    CreateGroupTask task = new CreateGroupTask();
                    groupInfo = new HashMap<>();
                    groupInfo.put("imagePath",saveFile.getPath());
                    groupInfo.put("groupName",group.getGroupName());
                    groupInfo.put("description",group.getDescription());
                    groupInfo.put("userId", MainActivity.userId);
                    groupInfo.put("EMGroupId",group.getGroupId());
                    task.execute(groupInfo);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getContext(),"创建群成功！",Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                } catch (final HyphenateException e) {
                    Log.e("Error",e.getMessage());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(CreateGroupActivity.this, st1 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 0:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    e_TakePhoto();
                }else{
                    Toast.makeText(CreateGroupActivity.this,"你禁止了访问相机",Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    e_OpenAlbum();
                }else{
                    Toast.makeText(CreateGroupActivity.this,"你禁止了访问相册",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
