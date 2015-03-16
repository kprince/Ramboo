package com.xiaoming.random.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.sina.weibo.sdk.openapi.models.User;
import com.xiaoming.random.R;
import com.xiaoming.random.fragments.UserProfileFragment;
import com.xiaoming.random.model.WeiboUser;

public class UserProfileActivity extends BaseActivity {
    protected static final String SCREEN_NAME = "SCREEN_NAME";
    private static final String TAG = "UserProfileActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.content_toolbar);
        toolbar.setTitle(getString(R.string.userDetail));
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        String screenName = null;
        WeiboUser user = null;
        UserProfileFragment fragment = null;
        if (bundle != null&&(user = (WeiboUser) bundle.getSerializable(UserProfileFragment.USER))!=null){
            fragment = UserProfileFragment.newInstance(user);
        }
        else if (bundle != null&& !TextUtils.isEmpty(screenName=bundle.getString(SCREEN_NAME)))
            fragment = UserProfileFragment.newInstance(screenName);
        else {
            Uri uri = getIntent().getData();
            String name = uri.getQueryParameter(SCREEN_NAME);
            screenName = name.substring(1, name.length());
            fragment = UserProfileFragment.newInstance(screenName);
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment, TAG);
        ft.commit();
    }
}
