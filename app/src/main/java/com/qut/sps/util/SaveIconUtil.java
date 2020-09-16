package com.qut.sps.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by lenovo on 2017/8/7.
 */

public class SaveIconUtil {

    public final static String ALBUM_PATH = Environment.getExternalStorageDirectory().getPath() + "/userIcon/";
    private static String iconName;
    /**
     * 通过网址把网上的图片转化为bitmap
     * @param address 图片服务器的准确地址
     * @return
     */
    public static Bitmap doInBackground(String address) {
        InputStream is;
        iconName = address.substring(address.lastIndexOf("/"));
        Bitmap bitmap = null;
        try {
            is = new URL(address).openStream();
            bitmap = BitmapFactory.decodeStream(is);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将下载的图片的网址传给该法方法
     * @param address
     * @return
     */
    public static boolean saveFile(String address) {
        Bitmap bm = doInBackground(address);
        File dirFile = new File(ALBUM_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myFile = new File(ALBUM_PATH + iconName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myFile));
            bm.compress(Bitmap.CompressFormat.PNG, 80, bos);
            bos.flush();
            bos.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
