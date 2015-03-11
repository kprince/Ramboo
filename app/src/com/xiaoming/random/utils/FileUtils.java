package com.xiaoming.random.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 文件操作工具类
 * Created by XM on 2015/3/6.
 */
public class FileUtils {

    public static final String FIlE_PATH_CARD = "/Random/cards";

    /**
     * 格式化文件大小
     *
     * @param space 文件容量
     * @return 格式化后的字符串
     */
    public static String getFileSpace(long space) {
        if (space < (1024 * 1024))
            return space / 1024 + "KB";
        return space / (1024 * 1024) + "MB";
    }

    /**
     * 获取文件夹占用磁盘空间
     *
     * @param dir 目标文件夹
     * @return 文件夹占用空间
     */
    public static long getDirSize(File dir) {
        if (dir == null) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return 0;
        }
        long dirSize = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                dirSize += file.length();
            } else if (file.isDirectory()) {
                dirSize += file.length();
                dirSize += getDirSize(file); // 如果遇到目录则通过递归调用继续统计
            }
        }
        return dirSize;
    }

    public static String getFileSizeStr(long fileSize) {
        String sFileSize = "";
        if (fileSize > 0) {
            double dFileSize = (double) fileSize;
            double kiloByte = dFileSize / 1024;
            if (kiloByte < 1) {
                return sFileSize + "Byte(s)";
            }
            double megaByte = kiloByte / 1024;
            if (megaByte < 1) {
                sFileSize = String.format("%.2f", kiloByte);
                return sFileSize + "KB";
            }

            double gigaByte = megaByte / 1024;
            if (gigaByte < 1) {
                sFileSize = String.format("%.2f", megaByte);
                return sFileSize + "MB";
            }
            double teraByte = gigaByte / 1024;
            if (teraByte < 1) {
                sFileSize = String.format("%.2f", gigaByte);
                return sFileSize + "GB";
            }
            sFileSize = String.format("%.2f", teraByte);
            return sFileSize + "TB";
        }
        return sFileSize;
    }

    /**
     * 创建文件夹：如果文件夹不存在，则创建之
     *
     * @param path
     */
    public static void createDir(String path) {
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
    }

    /**
     * 将view保存为图片
     *
     * @param view
     */
    public static void convertView2Image(View view) throws Exception {
        Bitmap b = getBitmapFromView(view);
        String id = view.getTag().toString();
        if (!TextUtils.isEmpty(id)) {
            String path = Environment.getExternalStorageDirectory() + FIlE_PATH_CARD;
            createDir(path);
            String name = id + ".png";
            File file = new File(path + name);
            if (file.exists())
                file.delete();
            file.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            b.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
        }
    }


    /**
     * 将view转为bitmap,
     *
     * @param view
     * @return
     */
    /*The most voted solution did not work for me because my view is a ViewGroup(have been inflated from a LayoutInflater). I needed to call view.measure to force the view size to be calculated in order to get the correct view size with view.getMeasuredWidth(Height).
    public static Bitmap getBitmapFromView(View view) {
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }*/
    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}
