package com.xiaoming.random.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonFloat;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.utils.LogUtil;
import com.xiaoming.random.Constants;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.activities.UserProfileActivity;
import com.xiaoming.random.listener.PauseOnScrollListener;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.model.WeiboUser;
import com.xiaoming.random.tasks.AsyncSave2DBTask;
import com.xiaoming.random.utils.OauthUtils;
import com.xiaoming.random.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendshipFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TYPE = "TYPE";
    public static final String FRIENDSHIP = "FRIENDSHIP";
    public static final String FRIEND = "FRIEND";
    public static final int FRIEND_INDEX = 0;
    public static final String FOLLOWER = "FOLLOWER";
    public static final int FOLLOWER_INDEX = 1;
    protected static final String TAG = "CommentsFragment";
    private static final int DEFAULT_COUNT = 200;
    private static final String POSITION = "POSITION";
    private static final String SCREEN_NAME = "SCREEN_NAME";
    private int mTabPosition;
    private int mPage = 0;
    private long mUid;
    private Oauth2AccessToken mAccessToken;
    private FriendshipsAPI mFriendsAPI;
    private List<WeiboUser> mCommentList = new ArrayList<>();
    private RecyclerView mCommentsView;
    private SwipeRefreshLayout swipeLayout;
    private FriendShipListListener mCommentsListListener = new FriendShipListListener();
    private FriendShipListAdapter mCommentsListAdapter = new FriendShipListAdapter();
    private String mType;
    private ButtonFloat mSendBtn;


    public FriendshipFragment() {

    }

    public static FriendshipFragment newInstance(int position) {
        FriendshipFragment fragment = new FriendshipFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, mTabPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mTabPosition = savedInstanceState.getInt(POSITION);
        else {
            Bundle bundle = getArguments();
            mTabPosition = bundle.getInt(POSITION);
        }
        initFriendsType();
    }

    private void initFriendsType() {
        switch (mTabPosition) {
            case FRIEND_INDEX:
                mType = FRIEND;
                break;
            case FOLLOWER_INDEX:
                mType = FOLLOWER;
                break;
            default:
                mType = FRIEND;
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Constants.DEVELOPER_MODE)
            Debug.startMethodTracing(TAG);
        mRootView = inflater.inflate(R.layout.main_time_line_layout, container, false);
        mCommentsView = (RecyclerView) mRootView.findViewById(R.id.main_time_line);
        mCommentsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsView.setAdapter(mCommentsListAdapter);
        mCommentsView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true));
        swipeLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(this);
        Utils.setSwipeRefreshColorSchema(swipeLayout);
        mSendBtn = (ButtonFloat) mRootView.findViewById(R.id.back_to_top);
        mSendBtn.setBackgroundColor(getColor(R.attr.colorAccent));
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsView.getLayoutManager().scrollToPosition(0);
            }
        });
        setRefreshing(swipeLayout, true);
        newGetCacheTask();
        if (Constants.DEVELOPER_MODE)
            Debug.stopMethodTracing();
        return mRootView;
    }

    @Override
    public boolean notifyDataSetChanged(Message msg) {
        int what = msg.what;
        if (what==1&&mCommentsListAdapter!=null)
            mCommentsListAdapter.notifyDataSetChanged();
        if (swipeLayout.isRefreshing())
            setRefreshing(swipeLayout,false);
//            swipeLayout.setRefreshing(false);
        return true;
    }

    /**
     * 获取缓存的用户列表
     *
     * @return
     */
    @Override
    public void getCachedContent() {

        mCommentList = mDao.getUserList(mType, DEFAULT_COUNT);
        if (mCommentList == null || mCommentList.size() == 0) {
            getUserList();
        } else {
            mHandler.sendEmptyMessage(1);
        }
    }

    @Override
    public void onRefresh() {
        getUserList();
    }

    /**
     * 初始化关系API
     */
    private void initFriendsApi() {
        mUid = getUserID();
        if (mAccessToken == null && mUid > 0) {
            AuthUser user = mDao.getAuthUser(mUid);
            mAccessToken = new Oauth2AccessToken(user.token, user.expires);
        }
        if (mAccessToken != null && mAccessToken.isSessionValid())
            mFriendsAPI = new FriendshipsAPI(mAccessToken);
    }

    private void getUserList() {
        if (mFriendsAPI == null)
            initFriendsApi();
        if (checkNetwork()) {
            switch (mTabPosition) {
                //关注列表
                case FRIEND_INDEX:
                    mFriendsAPI.friends(mUid, 200, 0, true, mCommentsListListener);
                    break;
                //粉丝列表
                case FOLLOWER_INDEX:
                    mFriendsAPI.followers(mUid, 200, 0, true, mCommentsListListener);
                    break;
                default:
                    mFriendsAPI.followers(mUid, 200, 0, false, mCommentsListListener);
                    break;
            }
        } else {
            swipeLayout.setRefreshing(false);
            OauthUtils.showToast(getActivity(), getString(R.string.networkUnavailable));
        }
    }

    public class FriendShipListListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"users\"")) {
                    AsyncSave2DBTask task = new AsyncSave2DBTask();
                    task.setContext(getActivity());
                    try {
                        task.execute(FRIENDSHIP, mType, response);
                        JSONObject json = new JSONObject(response);
                        JSONArray array = json.optJSONArray("users");
                        mPage = json.optInt("next_cursor", 0);
                        if (array != null && array.length() > 0) {
                            mCommentList.clear();
                            if (mTabPosition == 0) {
                                for (int i = array.length() - 1; i >= 0; i--) {
                                    mCommentList.add(0, WeiboUser.parse(array.optJSONObject(i)));
                                }
                            } else {
                                for (int i = 0; i < array.length(); i++) {
                                    mCommentList.add(0, WeiboUser.parse(array.optJSONObject(i)));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            mCommentsListAdapter.notifyDataSetChanged();
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(false);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(false);
            }
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(getActivity(), info.error==null?getString(R.string.networkUnavailable):info.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * RecyclerView adapter
     */
    public class FriendShipListAdapter extends RecyclerView.Adapter<FriendShipListAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friendship_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            setUpView(holder, position);
        }

        private void setUpView(ViewHolder holder, int position) {
            WeiboUser user = mCommentList.get(position);
            if (!TextUtils.isEmpty(user.name)) {
                holder.userImage.setTag(user);
                holder.userName.setTag(user);
                holder.userDesc.setText(user.description);
                String gender;
                if (user.gender.equals("n")) {
                    gender = getString(R.string.sexNA);
                } else {
                    gender = user.gender.equals("f") ? getString(R.string.female) : getString(R.string.male);
                }
                holder.cancel.setBackgroundColor(getColor(R.attr.colorAccent));
                holder.cancel.setTag(user);
                if (mTabPosition == 1 && !user.following)
                    holder.cancel.setText(getResources().getString(R.string.follow));
                holder.userLocation.setText(gender + user.location);
                holder.userName.setText(user.name);
                ImageLoader.getInstance().displayImage(user.avatarLarge, holder.userImage, BaseActivity.UIL_OPTIONS);
            }
        }

        @Override
        public int getItemCount() {
            return mCommentList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CircleImageView userImage;
            ButtonFlat cancel;
            TextView userName, userDesc, userLocation;


            public ViewHolder(View itemView) {
                super(itemView);
                initAll(itemView);
            }

            void initAll(View view) {
                userImage = (CircleImageView) view.findViewById(R.id.friendAvatar);
                userName = (TextView) view.findViewById(R.id.friendName);
                userDesc = (TextView) view.findViewById(R.id.friendDesc);
                userLocation = (TextView) view.findViewById(R.id.friendLocation);
                cancel = (ButtonFlat) view.findViewById(R.id.cancelFriendship);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mFriendsAPI == null)
                            initFriendsApi();
                        ButtonFlat btn = (ButtonFlat) v;
                        WeiboUser user = (WeiboUser) v.getTag();
                        String follow = getResources().getString(R.string.follow);
                        if (btn.getText().equals(follow)) {
                            mFriendsAPI.create(Long.parseLong(user.id), user.screenName, new FriendshipRequestListener());
                        } else {
                            mFriendsAPI.destroy(Long.parseLong(user.id), user.screenName, new FriendshipRequestListener());
                        }
                    }
                });
                userName.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(UserProfileFragment.USER,(WeiboUser)v.getTag());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        /**
         * 关注、取消关注回调
         */
        public class FriendshipRequestListener implements RequestListener {

            @Override
            public void onComplete(String s) {
                OauthUtils.showToast(getActivity(), getString(R.string.operationSucceed));
                setRefreshing(swipeLayout, true);
                getUserList();
            }

            @Override
            public void onWeiboException(WeiboException e) {
                Log.e(TAG, e.getMessage());
                ErrorInfo info = ErrorInfo.parse(e.getMessage());
                OauthUtils.showToast(getActivity(), info.error == null ?
                        getString(R.string.networkUnavailable) : getString(R.string.loadFailed) + info.toString());
            }
        }
    }
}
