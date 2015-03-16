package com.xiaoming.random.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.utils.LogUtil;
import com.xiaoming.random.Constants;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.AccountsActivity;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.activities.UserProfileActivity;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.listener.PauseOnScrollListener;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.model.Comment;
import com.xiaoming.random.tasks.AsyncSave2DBTask;
import com.xiaoming.random.utils.OauthUtils;
import com.xiaoming.random.utils.StatusUtils;
import com.xiaoming.random.utils.TimeUtils;
import com.xiaoming.random.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class CommentsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String COMMENTS = "COMMENTS";
    private static final int COMMENTS_DEFAULT_LENGTH = 50;
    private static final String COMMENTS_TO_ME = "TO_ME";//首页
    private static final String COMMENTS_BY_ME = "BY_ME";//我的微博
    private static final String COMMENTS_AT_ME = "AT_ME";
    private static final String POSITION = "POSITION";
    protected final String TAG = "CommentsFragment";
    private int mTabPosition;
    private long mMaxId = 0;
    private long mSinceId = 0;
    private long mUid;
    private Oauth2AccessToken mAccessToken;
    private CommentsAPI mCommentsAPI;
    private List<Comment> mCommentList = new ArrayList<>();
    private RecyclerView mCommentsListView;
    private SwipeRefreshLayout swipeLayout;
    private CommentsListListener mCommentsListListener = new CommentsListListener();
    private CommentsListAdapter mCommentsListAdapter = new CommentsListAdapter();
    private String mType;
    private RecyclerView.LayoutManager mLayoutManager;

    public CommentsFragment() {

    }
    public static CommentsFragment newInstance(int position) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mTabPosition = savedInstanceState.getInt(POSITION);
        else
            mTabPosition = getArguments().getInt(POSITION);
        mSinceId = 0l;
        mMaxId = 0;
        initType();
    }
    @Override
    public boolean notifyDataSetChanged(Message msg) {
        int what = msg.what;
        if (what==1&&mCommentsListAdapter!=null)
            mCommentsListAdapter.notifyDataSetChanged();
        if (swipeLayout.isRefreshing())swipeLayout.setRefreshing(false);
        return true;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(POSITION, mTabPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void getCachedContent() {
        try {
            mSinceId = mDao.getSinceId(COMMENTS, mType);
            mCommentList = mDao.readComments(COMMENTS_DEFAULT_LENGTH, mType);
            if (mCommentList == null || mCommentList.size() <= 0) {
                getComments();
            } else {
                mHandler.sendEmptyMessage(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initType() {
        switch (mTabPosition) {
            //@我的微博
            case 0:
                break;
            //@我的评论
            case 1:
                mType = COMMENTS_AT_ME;
                mSinceId = mDao.getSinceId(COMMENTS, mType);
                break;
            //收到的评论
            case 2:
                mType = COMMENTS_TO_ME;
                mSinceId = mDao.getSinceId(COMMENTS, mType);
                break;
            //发出的评论
            case 3:
                mType = COMMENTS_BY_ME;
                mSinceId = mDao.getSinceId(COMMENTS, mType);
                break;
            default:
                mType = COMMENTS_AT_ME;
                mSinceId = mDao.getSinceId(COMMENTS, mType);
                break;
        }
    }

    private void getComments() {
        if (checkNetwork()) {
            mUid = getUserID();
            if (mUid > 0) {
                AuthUser user = mDao.getAuthUser(mUid);
                mAccessToken = new Oauth2AccessToken(user.token, user.expires);
                if (mAccessToken != null && mAccessToken.isSessionValid()) {
                    getUserComments();
                } else {
                    OauthUtils.showToast(getActivity(), getString(R.string.authExpired));
                    Intent oauthIntent = new Intent(getActivity(),
                            AccountsActivity.class);
                    startActivity(oauthIntent);
                }
            }
        }
    }

    private void getUserComments() {
        mCommentsAPI = new CommentsAPI(mAccessToken);
        switch (mTabPosition) {
            //@我的微博
            case 0:
                //mCommentsAPI.byME(mSinceId, mMaxId, 50, 1, CommentsAPI.SRC_FILTER_ALL, mCommentsListListener);
                break;
            //@我的评论
            case 1:
                mCommentsAPI.mentions(mSinceId, mMaxId, 50, 1, CommentsAPI.AUTHOR_FILTER_ALL, CommentsAPI.SRC_FILTER_ALL, mCommentsListListener);
                break;
            //收到的评论
            case 2:
                mCommentsAPI.toME(mSinceId, mMaxId, 50, 1, CommentsAPI.AUTHOR_FILTER_ALL, CommentsAPI.SRC_FILTER_ALL, mCommentsListListener);
                break;
            //发出的评论
            case 3:
                mCommentsAPI.byME(mSinceId, mMaxId, 50, 1, CommentsAPI.SRC_FILTER_ALL, mCommentsListListener);
                break;
            default:
                mCommentsAPI.byME(mSinceId, mMaxId, 50, 1, CommentsAPI.SRC_FILTER_ALL, mCommentsListListener);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Constants.DEVELOPER_MODE)
            Debug.startMethodTracing(TAG);
        mRootView = inflater.inflate(R.layout.comment_time_line_layout, container, false);
        swipeLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.comment_swipe_refresh);
        mCommentsListView = (RecyclerView) mRootView.findViewById(R.id.comment_time_line);
        mCommentsListView.setAdapter(mCommentsListAdapter);
        mCommentsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCommentsListView.setLayoutManager(mLayoutManager);
        swipeLayout.setOnRefreshListener(this);
        Utils.setSwipeRefreshColorSchema(swipeLayout);
        setRefreshing(swipeLayout,true);
        newGetCacheTask();
        if (Constants.DEVELOPER_MODE)
            Debug.stopMethodTracing();
        return mRootView;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        getCachedComments();
    }

    @Override
    public void onRefresh() {
        getComments();
    }

    public class CommentsListListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            AsyncSave2DBTask task = new AsyncSave2DBTask();
            task.setContext(getActivity());
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"comments\"")) {
                    task.execute(COMMENTS, mType, response);
                    Comment.CommentList commentList = Comment.parseList(response);
                    if (commentList != null && commentList.commentList != null) {
                        if (mCommentList != null && mCommentList.size() > 0) {
                            for (int i = commentList.commentList.size() - 1; i >= 0; i--) {
                                mCommentList.add(0, commentList.commentList.get(i));
                            }
                        } else {
                            mCommentList = commentList.commentList;
                        }
                    } else {
                        OauthUtils.showToast(getActivity(), getString(R.string.noRecentComments));
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
            OauthUtils.showToast(getActivity(), info.error == null ?
                    getString(R.string.networkUnavailable) : getString(R.string.loadFailed) + info.toString());
        }

    }

    class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.ViewHolder>
            implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String screenName = v.getTag().toString();
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra(SCREEN_NAME, screenName);
            startActivity(intent);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_layout, parent, false);
            return new ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            setUpView(holder, position);
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mCommentList != null && mCommentList.size() > 0) {
                count = mCommentList.size();
            }
            return count;
        }

        /**
         * 生成Comment对象
         *
         * @param holder
         * @param position
         */
        private void setUpView(ViewHolder holder, int position) {
            Comment comment = mCommentList.get(position);
            holder.userName.setText(comment.user.name);
            holder.userName.setTag(comment.user.name);
            holder.userImage.setTag(comment.user.name);
            StatusUtils.dealStatusText(getActivity(), holder.commentText, comment.text, null);
            // holder.commentText.setText(comment.text);
            ImageLoader.getInstance().displayImage(
                    comment.user.avatar_large, holder.userImage, BaseActivity.UIL_OPTIONS);
//            OauthUtils.doLinkify(holder.commentText);
            holder.commentCreateAt.setText(TimeUtils
                    .parseTime(comment.createdAt) + "  "
                    + OauthUtils.splitAndFilterString(comment.source));
            if (comment.replyComment != null) {
                if (!TextUtils.isEmpty(comment.replyComment.text)) {
                    try {
                        StatusUtils.dealStatusText(getActivity(), holder.repostStatusText, comment.replyComment.text, comment.replyComment.user.name);
//                        holder.repostStatusText.setText("@"
//                                + comment.reply_comment.user.name + ":"
//                                + comment.reply_comment.text);
                    } catch (Exception e) {
                        holder.repostStatusText.setText(comment.replyComment.text);
                    }
//                    OauthUtils.doLinkify(holder.repostStatusText);

                }
                holder.repostStatusText.setVisibility(View.VISIBLE);
                holder.repostFlag.setVisibility(View.VISIBLE);
            } else if (comment.status != null) {
                if (!TextUtils.isEmpty(comment.status.text)) {
                    try {
                        StatusUtils.dealStatusText(getActivity(), holder.repostStatusText, comment.status.text, comment.status.user.name);
                    } catch (Exception e) {
                        holder.repostStatusText.setText(comment.status.text);
                    }
//                    OauthUtils.doLinkify(holder.repostStatusText);
                    if (!TextUtils.isEmpty(comment.status.bmiddle)) {
                        ImageLoader.getInstance().displayImage
                                (comment.status.bmiddle, holder.repostImageView, BaseActivity.UIL_OPTIONS);
                        holder.repostImageView.setVisibility(View.VISIBLE);
                    } else {
                        holder.repostImageView.setVisibility(View.GONE);
                    }
                }
                holder.repostStatusText.setVisibility(View.VISIBLE);
                holder.repostFlag.setVisibility(View.VISIBLE);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView commentText;
            ImageView userImage;
            TextView userName;
            TextView commentCreateAt;
            TextView repostStatusText;
            ImageView repostImageView;
            View repostFlag;

            public ViewHolder(View itemView) {
                super(itemView);
                setupAll(itemView);
            }

            public void setupAll(View convertView) {
                commentCreateAt = (TextView) convertView.findViewById(R.id.statusCreateAt);
                commentText = (TextView) convertView.findViewById(R.id.commentsText);
                userImage = (ImageView) convertView.findViewById(R.id.userImage);
                userName = (TextView) convertView.findViewById(R.id.userNickName);
                repostFlag = convertView.findViewById(R.id.commentRepostFlag);
                repostStatusText = (TextView) convertView.findViewById(R.id.commentRepostStatus);
                repostImageView = (ImageView) convertView.findViewById(R.id.commentRepostStatusImage);

                userImage.setOnClickListener(CommentsListAdapter.this);
                userName.setOnClickListener(CommentsListAdapter.this);

            }
        }

    }

}