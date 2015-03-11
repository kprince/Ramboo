package com.xiaoming.random.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;

import com.xiaoming.random.R;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.utils.Utils;

public class BaseFragment extends Fragment {
    protected static final String SCREEN_NAME = "SCREEN_NAME";
    private static final String TAG = "BaseFragment";
    protected Handler handler = new Handler();

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

    public boolean checkNetwork() {
        return Utils.detect(getActivity());
    }

    public void setRefreshing(final SwipeRefreshLayout swipeRefreshLayout, final boolean tf) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(tf);
            }
        }, 400);
    }

    ;


    /**
     * 获取Material Design 的颜色
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
