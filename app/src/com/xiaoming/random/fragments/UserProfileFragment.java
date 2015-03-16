package com.xiaoming.random.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.AccountsActivity;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.activities.GalleryActivity;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.model.WeiboUser;
import com.xiaoming.random.tasks.AsyncSave2DBTask;
import com.xiaoming.random.utils.OauthUtils;
import com.xiaoming.random.utils.TimeUtils;
import com.xiaoming.random.utils.Utils;

import java.util.ArrayList;

public class UserProfileFragment extends BaseFragment {
    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String USER = "USER";
    public static final String HAS_CACHE = "HAS_CACHE";
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                AsyncSave2DBTask task = new AsyncSave2DBTask();
                task.setContext(getActivity());
                task.execute(USER_PROFILE, USER, response);
                // 调用 WeiboUser#parse 将JSON串解析成User对象
                WeiboUser user = WeiboUser.parse(response);
                setUpViews(user);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(getActivity(), info.error == null ?
                            getString(R.string.networkUnavailable) : getString(R.string.loadFailed) + info.toString(),
                    Toast.LENGTH_LONG).show();
        }
    };
    protected static final String TAG = "UserProfileFragment";
    private static final String SCREEN_NAME = "SCREEN_NAME";
    private ImageView mAvatar;
    private TextView mUserName, mUserCity, mUserDescription, mStatusCount, mFriendCount, mFollowerCount, mUserCreateAt;
    private String mScreenName;
    private UsersAPI mUsersAPI;
    private Oauth2AccessToken mAccessToken;
    private long mUid;
    private boolean mSelfFlag;
    private ButtonRectangle mButton;
    private StatusDao mDao;
    private FriendshipsAPI mFriendsAPI;
    private android.os.Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            mUser = (WeiboUser) data.getSerializable(USER);
            if (!getActivity().isFinishing()&&data.getBoolean(HAS_CACHE,false))
                return setUpViews(mUser);
            initToken();
            mUsersAPI = new UsersAPI(mAccessToken);
            mUsersAPI.show(mScreenName, mListener);
            return true;
        }
    });
    private WeiboUser mUser;
    /**
     * required default constructor
     */
    public UserProfileFragment() {

    }

    /**
     * UserProfileFragment 创建工厂
     * @param user 用户信息 An instance of {@link WeiboUser}
     * @return
     */
    public static UserProfileFragment newInstance(WeiboUser user) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER,user);
        fragment.setArguments(bundle);
        return fragment;
    }
    /**
     * UserProfileFragment 创建工厂
     * @param screenName 用户昵称
     * @return
     */
    public static UserProfileFragment newInstance(String screenName) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screenName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SCREEN_NAME, mScreenName);
        outState.putSerializable(USER,mUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (savedInstanceState != null) {
            mUser = (WeiboUser) savedInstanceState.getSerializable(USER);
            if (mUser!=null)
               mScreenName = mUser.name;
            else
               mScreenName = savedInstanceState.getString(SCREEN_NAME);
        }
        else{
            if ((mUser = (WeiboUser) bundle.getSerializable(USER))!=null)
                mScreenName = mUser.name;
            else
                mScreenName = getArguments().getString(SCREEN_NAME);
        }
        if (mScreenName.equals(getUserName())) {
            mSelfFlag = true;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDao = new StatusDao(getActivity());
                mUid = getUserID();
            }
        }).start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_profile_hearder, container, false);
        findViews(rootView);
        if (mSelfFlag)
            mButton.setVisibility(View.GONE);
        else
            mButton.setVisibility(View.INVISIBLE);
        if (mUser==null)
            getUser();
        else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mDao.saveSingleUser(mUser.toString());
                }
            }).start();
            setUpViews(mUser);
        }

        //本应用用户
        if (mSelfFlag) {
            MainTimeLineFragment fragment = MainTimeLineFragment.newInstance(mScreenName, 0, 0);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.frg_container, fragment, null);
            ft.commit();
        }
        return rootView;
    }

    private void getUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WeiboUser user = mDao.getUserByName(mScreenName, USER);
                Message msg = new Message();
                Bundle bundle = new Bundle();
                if (user!=null){
                   bundle.putSerializable(USER,user);
                   bundle.putBoolean(HAS_CACHE,true);
                }else{
                   bundle.putBoolean(HAS_CACHE,false);
                }
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }).start();
//        WeiboUser user = mDao.getUserByName(mScreenName, USER);
//        if (user != null) {
//            setUpViews(user);
//        } else {
//            initToken();
//            mUsersAPI = new UsersAPI(mAccessToken);
//            mUsersAPI.show(mScreenName, mListener);
//        }
    }

    private void initToken() {
        if (mAccessToken == null) {
            AuthUser authUser = mDao.getAuthUser(mUid);
            mAccessToken = new Oauth2AccessToken(authUser.token, authUser.expires);
        }
        if (!mAccessToken.isSessionValid()) {
            OauthUtils.showToast(getActivity(), getString(R.string.authExpired));
            Intent oauthIntent = new Intent(getActivity(),
                    AccountsActivity.class);
            startActivity(oauthIntent);
            return;
        }
        mFriendsAPI = new FriendshipsAPI(mAccessToken);
    }

    private void findViews(View rootView) {
        mAvatar = (ImageView) rootView.findViewById(R.id.userProfileAvatar);
        mUserName = (TextView) rootView.findViewById(R.id.userProfileName);
        mUserCity = (TextView) rootView.findViewById(R.id.userProfileCity);
        mUserCreateAt = (TextView) rootView.findViewById(R.id.userProfCreate);
        mUserDescription = (TextView) rootView.findViewById(R.id.userProfileDescirption);
        mStatusCount = (TextView) rootView.findViewById(R.id.statusCount);
        mFriendCount = (TextView) rootView.findViewById(R.id.friendCount);
        mFollowerCount = (TextView) rootView.findViewById(R.id.followerCount);
        mButton = (ButtonRectangle) rootView.findViewById(R.id.userProBtn);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mFriendsAPI == null) initToken();
                        WeiboUser user = (WeiboUser) v.getTag();
                        String btnText = ((ButtonRectangle) v).getText();
                        if (btnText.equals(getString(R.string.cancelFollow))) {
                            mFriendsAPI.destroy(Long.parseLong(user.id), user.screenName, new FriendshipRequestListener());
                            return;
                        }
                        mFriendsAPI.create(Long.parseLong(user.id), user.screenName, new FriendshipRequestListener());
                    }
                });
                mAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WeiboUser user = (WeiboUser) v.getTag();
                        Intent intent = new Intent(getActivity(), GalleryActivity.class);
                        intent.putExtra("multi", false);//多图
                        intent.putExtra("position", 0);//多图时选中的图片位置
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(user.avatarHd);
                        intent.putStringArrayListExtra("uriList", list);
                        startActivity(intent);
                    }
                });
            }
        }).start();

    }


    /**
     * 创建user视图
     *
     * @param user
     */
    private boolean setUpViews(WeiboUser user) {
        if (mUserCity != null) {
            if (user != null) {
                mUserCity.setText(user.gender.equals("n") ? getString(R.string.sexNA) : (user.gender.equals("f") ?
                        getString(R.string.female) : getString(R.string.male)) + user.location);
                if (!TextUtils.isEmpty(user.description)) {
                    //int index2 = index==0?user.description.length():user.description.lastIndexOf('-');
                    mUserDescription.setText(user.description);
                }
                ImageLoader.getInstance().displayImage(user.avatarLarge,
                        mAvatar, BaseActivity.UIL_OPTIONS);
                mUserName.setText(user.name);
                mStatusCount.setText(Utils.formatNum(user.statusesCount) + getString(R.string.weibo));
                mFriendCount.setText(Utils.formatNum(user.friendsCount) + getString(R.string.followed));
                mFollowerCount.setText(Utils.formatNum(user.followersCount) + getString(R.string.followMe));
                mUserCreateAt.setText(getString(R.string.createAt) + TimeUtils.parseTime(user.createdAt));
                mButton.setTag(user);
                if (!mSelfFlag) {
                    mButton.setVisibility(View.VISIBLE);
                    mButton.setBackgroundColor(getColor(R.attr.colorAccent));
                    if (user.following) {
                        mButton.setText(getString(R.string.cancelFollow));
                        mButton.setBackgroundColor(getResources().getColor(R.color.orange_700));
                    }
                }
                mAvatar.setTag(user);
            }
        }
        return true;
    }

    /**
     * 关注、取消关注回调
     */
    public class FriendshipRequestListener implements RequestListener {

        @Override
        public void onComplete(String s) {
            OauthUtils.showToast(getActivity(), getString(R.string.operationSucceed));
            String btnText = mButton.getText();
            if (btnText.equals(getString(R.string.cancelFollow))) {
                mButton.setText(getString(R.string.follow));
                mButton.setBackgroundColor(getColor(R.attr.colorAccent));
                return;
            }
            mButton.setText(getString(R.string.cancelFollow));
            mButton.setBackgroundColor(getResources().getColor(R.color.orange_500));
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Log.e(TAG, e.getMessage());
            OauthUtils.showToast(getActivity(), getString(R.string.operationFailed) + e.getLocalizedMessage());
        }
    }
}
