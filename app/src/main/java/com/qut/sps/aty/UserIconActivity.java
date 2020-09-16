package com.qut.sps.aty;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.util.NetTask;
import com.qut.sps.util.Utility;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.qut.sps.util.SaveIconUtil.ALBUM_PATH;

public class UserIconActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_ALBUM = 2;
    private static final int REQUESTCODE_CUTTING = 3;
    private CircleImageView icon;
    private PopupWindow pop;
    private LinearLayout ll_popup;
    private static Uri imageUri;
    private String imagePath;
    private File outputImage;
    private  File saveFile;
    private ImageView headBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_icon);

        init();
    }

    /**
     * 处理各种组件的具体监听事件
     */
    private void init() {
        Button back = (Button) findViewById(R.id.title_back);

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("头像修改");

        ImageView titleImg = (ImageView) findViewById(R.id.title_right_img);
        titleImg.setVisibility(View.GONE);

        icon = (CircleImageView) findViewById(R.id.icon);
        icon.setImageResource(R.drawable.icon);
        headBackground=(ImageView)findViewById(R.id.head_background);

        loadingUserIcon();

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow();
                ll_popup.startAnimation(AnimationUtils.loadAnimation(
                        UserIconActivity.this, R.anim.activity_translate_in));
                pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
        });

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (UserIconActivity.this).finish();
            }
        });
    }

    /**
     * 加载用户头像
     */
    private void loadingUserIcon() {
        List<UsersInfo> iconList= DataSupport.select("iconUrl").where("account=?",MainActivity.userAccount).find(UsersInfo.class);
        String myIcon=null;
        if(iconList.size()>0){
            for(UsersInfo usersInfo:iconList){
                myIcon=usersInfo.getIconUrl();
            }
            Glide.with(this).load(HttpUtil.SPS_SOURCE_URL+myIcon).into(icon);
        }
        else{
            queryFromService();
        }
    }

    /**
     * 从服务器查找信息
     */
    public void queryFromService() {

        String url = Constant.SPS_URL + "GetUsersInforServlet";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(UserIconActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
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
                                loadingUserIcon();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /****
     * 显示上传头像的选择方式
     */
    public void showPopupWindow() {
        pop = new PopupWindow(UserIconActivity.this);
        View view = getLayoutInflater().inflate(R.layout.item_popwindom,
                null);
        ll_popup =  view.findViewById(R.id.ll_popup);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);
        RelativeLayout parent =  view.findViewById(R.id.parent);
        Button bt1 =  view.findViewById(R.id.item_popupwindows_camera);
        Button bt2 =  view.findViewById(R.id.item_popupwindows_Photo);
        Button bt3 =  view.findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(UserIconActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(UserIconActivity.this,new String[]{Manifest.permission.CAMERA},1);
                }else{
                    takePhotoAndSavePath();
                }
                pop.dismiss();
                ll_popup.clearAnimation();

            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(UserIconActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UserIconActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 2);
                } else {
                     openAlbum();
                }
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
    }

    /**
     * 拍照并存入文件中
     */
    private void takePhotoAndSavePath() {
        outputImage = new File(getExternalCacheDir(), System.currentTimeMillis()+"");

        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            imageUri = FileProvider.getUriForFile(UserIconActivity.this, "com.qut.sps.fileprovider", outputImage);
        }
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(camera, TAKE_PHOTO);
    }

    /**
     * 打开相册
     */
    protected void openAlbum(){
        Intent intent=new Intent(Intent.ACTION_PICK,null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent,2);
    }

    /**
     *处理具体事件结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO && resultCode == Activity.RESULT_OK ) {

            String sdState = Environment.getExternalStorageState();

            if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
                return;
            }

            startPhotoZoom(imageUri,TAKE_PHOTO);

        }
        if (requestCode == CHOOSE_ALBUM && resultCode == Activity.RESULT_OK ) {


            if (Build.VERSION.SDK_INT >= 19) {

                handleImageOnKitKat(data);

            } else {

                handleImageBeforeKitKat(data);
            }
            startPhotoZoom(data.getData(),CHOOSE_ALBUM);
        }
        if(requestCode == REQUESTCODE_CUTTING){

           setPicToView(data);
        }
    }

    /**
     * 显示并上传图片
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = null;
        if (picdata != null){
            extras = picdata.getExtras();
        }
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            if (photo != null){
                Drawable drawable = new BitmapDrawable(getResources(), photo);
                icon.setImageDrawable(drawable);
                Bitmap2Bytes(photo);
                NetTask task = new NetTask();
                task.execute(saveFile.getPath());
                Toast.makeText(MyApplication.getContext(),"修改头像成功！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 压缩
     * @param bm
     * @return
     */
    public byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 剪切照片,并将剪切的照片存入指定地点
     * @param uri
     * @param flag
     */
    public void startPhotoZoom(Uri uri,int flag) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);

        if(flag==CHOOSE_ALBUM){//从相册
            File userIconFile=new File(ALBUM_PATH);

            if(!userIconFile.exists()){
                userIconFile.mkdirs();
            }
            saveFile = new File(ALBUM_PATH,System.currentTimeMillis()+".jpg");

        }else{
            saveFile = new File(outputImage.getPath());
        }
        intent.putExtra("output", Uri.fromFile(saveFile)); // 输出的图片路径
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); // 输出的图片格式
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);//取消人脸识别功能
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     *
     * 解析封装过的Url
     * @param data
     */
    private void handleImageOnKitKat(Intent data) {
        imageUri = data.getData();

        if (DocumentsContract.isDocumentUri(this, imageUri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(imageUri);
            if("com.android.providers.media.documents".equals(imageUri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(imageUri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(imageUri, null);
        } else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = imageUri.getPath();
        }

    }

    /**
     * 将url传入getImagePath()
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        imageUri = data.getData();
        imagePath = getImagePath(imageUri, null);

    }

    /**
     * 获取图片的真实路径
     * @param uri
     * @param selection
     * @return
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        switch(requestCode){
            case CHOOSE_ALBUM:
                if(grantResults.length >0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case TAKE_PHOTO:
                if(grantResults.length >0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePhotoAndSavePath();
                }else{
                    Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                }
        }

    }

}
