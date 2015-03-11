package com.xiaoming.random.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.FavoritesAPI;
import com.sina.weibo.sdk.openapi.legacy.PlaceAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.FavoriteList;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;
import com.xiaoming.random.Constants;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.AccountsActivity;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.listener.PauseOnScrollListener;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.tasks.AsyncSave2DBTask;
import com.xiaoming.random.utils.OauthUtils;
import com.xiaoming.random.utils.StatusViewHolder;
import com.xiaoming.random.widgets.RandomButtonFloat;

import java.util.ArrayList;
import java.util.List;

public class MainTimeLineFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener {
    public static final String STATUS_HOME = "HOME";//首页
    private String mType = STATUS_HOME;//微博类型
    public static final String STATUS_BY_ME = "BY_ME";//我的微博
    public static final String STATUS_FAVORITES = "FAVORITES";//我的收藏
    public static final String STATUS_NEARBY = "NEARBY";//附近微博
    public static final String STATUS_AT_ME = "AT_ME";//@我的微博
    public static final String STATUS_USER = "USER";//用户微博
    public static final String STATUS_EACH_OTHER = "EACH_OTHER";//互相关注
    public static final String STATUS = "STATUS";
    public static final String SCREEN_NAME = "SCREEN_NAME", AT_FLAG = "AT_FLAG", POSITION = "POSITION",SINCE_ID="SINCE_ID";
    private static final int STATUS_DEFAULT_LENGTH = 50;
    protected final String TAG = "MainTimeLineFragment";
    private int mTabPosition;
    private long mMaxId = 0;
    private long mSinceId = 0;
    private long mUid;
    private Oauth2AccessToken mAccessToken;
    private StatusesAPI mStatusesAPI;
    private FavoritesAPI mFavoriteAPI;
    private PlaceAPI mPlaceAPI;
    private List<Status> mStatusList = new ArrayList<>();
    private RecyclerView mStatusListView;
    private StatusAdapter mStatusAdapter;
    private SwipeRefreshLayout swipeLayout;
    private RequestListener requestListener = new StatusRequestListener();
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private int mAtFlag;
    private String mScreenName;
    private StatusDao mStatusDao;
    private RecyclerView.LayoutManager mLayoutManager;
    private RandomButtonFloat mBack2Top;

    public static MainTimeLineFragment newInstance(String screenName, int position, int atFlag) {
        MainTimeLineFragment fragment = new MainTimeLineFragment();
        Bundle args = new Bundle();
        args.putString(SCREEN_NAME, screenName);
        args.putInt(AT_FLAG, atFlag);
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, mTabPosition);
        outState.putInt(AT_FLAG, mAtFlag);
        outState.putString(SCREEN_NAME, mScreenName);
        outState.putLong(SINCE_ID,mSinceId);
        super.onSaveInstanceState(outState);
    }


    /**
     * 点击收藏按钮时触发刷新列表
     */
    public void refresh() {
        if (!mType.equals(STATUS_FAVORITES))
            return;
        if (swipeLayout != null)
            swipeLayout.setRefreshing(true);
        onRefresh();
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mTabPosition = savedInstanceState.getInt(POSITION, 0);
            mAtFlag = savedInstanceState.getInt(AT_FLAG, 0);
            mScreenName = savedInstanceState.getString(SCREEN_NAME, "");
            mSinceId = savedInstanceState.getLong(SINCE_ID,0);
        } else {
            Bundle bundle = getArguments();
            mTabPosition = bundle.getInt(POSITION, 0);
            mAtFlag = bundle.getInt(AT_FLAG, 0);
            mScreenName = bundle.getString(SCREEN_NAME, "");
            mSinceId = 0l;
        }
        mMaxId = 0;
        mStatusDao = new StatusDao(getActivity());
        initStatusType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_time_line_layout,
                container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(
                R.color.red_a400,
                R.color.green_a400,
                R.color.light_blue_a400,
                R.color.pink_a400,
                R.color.teal_a400, R.color.purple_a400,
                R.color.indigo_a400
        );
        setRefreshing(swipeLayout, true);
        mStatusListView = (RecyclerView) rootView.findViewById(R.id.main_time_line);
//      ImageLoader滑动停止加载图片
//		mStatusListView.setOnScrollListener(new PauseOnScrollListener(imageLoader, true, true));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mStatusListView.setLayoutManager(mLayoutManager);
        mStatusAdapter = new StatusAdapter();
        mStatusListView.setAdapter(mStatusAdapter);
        mStatusListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true));
        getCachedStatus();
        mBack2Top = (RandomButtonFloat) rootView.findViewById(R.id.back_to_top);
        mBack2Top.setBackgroundColor(getColor(R.attr.colorAccent));
        mBack2Top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutManager.scrollToPosition(0);
            }
        });
        return rootView;
    }

    private void initToken() {
        if (mAccessToken == null) {
            long uid = getUserID();
            if (uid > 0) {
                StatusDao dao = new StatusDao(getActivity());
                AuthUser user = dao.getAuthUser(uid);
                mAccessToken = new Oauth2AccessToken(user.token, user.expires);
            }
        }
    }

    /**
     * 获取缓存的微博列表
     */
    public void getCachedStatus() {
        //授权后第一次进入，不进行第二个TAB的预加载
//        if (mTabPosition==1&&getUserPref().getBoolean(BaseActivity.SF_JUST_AUTH,false)){
//            SharedPreferences.Editor editor = getUserPref().edit();
//            editor.putBoolean(BaseActivity.SF_JUST_AUTH,false);
//            editor.commit();
//            return;
//        };
        mSinceId = mStatusDao.getSinceId(STATUS, mType);
        mStatusList = mStatusDao.readStatus(STATUS_DEFAULT_LENGTH, mType);
        if (mStatusList == null || mStatusList.size() <= 0) {
            getTimeLine();
        } else {
            mStatusAdapter.notifyDataSetChanged();
            setRefreshing(swipeLayout, false);
        }
    }

    /**
     * 获取用户微博列表
     */
    private void getTimeLine() {
        if (checkNetwork()) {
            initToken();
            if (mAccessToken.isSessionValid()) {
                getUserTimeline();
            } else {
                OauthUtils.showToast(getActivity(), getString(R.string.authExpired));
                Intent intent = new Intent(getActivity(),
                        AccountsActivity.class);
                startActivity(intent);
            }
        } else {
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(false);
            }
            OauthUtils.showToast(getActivity(), getString(R.string.networkUnavailable));

        }
    }

    /**
     * 初始化微博列表类型
     */
    private void initStatusType() {
        // @我的微博
        if (mAtFlag != 0) {
            mType = STATUS_AT_ME;
        }
        // 用户界面微博
        else if (!TextUtils.isEmpty(mScreenName)) {
            mType = STATUS_USER;
        } else {
            switch (mTabPosition) {
                // 首页
                case 0:
                    mType = STATUS_HOME;
                    break;
                // 互相关注
                case 1:
                    mType = STATUS_EACH_OTHER;
                    break;
                // 我的微博
                case 2:
                    mType = STATUS_BY_ME;
                    break;
                // 收藏
                case 3:
                    mType = STATUS_FAVORITES;
                    break;
                // 附近微博
                case 4:
                    mType = STATUS_NEARBY;

                    break;
                default:
                    mType = STATUS_HOME;
                    break;
            }
        }
    }

    /**
     * 请求网络，获取用户时间线微博列表
     */
    private void getUserTimeline() {
        if (checkNetwork()) {

            // @我的微博
            if (mAtFlag != 0) {
                mType = STATUS_AT_ME;
                mSinceId = mStatusDao.getSinceId(STATUS, mType);
                mStatusesAPI = new StatusesAPI(mAccessToken);
                mStatusesAPI.mentions(0, 0, 100, 1,
                        StatusesAPI.AUTHOR_FILTER_ALL,
                        StatusesAPI.SRC_FILTER_ALL,
                        StatusesAPI.FEATURE_ALL, false, requestListener);
            }
            // 用户界面微博
            else if (!TextUtils.isEmpty(mScreenName)) {
                mStatusesAPI = new StatusesAPI(mAccessToken);

                mStatusesAPI.userTimeline(mScreenName, 0, 0, 50, 1, false,
                        StatusesAPI.FEATURE_ALL, false, requestListener);
            } else {
                switch (mTabPosition) {
                    // 首页
                    case 0:
                        mType = STATUS_HOME;
                        mSinceId = mStatusDao.getSinceId(STATUS, mType);
                        mStatusesAPI = new StatusesAPI(mAccessToken);
                        mStatusesAPI.friendsTimeline(mSinceId, mMaxId, 50, 1,
                                false, StatusesAPI.FEATURE_ALL, false,
                                requestListener);
                        break;
                    // 互相关注
                    case 1:
                        mType = STATUS_EACH_OTHER;
                        mSinceId = mStatusDao.getSinceId(STATUS, mType);
                        mStatusesAPI = new StatusesAPI(mAccessToken);
                        mStatusesAPI.bilateralTimeline(0, 0, 50, 1, false,
                                StatusesAPI.FEATURE_ALL, true,
                                requestListener);
                        break;
                    // 我的微博
                    case 2:
                        mType = STATUS_BY_ME;
                        mUid = getUserID();
                        mSinceId = mStatusDao.getSinceId(STATUS, mType);
                        mStatusesAPI = new StatusesAPI(mAccessToken);
                        mStatusesAPI.userTimeline(mUid, 0, mMaxId, 50,
                                1, false, StatusesAPI.FEATURE_ALL, false,
                                requestListener);
                        break;
                    // 收藏
                    case 3:
                        mType = STATUS_FAVORITES;
                        mSinceId = mStatusDao.getSinceId(STATUS, mType);
                        mFavoriteAPI = new FavoritesAPI(mAccessToken);
                        mFavoriteAPI.favorites(50, 1, requestListener);
                        break;
                    // 附近微博
                    case 4:
                        mType = STATUS_NEARBY;
                        mSinceId = mStatusDao.getSinceId(STATUS, mType);
                        mPlaceAPI = new PlaceAPI(mAccessToken);
                        getLocationDegrees();
                        long now = System.currentTimeMillis();
                        mPlaceAPI.nearbyTimeline(String.valueOf(mLatitude),
                                String.valueOf(mLongitude),
                                Constants.GPS_RANGE, 0, now,
                                PlaceAPI.SORT_BY_DISTENCE, 50, 1, false, false,
                                requestListener);

                        break;
                    default:
                        mType = STATUS_HOME;
                        mSinceId = mStatusDao.getSinceId(STATUS, mType);
                        mStatusesAPI = new StatusesAPI(mAccessToken);
                        mStatusesAPI.friendsTimeline(mSinceId, mMaxId, 50, 1,
                                false, StatusesAPI.FEATURE_ALL, false,
                                requestListener);
                        break;
                }
            }
        } else {
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(false);
            }
            OauthUtils.showToast(getActivity(), getString(R.string.networkUnavailable));
        }
    }

    @Override
    public void onRefresh() {
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            getUserTimeline();
        } else {
            getTimeLine();
        }
    }

    /**
     * 获取经纬度
     */
    public void getLocationDegrees() {

        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {

            }

            // Provider被enable时触发此函数，比如GPS被打开
            @Override
            public void onProviderEnabled(String provider) {

            }

            // Provider被disable时触发此函数，比如GPS被关闭
            @Override
            public void onProviderDisabled(String provider) {

            }

            // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    Log.e("Map",
                            "Location changed : Lat: " + location.getLatitude()
                                    + " Lng: " + location.getLongitude());
                }
            }
        };
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        Location location = locationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            mLatitude = location.getLatitude(); // 经度
            mLongitude = location.getLongitude(); // 纬度
        }

    }

    /**
     * 微博 Adapter
     */
    public class StatusAdapter extends RecyclerView.Adapter<StatusViewHolder> {

        @Override
        public StatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.status_layout, parent, false);
            StatusViewHolder holder = new StatusViewHolder(view);
            initToken();
            holder.setContext(getActivity(), mAccessToken, MainTimeLineFragment.this);
            return holder;
        }

        @Override
        public void onBindViewHolder(StatusViewHolder holder, int position) {
            holder.buildStatusItemView(mStatusList.get(position), getResources().getColor(R.color.grey_400));
        }

        @Override
        public int getItemCount() {
            return mStatusList.size();
        }
    }
    class StatusRequestListener implements RequestListener {

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(getActivity(), info.error == null ?
                    getString(R.string.networkUnavailable) : getString(R.string.loadFailed) + info.toString(), Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                AsyncSave2DBTask task = new AsyncSave2DBTask();
                task.setContext(getActivity());
                if (response.startsWith("{\"statuses\"")) {
                    task.execute(STATUS, mType, response);
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response);
                    if (statuses.statusList != null
                            && statuses.statusList.size() > 0) {
                        // 收藏列表由于没有mSinceId,每次都会返回50条数据，因此add会导致重复
                        if (mStatusList != null && mStatusList.size() > 0
                                && mTabPosition != 3) {
                            // for (Status status : statuses.statusList) {
                            for (int i = statuses.statusList.size() - 1; i >= 0; i--) {
                                mStatusList.add(0, statuses.statusList.get(i));
                            }
                        } else {
                            mStatusList = statuses.statusList;
                        }

                    }
                    if (mStatusList != null && mStatusList.size() > 0) {
                        mMaxId = Long.parseLong(mStatusList.get(mStatusList
                                .size() - 1).id);
                        mSinceId = Long.parseLong(mStatusList.get(0).id);
                    }
                    mStatusAdapter.notifyDataSetChanged();
                }
                if (response.startsWith("{\"favorites\"")) {
                    task.execute(STATUS, mType, response);
                    mStatusList.clear();
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    FavoriteList favoriteList = FavoriteList.parse(response);
                    if (favoriteList.favoriteList != null
                            && favoriteList.favoriteList.size() > 0) {
                        // for (Favorite status : favoriteList.favoriteList) {
                        for (int i = favoriteList.favoriteList.size() - 1; i >= 0; i--) {
                            if (favoriteList.favoriteList.get(i).status.user != null)
                                mStatusList.add(0,
                                        favoriteList.favoriteList.get(i).status);
                        }
                    }
                    if (mStatusList != null && mStatusList.size() > 0) {
                        mMaxId = Long.parseLong(mStatusList.get(mStatusList
                                .size() - 1).id);
                        mSinceId = Long.parseLong(mStatusList.get(0).id);
                    }
                    mStatusAdapter.notifyDataSetChanged();
                }
            }
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(false);
            }
        }
    }

}
