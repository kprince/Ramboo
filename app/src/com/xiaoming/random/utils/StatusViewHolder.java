package com.xiaoming.random.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.FavoritesAPI;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.activities.GalleryActivity;
import com.xiaoming.random.activities.LineDetailActivity;
import com.xiaoming.random.activities.SendWeiboActivity;
import com.xiaoming.random.activities.UserProfileActivity;
import com.xiaoming.random.fragments.MainTimeLineFragment;
import com.xiaoming.random.fragments.UserProfileFragment;
import com.xiaoming.random.model.Status;
import com.xiaoming.random.model.WeiboUser;
import com.xiaoming.random.widgets.ClickPreventableTextView;

import java.util.ArrayList;
import java.util.List;

public class StatusViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
    protected static final String SCREEN_NAME = "SCREEN_NAME";
    private static final String TAG = "StatusViewHolder";
    private static final String EXPORT_SUCCESS = "EXPORT_SUCCESS";
    private static Oauth2AccessToken mToken;
    TextView postNickName, statusCreateAt;
    ImageView userImage, statusImage, repostStatusImage;
    View repostFlag;
    GridView imageGrid, repostImageGrid;
    ClickPreventableTextView statusTextView;
    TextView repostStatus;
    private Context mContext;
    private List<String> mImageList = new ArrayList<>();
    private List<String> mRepostImageList = new ArrayList<>();
    private ArrayList<String> mUriList = new ArrayList<>();
    private StatusImageGridAdapter mImageGridAdapter = new StatusImageGridAdapter();
    private RepostImageGridAdapter mRepostGridAdapter = new RepostImageGridAdapter();
    private GridItemClickListener mGridImageClick = new GridItemClickListener();
    private ImageClickListener mImageClick = new ImageClickListener();
    private ButtonFlat mRepostIt, mCommentIt, mExportIt, mFavoriteIt;
    private CardView mCard;
    private Handler mHandler;
    private LinearLayout mRepostWrapper;
    private MainTimeLineFragment mFragment;


    public StatusViewHolder(View itemView) {
        super(itemView);
        initAllItem(itemView);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                boolean success = msg.getData().getBoolean(EXPORT_SUCCESS);
                if (!success) {
                    OauthUtils.showToast(mContext, getString(R.string.exportSucceed));
                    return success;
                }
                OauthUtils.showToast(mContext, getString(R.string.exportFailed));
                return success;
            }
        });

    }

    public void setContext(Context context, Oauth2AccessToken token, MainTimeLineFragment frag) {
        mContext = context;
        mToken = token;
        mFragment = frag;
    }

    /**
     * 初始化所有控件
     *
     * @param view 布局文件view
     */
    public void initAllItem(View view) {
        mCard = (CardView) view.findViewById(R.id.card_view);
        statusTextView = (ClickPreventableTextView) view.findViewById(R.id.status);
        userImage = (ImageView) view.findViewById(R.id.userImage);
        postNickName = (TextView) view.findViewById(R.id.userNickName);
        mRepostIt = (ButtonFlat) view.findViewById(R.id.repost_it);
        mCommentIt = (ButtonFlat) view.findViewById(R.id.comment_it);
        mExportIt = (ButtonFlat) view.findViewById(R.id.export_it);
        mFavoriteIt = (ButtonFlat) view.findViewById(R.id.favorite_it);

        statusCreateAt = (TextView) view.findViewById(R.id.statusCreateAt);
        repostStatus = (TextView) view.findViewById(R.id.repostStatus);
        statusImage = (ImageView) view.findViewById(R.id.statusImage);
        repostFlag = view.findViewById(R.id.repostFlag);
        repostStatusImage = (ImageView) view.findViewById(R.id.repostStatusImage);
        imageGrid = (GridView) view.findViewById(R.id.statusImageGrid);
        repostImageGrid = (GridView) view.findViewById(R.id.repostImageGrid);
        imageGrid.setAdapter(mImageGridAdapter);
        repostImageGrid.setAdapter(mRepostGridAdapter);
        imageGrid.setOnItemClickListener(mGridImageClick);
        repostImageGrid.setOnItemClickListener(mGridImageClick);
        statusImage.setOnClickListener(mImageClick);
        repostStatusImage.setOnClickListener(mImageClick);


        mRepostWrapper = (LinearLayout) view.findViewById(R.id.ret_status_wrapper);
        mRepostWrapper.setOnClickListener(this);

        mRepostIt.setOnClickListener(this);
        mCommentIt.setOnClickListener(this);
        mExportIt.setOnClickListener(this);
        mFavoriteIt.setOnClickListener(this);
        userImage.setOnClickListener(this);
        postNickName.setOnClickListener(this);
        statusTextView.setOnClickListener(this);

        repostStatus.setMovementMethod(LinkMovementMethod.getInstance());

//        LinearLayout statusHeader = (LinearLayout) view.findViewById(R.id.status_header);
//        statusHeader.setOnClickListener(this);

        // repostStatus.setOnClickListener(this);
    }


    /**
     * 渲染微博
     *
     * @param status 微博对象
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void buildStatusItemView(final Status status, int color) {
        mRepostIt.setBackgroundColor(color);
        mCommentIt.setBackgroundColor(color);
        mExportIt.setBackgroundColor(color);
        mFavoriteIt.setBackgroundColor(color);
        mRepostIt.setBackground(mContext.getResources().getDrawable(R.drawable.left_bottom_radius));
        mExportIt.setBackground(mContext.getResources().getDrawable(R.drawable.right_bottom_radius));
        if (status.user != null) {
            // 用户
            postNickName.setText(status.user.name);
            ImageLoader.getInstance().displayImage(status.user.avatarLarge,
                    userImage, BaseActivity.UIL_OPTIONS);
            // 微博内容
            StatusUtils.dealStatusText(mContext, statusTextView, status.text, null);
            //statusTextView.setText(status.text);
//            OauthUtils.doLinkify(statusTextView);
            statusTextView.setTag(status);
            statusCreateAt.setText(TimeUtils
                    .parseTime(status.createdAt)
                    + "  "
                    + OauthUtils.splitAndFilterString(status.source));
            // 微博图片 首先判断pic_urls是否为空，如果为空则不显示图片grid
            if (status.pic_urls != null && status.pic_urls.size() > 1) {
                statusImage.setVisibility(View.GONE);
                imageGrid.setVisibility(View.VISIBLE);
                mImageList = status.pic_urls;
                imageGrid.setTag(mImageList);
                mImageGridAdapter.notifyDataSetChanged();
            } else {
                imageGrid.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(status.bmiddle)) {
                    //加标记
                    setUpViewTag(statusImage, status);
                    ImageLoader.getInstance().displayImage(status.bmiddle,
                            statusImage, BaseActivity.UIL_OPTIONS);
                    statusImage.setVisibility(View.VISIBLE);
                } else {
                    statusImage.setVisibility(View.GONE);
                }
            }
            // 转发评论
            if (status.repostCount > 0) {
                mRepostIt.setText(Utils.formatNum(status.repostCount) + getString(R.string.repost));
            }
            if (status.commentCount > 0) {
                mCommentIt.setText(Utils.formatNum(status.commentCount) + getString(R.string.comment));
            }
            if (status.favorited)
                mFavoriteIt.setText(getString(R.string.cancelFavor));
            mRepostIt.setTag(status);
            userImage.setTag(status.user);
            postNickName.setTag(status.user);
            mCommentIt.setTag(status.id);
            mFavoriteIt.setTag(status.id);
            mExportIt.setTag(status.id);
            mCard.setTag(status.id);
            statusTextView.setTag(status);

        } else {
            // 微博已删除
            statusCreateAt.setText("");
            postNickName.setText("");
            userImage.setImageBitmap(null);
            statusImage.setImageBitmap(null);
            StatusUtils.dealStatusText(mContext, statusTextView, status.text, null);
//            OauthUtils.doLinkify(statusTextView);
        }
        Status retStatus = status.repostStatus;
        // 转发的原微博
        if (retStatus != null) {
            // 微博内容
            if (!TextUtils.isEmpty(retStatus.text)) {
                try {
                    StatusUtils.dealStatusText(mContext, repostStatus, retStatus.text, retStatus.user.name);
                } catch (Exception e) {
                    repostStatus.setText(retStatus.text);
                }
                // 图片
                if (retStatus.pic_urls != null
                        && retStatus.pic_urls.size() > 1) {
                    repostImageGrid.setVisibility(View.VISIBLE);
                    repostStatusImage.setVisibility(View.GONE);
                    mRepostImageList = retStatus.pic_urls;
                    repostImageGrid.setTag(mRepostImageList);
                    mRepostGridAdapter.notifyDataSetChanged();
                } else {
                    setUpViewTag(repostStatusImage, retStatus);
                    repostImageGrid.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(retStatus.bmiddle)) {
                        ImageLoader.getInstance().displayImage(retStatus.bmiddle, repostStatusImage
                                , BaseActivity.UIL_OPTIONS);
                        repostStatusImage.setVisibility(View.VISIBLE);
                    } else {
                        repostStatusImage.setVisibility(View.GONE);
                    }
                }
                repostStatus.setVisibility(View.VISIBLE);
                mRepostWrapper.setTag(status.repostStatus);
                repostFlag.setVisibility(View.VISIBLE);
            }
        } else {
            repostStatus.setVisibility(View.GONE);
            repostStatusImage.setVisibility(View.GONE);
            repostFlag.setVisibility(View.GONE);
            repostImageGrid.setVisibility(View.GONE);
        }
    }

    private void setUpViewTag(ImageView view, Status status) {
        if (!TextUtils.isEmpty(status.bmiddle)) {
            view.setTag(status.bmiddle);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.repost_it:
                repostStatus(v);
                break;
            case R.id.comment_it:
                commentStatus(v);
                break;
            case R.id.export_it:
                saveWeiboCard();
                break;
            case R.id.favorite_it:
                favoritesStatus(v);
                break;
            case R.id.userNickName:
                showUserProfile(v);
                break;
            case R.id.userImage:
                showUserProfile(v);
                break;
            case R.id.status:
                showStatusDetail(v);
                break;
            case R.id.ret_status_wrapper:
                showStatusDetail(v);
                break;
            default:
                break;
        }

    }

    //导出微博Card
    private void saveWeiboCard() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message m = new Message();
                Bundle b = new Bundle();
                try {
                    FileUtils.convertView2Image(mCard);
                    b.putBoolean(EXPORT_SUCCESS, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    b.putBoolean(EXPORT_SUCCESS, true);
                }
                m.setData(b);
                mHandler.sendMessage(m);
            }
        }).start();
    }

    /**
     * 收藏微博
     *
     * @param v
     */
    private void favoritesStatus(View v) {
        long id = Long.parseLong((String) v.getTag());
        FavoritesAPI favoritesAPI = new FavoritesAPI(mToken);
        String text = ((ButtonFlat) v).getText();
        if (text.equals("收藏"))
            favoritesAPI.create(id, new FavoritesListener());
        else
            favoritesAPI.destroy(id, new FavoritesListener());
    }

    public final String getString(int id) {
        return mContext.getString(id);
    }

    /**
     * 跳转到微博详情页
     *
     * @param v
     */
    private void showStatusDetail(View v) {
        if (mContext instanceof LineDetailActivity)
            return;
        if (v.getTag() == null)
            return;
        Intent intent = new Intent(mContext, LineDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainTimeLineFragment.STATUS, (Status) v.getTag());
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    /**
     * 跳转到用户资料页
     *
     * @param v
     */
    private void showUserProfile(View v) {
        WeiboUser user = (WeiboUser) v.getTag();
        if (user == null)
            return;
        Intent intent = new Intent(mContext, UserProfileActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(UserProfileFragment.USER, user);
        intent.putExtras(extras);
        mContext.startActivity(intent);
    }

    /**
     * 评论微博
     *
     * @param v
     */
    private void commentStatus(View v) {
        long id = Long.parseLong((String) v.getTag());
        Intent intent2 = new Intent(mContext, SendWeiboActivity.class);
        intent2.putExtra(SendWeiboActivity.SEND_WEIBO_ID, id);
        intent2.putExtra(SendWeiboActivity.SEND_WEIBO_TYPE, 2);
        mContext.startActivity(intent2);
    }

    /**
     * 转发微博
     *
     * @param v
     */
    private void repostStatus(View v) {
        Status status = (Status) v.getTag();
        Intent intent = new Intent(mContext, SendWeiboActivity.class);
        intent.putExtra(SendWeiboActivity.SEND_WEIBO_ID, Long.parseLong(status.id));
        intent.putExtra(SendWeiboActivity.SEND_WEIBO_TYPE, 1);
        intent.putExtra(SendWeiboActivity.SEND_WEIBO_TXT,
                status.repostStatus != null ? "//@" + status.user.name + "：" + status.text : getString(R.string.repostWeibo));
        mContext.startActivity(intent);
    }

    class FavoritesListener implements RequestListener {

        @Override
        public void onComplete(String s) {
            OauthUtils.showToast(mContext, getString(R.string.operationSucceed));
            String text = mFavoriteIt.getText();
            if (text.equals(getString(R.string.favorites)))
                mFavoriteIt.setText(getString(R.string.cancelFavor));
            else
                mFavoriteIt.setText(getString(R.string.favorites));
            if (mFragment != null)
                mFragment.refresh();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Log.e(TAG, e.getMessage());
            OauthUtils.showToast(mContext, getString(R.string.operationFailed) + e.getLocalizedMessage());
        }
    }

    public class RepostImageGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int count = 0;
            if (mRepostImageList != null && mRepostImageList.size() > 0) {
                count = mRepostImageList.size();
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return mRepostImageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;
            if (convertView == null) {
                image = new ImageView(mContext);
                image.setLayoutParams(new GridView.LayoutParams(150, 150));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setPadding(5, 5, 5, 5);
            } else {
                image = (ImageView) convertView;
            }
            if (mRepostImageList != null && mRepostImageList.size() > 0) {
                String uri = mRepostImageList.get(position);
                ImageLoader.getInstance().displayImage(uri, image, BaseActivity.UIL_OPTIONS);
                StatusUtils.toggleTagOnImageView(image, uri);
            }
            return image;
        }

    }

    public class StatusImageGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int count = 0;
            if (mImageList != null && mImageList.size() > 0) {
                count = mImageList.size();
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return mImageList.get(position);
        }

        @Override
        public long getItemId(int position) {

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;
            if (convertView == null) {
                image = new ImageView(mContext);
                image.setLayoutParams(new GridView.LayoutParams(150, 150));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setPadding(5, 5, 5, 5);
            } else {
                image = (ImageView) convertView;
            }
            if (mImageList != null && mImageList.size() > 0) {
                String uri = mImageList.get(position);
                ImageLoader.getInstance().displayImage(uri, image, BaseActivity.UIL_OPTIONS);
                StatusUtils.toggleTagOnImageView(image, uri);
            }
            return image;
        }

    }

    public class ImageClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            String uri = (String) v.getTag();
            if (mUriList != null && mUriList.size() > 0) {
                mUriList.clear();
            }
            mUriList.add(uri);
            if (mUriList != null && mUriList.size() > 0) {
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putStringArrayListExtra("uriList", mUriList);
                intent.putExtra("position", 0);
                intent.putExtra("multi", false);
                mContext.startActivity(intent);
            }
        }

    }

    public class GridItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mUriList = (ArrayList<String>) parent.getTag();
            if (mUriList != null && mUriList.size() > 0) {
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putStringArrayListExtra("uriList", mUriList);
                intent.putExtra("position", position);
                intent.putExtra("multi", true);
                mContext.startActivity(intent);
            }
        }
    }
}
