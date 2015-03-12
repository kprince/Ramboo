package com.xiaoming.random.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.xiaoming.random.Constants;
import com.xiaoming.random.R;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.tasks.AsyncGetEmotionsTask;
import com.xiaoming.random.utils.OauthUtils;

import java.util.ArrayList;
import java.util.List;

public class AccountsActivity extends BaseActivity {

    private long mBackPressedTime = 0;
    private ListView mAccountList;
    // private Oauth2AccessToken mAccessToken;
    private UsersAPI mUsersAPI;
    private User mUser;
    private List<AuthUser> mUserList = new ArrayList<AuthUser>();
    private AccountListAdapter mUserListAdapter = new AccountListAdapter();
    private Toolbar mToolbar;
    private WeiboAuth mWeiboAuth;
    private Oauth2AccessToken mToken;
    /**
     * userAPI listner
     */
    private RequestListener mListener = new RequestListener() {

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                mStatusDao.saveAuthUser(response, mToken);
                User user = User.parse(response);
                Intent mtlIntent = new Intent(AccountsActivity.this,
                        AppMainActivity.class);
                SharedPreferences sp = getUserPref();
                Editor editor = sp.edit();
                editor.putLong(SF_USER_UID, Long.parseLong(user.id));
                editor.putString(SF_USER_NAME, user.name);
                editor.putString(SF_USER_AVATAR, user.avatar_large);
                editor.putBoolean(AppMainActivity.FIRST_TIME_FLAG, false);
                editor.apply();
                startActivity(mtlIntent);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(AccountsActivity.this, info.toString(),
                    Toast.LENGTH_LONG).show();
        }
    };
    private StatusDao mStatusDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusDao = new StatusDao(this);
        setContentView(R.layout.acount_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
//      Lollipop sso授权报错：Service Intent must be explicit
//      mSsoHandler = new SsoHandler(this, mWeiboAuth);
        setToolBar(mToolbar, BaseActivity.ACCOUNT, null);
        getUserList();
        mAccountList = (ListView) findViewById(R.id.accountList);
        mAccountList.setAdapter(mUserListAdapter);
        mAccountList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Long uid = Long.decode(mUserList.get(position).id);
                String userName = mUserList.get(position).name;
                String userAvartar = mUserList.get(position).avatar_large;
                Intent mtlIntent = new Intent(AccountsActivity.this,
                        AppMainActivity.class);
                SharedPreferences sp = getUserPref();
                Editor editor = sp.edit();
                editor.putLong(SF_USER_UID, uid);
                editor.putString(SF_USER_NAME, userName);
                editor.putString(SF_USER_AVATAR, userAvartar);
                editor.apply();
                startActivity(mtlIntent);
            }

        });
    }

    /**
     * 获取所有token
     */
    private void getUserList() {
        mUserList = getUsers();
        if (mUserList != null && mUserList.size() > 0) {
            mUserListAdapter.notifyDataSetChanged();
        } else {
            mWeiboAuth.anthorize(new AuthListener());
        }
    }

    @Override
    public void onBackPressed() {
        long t = System.currentTimeMillis();
        if (t - mBackPressedTime > 2000) {    // 2 secs
            mBackPressedTime = t;
            Toast.makeText(this, getString(R.string.pressAgain),
                    Toast.LENGTH_SHORT).show();
        } else {    // this guy is serious
            // clean up
            super.onBackPressed();       // bye
        }

    }

    /**
     * 获取已授权用户
     *
     * @return
     */
    private List<AuthUser> getUsers() {
//		mStatusDao = new StatusDao(this);
        return mStatusDao.getAuthUserList();
    }

    private void requestUser() {
        if (mToken != null && mToken.isSessionValid()) {
            mUsersAPI = new UsersAPI(mToken);
            mUsersAPI.show(Long.parseLong(mToken.getUid()), mListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (mSsoHandler != null) {
//            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accounts_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
            // 授权
            case 0:
                mWeiboAuth.anthorize(new AuthListener());
                break;
            default:
                break;
        }
        return true;
    }

    public void redirect2Timeline() {
        if (mToken != null && mToken.isSessionValid()) {
            Intent intent = new Intent(this, AppMainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 微博数等
     *
     * @param user
     * @return
     */
    public String formatUserCounts(User user) {
        return getString(R.string.followed) + " " + user.friends_count + "  "
                + getString(R.string.weibo) + " " + user.statuses_count + "  "
                + getString(R.string.followMe) + " "
                + user.followers_count;
    }

    class AuthListener implements WeiboAuthListener {
        @Override
        public void onWeiboException(WeiboException arg0) {
            OauthUtils.showToast(AccountsActivity.this,
                    getString(R.string.authFailed) + arg0.getMessage());
        }

        @Override
        public void onComplete(Bundle values) {
            mToken = Oauth2AccessToken.parseAccessToken(values);
            Editor editor = getSharedPreferences(BaseActivity.USER_PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(BaseActivity.SF_JUST_AUTH, true);
            editor.apply();
            OauthUtils.showToast(AccountsActivity.this, getString(R.string.authSucceed));
            if (!getUserPref().getBoolean(SendWeiboActivity.EMOTIONS_EXISTS, false)) {
                AsyncGetEmotionsTask task = new AsyncGetEmotionsTask();
                task.setContext(AccountsActivity.this);
                task.execute(mToken);
            }
            AccountsActivity.this.requestUser();
        }

        @Override
        public void onCancel() {
            OauthUtils.showToast(AccountsActivity.this, getString(R.string.cancelAuth));
        }
    }

    /**
     * 用户账户列表adapter
     *
     * @author xiaoming
     */
    public class AccountListAdapter extends BaseAdapter {
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            mUser = mUserList.get(position);
            view.requestLayout();
            if (view == null) {
                view = View.inflate(AccountsActivity.this, R.layout.user_layout, null);
            }
            TextView userNickName = (TextView) view
                    .findViewById(R.id.userNickName);
            ImageView userAvatar = (ImageView) view
                    .findViewById(R.id.userAvatar);
            TextView userCreateAt = (TextView) view
                    .findViewById(R.id.userCreateAt);
            TextView userCounts = (TextView) view.findViewById(R.id.userCounts);
            userNickName.setText(mUser.screen_name);
            ImageLoader.getInstance().displayImage(mUser.avatar_large,
                    userAvatar, UIL_OPTIONS);
            userCreateAt.setText(getString(R.string.createAt)
                    + OauthUtils.formatCreateAt(mUser.created_at));
            userCounts.setText(formatUserCounts(mUser));
            return view;
        }

        @Override
        public long getItemId(int position) {
            Long uid = Long.decode(mUserList.get(position).id);
            return uid;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return mUserList.size();
        }

    }
}