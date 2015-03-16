package com.xiaoming.random.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.utils.LogUtil;
import com.xiaoming.random.R;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.fragments.MainTimeLineFragment;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.model.Comment;
import com.xiaoming.random.model.Status;
import com.xiaoming.random.model.WeiboUser;
import com.xiaoming.random.utils.OauthUtils;
import com.xiaoming.random.utils.StatusUtils;
import com.xiaoming.random.utils.StatusViewHolder;
import com.xiaoming.random.utils.TimeUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LineDetailActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener {
    private static String TAG = "TimeLineDetailActivity";
    private static String STA_COMMENTS = "STA_COMMENTS";
    private static int DEFAULT_LENGTH = 100;
    private ListView mCommentsListView;
    private Status mStatus;
    private List<Comment> mCommentList;
    private Oauth2AccessToken mAccessToken;
    private long mId;
    private CommentListAdapter mCommentListAdapter = new CommentListAdapter();
    private StatusDao mStatusDao;
    private long mSinceId;
    private SwipeRefreshLayout mRefresh;
    private FriendshipsAPI mFriendshipsApi;

    @Override
    public void onRefresh() {
        requestCommentsList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commentUserAvatar:
                showUserProfile(v);
                break;
            case R.id.commentUserName:
                showUserProfile(v);
                break;
            case R.id.comment_follow_user:
                WeiboUser user = (WeiboUser) v.getTag();
                ButtonFlat btn = (ButtonFlat) v;
                String follow = getResources().getString(R.string.follow);
                if (btn.getText().equals(follow)) {
                    mFriendshipsApi.create(Long.parseLong(user.id), user.screenName, new FriendsListener());
                } else {
                    mFriendshipsApi.destroy(Long.parseLong(user.id), user.screenName, new FriendsListener());
                }
                break;

        }
    }

    private void showUserProfile(View v) {
        String name = v.getTag().toString();
        Intent intent = new Intent(LineDetailActivity.this, UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.SCREEN_NAME, name);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.status_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.time_line_detail_bar);
        toolbar.setTitle(getString(R.string.weiboDetail));
        setSupportActionBar(toolbar);

        mRefresh = (SwipeRefreshLayout) findViewById(R.id.time_line_detail_refresh);
        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeResources(
                R.color.red_a400,
                R.color.green_a400,
                R.color.light_blue_a400,
                R.color.pink_a400,
                R.color.teal_a400, R.color.purple_a400,
                R.color.indigo_a400
        );

        mStatus = (Status) getIntent().getExtras().get(MainTimeLineFragment.STATUS);
        mId = Long.parseLong(mStatus.mid);
        mStatusDao = new StatusDao(this);
        setUpListView();
        getCachedComments();
    }

    private void getCachedComments() {
        try {
            mCommentList = mStatusDao.readStaComments(DEFAULT_LENGTH, mId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCommentList != null && mCommentList.size() > 0) {
            mCommentListAdapter.notifyDataSetChanged();
        } else {
            mSinceId = mStatusDao.getStaCommSinId(STA_COMMENTS, mId);
            requestCommentsList();
        }
    }

    private void initToken() {
        if (mAccessToken == null) {
            long uid = getUserID();
            if (uid > 0) {
                StatusDao dao = new StatusDao(this);
                AuthUser user = dao.getAuthUser(uid);
                mAccessToken = new Oauth2AccessToken(user.token, user.expires);
            }
        }
        if (mAccessToken.isSessionValid()) {
            mFriendshipsApi = new FriendshipsAPI(mAccessToken);
        } else {
            OauthUtils.showToast(this, getString(R.string.authExpired));
            Intent oauthIntent = new Intent(this,
                    AccountsActivity.class);
            startActivity(oauthIntent);
        }
    }

    private void requestCommentsList() {
        if (checkNetwork()) {
            initToken();
            CommentsAPI commentsAPI = new CommentsAPI(mAccessToken);
            commentsAPI.show(mId, mSinceId, 0, 30, 1, 0, new RequestListener() {
                @Override
                public void onWeiboException(WeiboException e) {
                    LogUtil.e(TAG, e.getMessage());
                    ErrorInfo info = ErrorInfo.parse(e.getMessage());
                    Toast.makeText(LineDetailActivity.this, info.toString(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onComplete(String response) {
                    if (!TextUtils.isEmpty(response)) {
                        if (response.startsWith("{\"comments\"")) {
                            Comment.CommentList commentList = Comment.parseList(response);
                            mStatusDao.saveStaComments(response, mId);
                            if (mCommentList != null && mCommentList.size() > 0) {
                                for (int i = commentList.commentList.size() - 1; i >= 0; i--) {
                                    mCommentList.add(0, commentList.commentList.get(i));
                                }
                            } else {
                                mCommentList = commentList.commentList;
                            }
                        }
                    }
                    mCommentListAdapter.notifyDataSetChanged();
                }
            });
            mCommentListAdapter.notifyDataSetChanged();
            if (mRefresh != null) mRefresh.setRefreshing(false);


        } else {
            OauthUtils.showToast(this, getString(R.string.networkUnavailable));
        }
    }

    private void setUpListView() {
        mCommentsListView = (ListView) findViewById(R.id.commentsList);
        View headerView = View.inflate(this, R.layout.status_layout, null);
        StatusViewHolder holder = new StatusViewHolder(headerView);
        initToken();
        holder.setContext(this, mAccessToken, null);
        holder.buildStatusItemView(mStatus, getColor(R.attr.colorPrimary));
        mCommentsListView.addHeaderView(headerView);
        mCommentsListView.setHeaderDividersEnabled(false);
        mCommentsListView.setAdapter(mCommentListAdapter);
    }

    class ViewHolder {
        CircleImageView mCommentUserAvartar;
        TextView mCommentUserName, mCommentCreateAt, mCommentText;
        ButtonFlat mFollowBtn;
    }

    class FriendsListener implements RequestListener {

        @Override
        public void onComplete(String s) {
            OauthUtils.showToast(LineDetailActivity.this, getString(R.string.operationSucceed));
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Log.e(TAG, e.getMessage());
            OauthUtils.showToast(LineDetailActivity.this, getString(R.string.operationFailed) + e.getLocalizedMessage());
        }
    }

    class CommentListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int count = 0;
            if (mCommentList != null) {
                count = mCommentList.size();
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return mCommentList.get(position);
        }

        @Override
        public long getItemId(int position) {

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            Comment comment = mCommentList.get(position);
            if (convertView != null && convertView.getTag() != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(LineDetailActivity.this, R.layout.comment_layout, null);
                holder = new ViewHolder();
                holder.mCommentCreateAt = (TextView) convertView.findViewById(R.id.commentCreateAt);
                holder.mCommentText = (TextView) convertView.findViewById(R.id.commentText);
                holder.mCommentUserAvartar = (CircleImageView) convertView.findViewById(R.id.commentUserAvatar);
                holder.mCommentUserName = (TextView) convertView.findViewById(R.id.commentUserName);
                holder.mFollowBtn = (ButtonFlat) convertView.findViewById(R.id.comment_follow_user);
                holder.mCommentUserAvartar.setOnClickListener(LineDetailActivity.this);
                holder.mCommentUserName.setOnClickListener(LineDetailActivity.this);

                holder.mFollowBtn.setBackgroundColor(getColor(R.attr.colorAccent));
                holder.mFollowBtn.setOnClickListener(LineDetailActivity.this);

                ViewGroup.LayoutParams lp = holder.mCommentUserAvartar.getLayoutParams();
                lp.width = 100;
                lp.height = 100;
                holder.mCommentUserAvartar.setLayoutParams(lp);
                convertView.setTag(holder);
            }
            if (comment != null) {
                holder.mCommentUserName.setText(comment.user.name);
                ImageLoader.getInstance().displayImage(comment.user.avatar_large, holder.mCommentUserAvartar, UIL_OPTIONS);
//                holder.mCommentText.setText(comment.text);
                StatusUtils.dealStatusText(LineDetailActivity.this, holder.mCommentText, comment.text, null);
                holder.mCommentCreateAt.setText(TimeUtils
                        .parseTime(comment.createdAt) + "  "
                        + OauthUtils.splitAndFilterString(comment.source));
//                OauthUtils.doLinkify(holder.mCommentText);
                if (comment.user.following) {
                    holder.mFollowBtn.setText(getString(R.string.cancelFollow));
                } else
                    holder.mFollowBtn.setText(getString(R.string.follow));
                holder.mCommentUserAvartar.setTag(comment.user.name);
                holder.mCommentUserName.setTag(comment.user.name);
                holder.mFollowBtn.setTag(comment.user);
            }
            return convertView;
        }
    }
}
