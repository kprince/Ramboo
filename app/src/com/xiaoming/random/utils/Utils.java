package com.xiaoming.random.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
    public static boolean detect(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }

    /**
     * 格式化数字
     *
     * @param num 需要格式化的数字
     * @return
     */
    public static String formatNum(int num) {
        String s;
        if (num > 10000)
            s = num / 10000 + "万";
        else
            s = String.valueOf(num);
        return s;
    }


}
