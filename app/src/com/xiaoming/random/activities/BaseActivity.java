package com.xiaoming.random.activities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.xiaoming.random.R;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.utils.Utils;

public class BaseActivity extends ActionBarActivity {
    public static final int ACCOUNT = 0;
    public static final int INDEX = 1;
    public static final int COMMENT = 2;
    public static final String TAG = "BaseActivity";
    public static final String USER_PREFERENCES = "USER_PREFERENCES";//用户偏好设置
    public static final String SF_JUST_AUTH = "JUST_AUTH";//是否授权后第一次使用
    public static final String SF_USER_NAME = "USER_NAME";//微博用户昵称
    public static final String SF_USER_UID = "UID";//微博用户id
    public static final String SF_USER_AVATAR = "USER_AVATAR";//微博用户头像
    public static final String SF_THEME_COLOR = "THEME_COLOR";
    protected StatusDao mDao;

    public static DisplayImageOptions UIL_OPTIONS = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.ic_error)
            .showImageOnLoading(R.drawable.menu_background)
            .resetViewBeforeLoading(true).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
            .displayer(new FadeInBitmapDisplayer(300)).build();


    public SharedPreferences getUserPref() {
        return getSharedPreferences(USER_PREFERENCES, MODE_APPEND);
    }

    /**
     * 设置系统状态栏background（仅4.4以上系统有效）
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setStatusBarMDStyle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getColor(R.attr.colorPrimaryDark));
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String themeColor = getUserPref().getString(SF_THEME_COLOR, "Teal");
        setThemeColor(themeColor);
        setStatusBarMDStyle();
        mDao = new StatusDao();
    }


    public void setToolBar(Toolbar toolbar, int which, String what) {
        switch (which) {
            case ACCOUNT:
                toolbar.setTitle(getString(R.string.accounts));
                break;
            case INDEX:
                toolbar.setTitle(what);
                break;
            default:
                toolbar.setTitle(what);
                break;
        }
        setSupportActionBar(toolbar);
        if (which == INDEX) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_menu_white_36dp);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNavigationDrawer();
            }
        });


    }

    /**
     * hook to show NavigationDrawer
     */
    public void showNavigationDrawer() {

    }

    /**
     * 检查网络状态
     *
     * @return
     */
    public boolean checkNetwork() {
        return Utils.detect(this);
    }

    /**
     * 获取Material Design 的颜色
     *
     * @param attr Material Design Color attr like R.attr.colorPrimary
     * @return
     */
    public int getColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, false);
        return getResources().getColor(typedValue.data);
    }

    /**
     * 获取授权用户昵称
     *
     * @return 授权用户昵称
     */
    public String getUserName() {
        return getUserPref().getString(SF_USER_NAME, "");
    }

    /**
     * 获取授权用户uid
     *
     * @return 授权用户uid
     */
    public long getUserID() {
        return getUserPref().getLong(SF_USER_UID, 0);
    }

    /**
     * 获取授权用户头像 url
     *
     * @return 授权用户头像 url
     */
    public String getUserAvatar() {
        return getUserPref().getString(SF_USER_AVATAR, "");
    }

    /**
     * setImageViewFilter
     *
     * @param image
     * @param id
     */
    public void setImageViewFilter(ImageView image, int id) {
        image.setColorFilter(getResources().getColor(id), PorterDuff.Mode.MULTIPLY);
    }
}
