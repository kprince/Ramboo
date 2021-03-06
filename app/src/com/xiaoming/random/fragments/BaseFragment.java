package com.xiaoming.random.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.View;

import com.xiaoming.random.R;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.utils.Utils;

public class BaseFragment extends Fragment {
    protected static final String SCREEN_NAME = "SCREEN_NAME";
    private static final String TAG = "BaseFragment";
    protected StatusDao mDao;
    protected View mRootView;
    protected Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return notifyDataSetChanged(msg);
        }
    });

    /**
     * 获取用户偏好设置
     *
     * @return
     */
    public SharedPreferences getUserPref() {
        return getActivity().getSharedPreferences(BaseActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void setTheme(int id) {
        getActivity().setTheme(id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        String themeColor = getUserPref().getString(BaseActivity.SF_THEME_COLOR, "Teal");
        setThemeColor(themeColor);
        mDao = new StatusDao();
    }


    /**
     *
     * @param msg
     */
    public boolean notifyDataSetChanged(Message msg){
       return false;
    }

    /**
     * 根据偏好设置设置主题颜色
     *
     * @param themeColor 偏好设置中保存的颜色
     */
    private void setThemeColor(String themeColor) {
        switch (themeColor) {
            case "Red":
                setTheme(R.style.Theme_Random_red);
                break;
            case "Pink":
                setTheme(R.style.Theme_Random_pink);
                break;
            case "Lime":
                setTheme(R.style.Theme_Random_lime);
                break;
            case "Light Green":
                setTheme(R.style.Theme_Random_light_green);
                break;
            case "Light Blue":
                setTheme(R.style.Theme_Random_light_blue);
                break;
            case "Purple":
                setTheme(R.style.Theme_Random_purple);
                break;
            case "Deep Purple":
                setTheme(R.style.Theme_Random_deep_purple);
                break;
            case "Teal":
                setTheme(R.style.Theme_Random_teal);
                break;
            case "Yellow":
                setTheme(R.style.Theme_Random_yellow);
                break;
            case "Blue":
                setTheme(R.style.Theme_Random_blue);
                break;
            case "Amber":
                setTheme(R.style.Theme_Random_amber);
                break;
            default:
                setTheme(R.style.Theme_Random_teal);
                break;
        }
    }

    /**
     * 开启线程读取缓存到数据库的内容
     */
    public void newGetCacheTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                getCachedContent();
            }
        }).start();
    }

    /**
     * 获取缓存的内容
     */
    public void getCachedContent(){

    }

    public boolean checkNetwork() {
        return Utils.detect(getActivity());
    }

    public void setRefreshing(final SwipeRefreshLayout swipeRefreshLayout, final boolean tf) {
        if (!tf) {
            swipeRefreshLayout.setRefreshing(tf);
            return;
        }
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipeRefreshLayout.setRefreshing(true);
//        TypedValue typed_value = new TypedValue();
//        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
//        swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
//        swipeRefreshLayout.setRefreshing(true);
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(tf);
//            }
//        }, 200);
    }


    /**
     * 获取Material主题的颜色colorPrimary/colorPrimaryDark/colorAccent
     *
     * @param attr Material Design Color attr like R.attr.colorPrimary
     * @return
     */
    public int getColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(attr, typedValue, false);
        return getResources().getColor(typedValue.data);
    }

    /**
     * 授权用户昵称
     *
     * @return 授权用户昵称
     */
    public String getUserName() {
        return getUserPref().getString(BaseActivity.SF_USER_NAME, "");
    }

    /**
     * 授权用户id
     *
     * @return
     */
    public long getUserID() {
        return getUserPref().getLong(BaseActivity.SF_USER_UID, 0);
    }

    /**
     * 授权用户头像地址
     *
     * @return
     */
    public String getUserAvatar() {
        return getUserPref().getString(BaseActivity.SF_USER_AVATAR, "");
    }
}
