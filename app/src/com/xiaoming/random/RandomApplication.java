package com.xiaoming.random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.StrictMode;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.xiaoming.random.utils.OauthUtils;


public class RandomApplication extends Application {
    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        //todo:remove when release
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        if (Constants.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }
        super.onCreate();
        L.writeLogs(false);
        initPreferences();
        initImageLoader(getApplicationContext());
    }

    /**
     * 初始化设置
     */
    private void initPreferences() {
        SharedPreferences sp = getSharedPreferences("appPref", Activity.MODE_PRIVATE);
        Editor editor = sp.edit();
        boolean isWifiConnected = OauthUtils.isWifiConnected(getApplicationContext());
//		if (isWifiConnected) {
//			editor.putBoolean("show_image", true);
//		}else {
//			editor.putBoolean("show_image", false);
//		}
        editor.putBoolean("show_image", true);
        editor.apply();
    }
}