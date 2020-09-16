package com.qut.sps.aty;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.MainActivity;
import com.qut.sps.R;
import com.qut.sps.util.BaseActivity;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.DatePickerFragment;
import com.qut.sps.view.TimePickerFragment;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CompetitionCreatActivity extends BaseActivity implements TimePickerFragment.DataCallBack,View.OnClickListener{

    private TextView start;

    private TextView end;

    private ImageView image;

    private EditText ename ;
    private EditText eplace;
    private EditText etel;
    private EditText edescription;

    private String imagePath;

    // 1 代表开始时间  0 代表结束时间
    private static int choice = 1;

    public static final int PHOTO = 1;

    public static final int UCROP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_creat);
        init();
    }

    private void init(){

        Button backBtn = (Button) findViewById(R.id.title_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("创建比赛");

        ImageView titleImg = (ImageView) findViewById(R.id.title_right_img);
        titleImg.setVisibility(View.GONE);

        start= (TextView) findViewById(R.id.competition_creat_start);
        end = (TextView) findViewById(R.id.competition_create_end);
        image = (ImageView) findViewById(R.id.competition_creat_image);
        ename = (EditText) findViewById(R.id.competition_creat_name);
        eplace = (EditText) findViewById(R.id.competition_creat_place);
        etel = (EditText) findViewById(R.id.competition_creat_tel);
        edescription = (EditText) findViewById(R.id.competition_creat_description);
        Button submitButton = (Button) findViewById(R.id.competition_submit);
        start.setOnClickListener(this);
        end.setOnClickListener(this);
        image.setOnClickListener(this);
        submitButton.setOnClickListener(this);
    }

    private void setTime(){
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getFragmentManager(),"data_picker");
    }
    @Override
    public void getData(String data) {
        if (choice == 1){
            start.setText(data);
            Log.d("开始时间",start.getText().toString());
        }else {
            end.setText(data);
            Log.d("结束时间",end.getText().toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.competition_creat_image:
                selectImage();
                break;
            case R.id.competition_creat_start:
                choice = 1;
                setTime();
                break;
            case R.id.competition_create_end:
                choice = 0;
                setTime();
                break;
            case R.id.competition_submit:
                submit();

                break;

        }
    }

    private void startDialog(){
        new AlertDialog.Builder(CompetitionCreatActivity.this).setTitle("提示")
                .setMessage("您所选择时间已经过去，请重新选择")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choice = 1;
                        setTime();
                    }
                }).show();

    }

    private void endDialog(){
        new AlertDialog.Builder(CompetitionCreatActivity.this).setTitle("提示")
                .setMessage("比赛结束时间不能早于开始时间，请重新选择")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choice = 0;
                        setTime();
                    }
                }).show();

    }
    private void submit(){
        String name = ename.getText().toString().trim();
        String start_time = start.getText().toString().trim();
        String end_time = end.getText().toString().trim();
        String place = eplace.getText().toString().trim();
        String tel = etel.getText().toString().trim();
        String description = edescription.getText().toString().trim();
        if (imagePath != null) {
            if (!name.equals("")) {
                if (!start_time.equals("")) {
                    if (compareNowTime(start_time)) {
                        if (!end_time.equals("")) {
                            if (compareTwoTime(start_time, end_time)) {
                            if (!place.equals("")) {
                                if (!tel.equals("")) {
                                    if (tel.length() == 11) {
                                        if (!description.equals("")) {
                                            Intent intent = new Intent(CompetitionCreatActivity.this, SubmitActivity.class);
                                            intent.putExtra("id", MainActivity.userId);
                                            intent.putExtra("image", imagePath);
                                            intent.putExtra("name", name);
                                            intent.putExtra("start", start_time);
                                            intent.putExtra("end", end_time);
                                            intent.putExtra("place", place);
                                            intent.putExtra("tel", tel);
                                            intent.putExtra("description", description);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(this, "比赛简介不能为空", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "手机号码位数不对", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "比赛地点不能为空", Toast.LENGTH_SHORT).show();
                            }
                            }else {
                                endDialog();
                            }
                        } else {
                            Toast.makeText(this, "请选择结束时间", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        startDialog();
                    }
                } else {
                    Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "请填写比赛名称", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "请上传比赛海报", Toast.LENGTH_SHORT).show();
        }

    }
    private void selectImage(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK,null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent,PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"无访问权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO:
                if (resultCode == this.RESULT_OK) {
                    startPhotoZoom(data.getData());
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode != RESULT_OK){
                    return;
                }
                Uri imageUri = UCrop.getOutput(data);
                if (imageUri != null) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(imageUri);
                    } else {
                        handleImageBeforeKitKat(imageUri);
                    }
                }
                break;
            case UCrop.RESULT_ERROR:
               final Throwable error =  UCrop.getError(data);
                break;
        }
    }
    private void handleImageOnKitKat(Uri imageUri) {
        if (DocumentsContract.isDocumentUri(MyApplication.getContext(), imageUri)) {
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
        displayImage(imagePath);
    }


    private void handleImageBeforeKitKat(Uri imageUri) {
        imagePath = getImagePath(imageUri, null);
        displayImage(imagePath);

    }

    /**
     * 获取图片的真实路径
     *
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

    /**
     * 图片裁剪方法
     * @param uri  原始图片的路径
     */
    public void startPhotoZoom(Uri uri) {

        //mDestinationUri--目标裁剪的图片保存的Uri
        Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "CropImage.jpeg"));
        UCrop uCrop = UCrop.of(uri,mDestinationUri);


        // 开始设置
        UCrop.Options options = new UCrop.Options();
        // 设置图片的显示格式
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        //一共三个参数，分别对应裁剪功能页面的“缩放”，“旋转”，“裁剪”界面
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.NONE, UCropActivity.NONE);
        //设置缩放的大小
        options.withMaxResultSize(512,256);
        //设置缩放的尺寸
        options.withAspectRatio(16,9);
        //设置最大缩放比例
        options.setMaxScaleMultiplier(5);
        //是否为圆形裁剪框
        options.setCircleDimmedLayer(false);
        //设置图片在切换比例时的动画
        options.setImageToCropBoundsAnimDuration(666);
        //设置是否展示矩形裁剪框
        options.setShowCropFrame(true);
        //设置裁剪框横竖线的宽度
        //options.setCropGridStrokeWidth(20);
        //设置裁剪框横竖线的颜色
        //options.setCropGridColor(Color.GREEN);
        //设置竖线的数量
        options.setCropGridColumnCount(0);
        //设置横线的数量
        options.setCropGridRowCount(0);
        //结束设置
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    /**
     * 图片显示方法
     * @param imagepath 文件路径
     */
    private void displayImage(String imagepath){
        if (imagepath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            image.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"图片无法显示",Toast.LENGTH_SHORT).show();
        }

    }
    /**
     * 获取当前时间
     *
     * @return
     */
    public String getNowTime() {
        String timeString = null;
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        timeString = simpleDateFormat.format(date);
        return timeString;
    }

    /**
     * 与当前时间比较早晚
     *
     * @param time 需要比较的时间
     * @return 输入的时间比现在时间晚则返回true
     */
    public boolean compareNowTime(String time) {
        boolean isDayu = false;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date parse = dateFormat.parse(time);
            Date parse1 = dateFormat.parse(getNowTime());

            long diff = parse1.getTime() - parse.getTime();
            if (diff <= 0) {
                isDayu = true;
            } else {
                isDayu = false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return isDayu;
    }
    /**
     * 比较两个时间
     *
     * @param starTime
     *            开始时间
     * @param endString
     *            结束时间
     * @return 结束时间大于开始时间返回true，否则反之֮
     */
    public boolean compareTwoTime(String starTime, String endString) {
        boolean isDayu = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date parse = dateFormat.parse(starTime);
            Date parse1 = dateFormat.parse(endString);

            long diff = parse1.getTime() - parse.getTime();
            if (diff >= 0) {
                isDayu = true;
            } else {
                isDayu = false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return isDayu;

    }
}

