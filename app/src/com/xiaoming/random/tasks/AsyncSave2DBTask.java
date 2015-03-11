package com.xiaoming.random.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.fragments.CommentsFragment;
import com.xiaoming.random.fragments.FriendshipFragment;
import com.xiaoming.random.fragments.MainTimeLineFragment;
import com.xiaoming.random.fragments.UserProfileFragment;

/**
 * 异步数据库操作
 * Created by XIAOM on 2015/1/30.
 */
public class AsyncSave2DBTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private String mObjectType;//保存的对象类型：微博 STATUS、 用户 USER、评论 COMMENT
    private String mType;//保存类型
    private String mWhat;
    private StatusDao mDao;


    public void setContext(Context context) {
        if (context != null) mContext = context;
    }

    /**
     * @param params [mObjectType,mType,ObjectString]
     * @return
     */
    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null & params.length < 3)
            throw new RuntimeException("params is null");
        mDao = new StatusDao(mContext);
        mObjectType = params[0];
        mType = params[1];
        mWhat = params[2];
        switch (mObjectType) {
            case MainTimeLineFragment.STATUS:
                mDao.saveStatus(mWhat, mType);
                break;
            case CommentsFragment.COMMENTS:
                mDao.saveComments(mWhat, mType);
                break;
            case FriendshipFragment.FRIENDSHIP:
                mDao.saveUser(mWhat, mType);
                break;
            case UserProfileFragment.USER_PROFILE:
                mDao.saveSingleUser(mWhat);
                break;
        }
        return true;
    }
}
