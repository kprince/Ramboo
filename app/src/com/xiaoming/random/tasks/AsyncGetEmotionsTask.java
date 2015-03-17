package com.xiaoming.random.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.activities.SendWeiboActivity;
import com.xiaoming.random.dao.StatusDao;

/**
 * Created by XM on 2015/2/10.
 */
public class AsyncGetEmotionsTask extends AsyncTask {
    private static final String TAG = "AsyncGetEmotions";
    private static final String EMOTION_TYPE_FACE = "face";
    private static final String EMOTION_LAN_SIMPLIFIED = "cnname";
    private Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        StatusesAPI statusesAPI = new StatusesAPI((Oauth2AccessToken) params[0]);
        statusesAPI.emotions(EMOTION_TYPE_FACE, EMOTION_LAN_SIMPLIFIED, new RequestListener() {
            @Override
            public void onComplete(final String s) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StatusDao dao = new StatusDao();
                        dao.saveEmotions(s);
                    }
                }).start();
                SharedPreferences.Editor editor = mContext.getSharedPreferences(BaseActivity.USER_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putBoolean(SendWeiboActivity.EMOTIONS_EXISTS, true);
                editor.apply();
            }

            @Override
            public void onWeiboException(WeiboException e) {
                Log.v(TAG, mContext.getString(R.string.saveEmotionFailed) + e.getMessage());
            }
        });
        return null;
    }
}
