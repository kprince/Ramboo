package com.xiaoming.random.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gc.materialdesign.views.ButtonFloat;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.xiaoming.random.R;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.utils.FileUtils;
import com.xiaoming.random.utils.OauthUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SendWeiboActivity extends BaseActivity implements View.OnClickListener {
    public static final String EMOTIONS_EXISTS = "EMOTIONS_EXISTS";
    public static final String SEND_WEIBO_TYPE = "SEND_WEIBO_TYPE";//发送微博类型 0:发微博，1：转发，2：评论
    public static final String SEND_WEIBO_TXT = "SEND_WEIBO_TXT";//发送微博内容
    public static final String SEND_WEIBO_ID = "SEND_WEIBO_ID";//转发评论微博ID
    public static final String RANDOM_TMP_PIC = "/Random/tmp/pic";
    private static final int TAKE_PHOTO = 1;//拍一张照片
    private static final int CROP_PHOTO = 2;//裁剪照片
    private static final int TYPE_SEND = 0;//裁剪照片
    private static final int TYPE_REPOST = 1;//转发微博
    private static final int TYPE_COMMENT = 2;//发送评论
    private int mType = 0;//默认为发送微博
    private ImageView mPicture, mCamera, mAtFriends, mEmotions, mStatusImage;
    private Toolbar mToolbarTop;
    private EditText mStatusContent;
    private Uri mImageUri;
    private GridView mEmotionsGrid;
    private List<String> mEmotionsUrlList;
    private EmotionsGridAdapter mAdapter;
    private RelativeLayout mEmotionsGridLayout;
    private Oauth2AccessToken mToken;
    private StatusesAPI mStatusApi;
    private CommentsAPI mCommentApi;
    private ButtonFloat mSendButton;
    private long mStatusId = 0;
    public static DisplayImageOptions UIL_OPTIONS = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.ic_error)
            .showImageOnLoading(R.drawable.ic_tag_faces_grey600_48dp)
            .resetViewBeforeLoading(true).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
            .displayer(new FadeInBitmapDisplayer(300)).build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_weibo);
        findViews();
        initArguments(savedInstanceState);
        setToolbarTop();
        new Thread(new initAPIThread()).start();
    }

    /**
     * 初始化所有参数
     *
     * @param savedInstanceState
     */
    private void initArguments(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(SEND_WEIBO_TYPE, 0);
            mStatusId = savedInstanceState.getLong(SEND_WEIBO_ID, 0);
            mStatusContent.setText(savedInstanceState.getString(SEND_WEIBO_TXT, ""));
        } else {
            if (getIntent().getExtras() != null)
                mType = getIntent().getExtras().getInt(SEND_WEIBO_TYPE, 0);
        }
        if (mType != 0) {
            mStatusId = getIntent().getExtras().getLong(SEND_WEIBO_ID, 0);
            mStatusContent.setText(getIntent().getExtras().getString(SEND_WEIBO_TXT, ""));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mStatusContent != null)
            outState.putString(SEND_WEIBO_TXT, mStatusContent.getText().toString());
        outState.putInt(SEND_WEIBO_TYPE, mType);
        outState.putLong(SEND_WEIBO_ID, mStatusId);
        super.onSaveInstanceState(outState);
    }

    /**
     * 设置顶部Toolbar
     */
    private void setToolbarTop() {
        switch (mType) {
            case TYPE_SEND:
                mToolbarTop.setTitle(getString(R.string.sendWeibo));
                break;
            case TYPE_COMMENT:
                mToolbarTop.setTitle(getString(R.string.sendComment));
                break;
            case TYPE_REPOST:
                mToolbarTop.setTitle(getString(R.string.repostWeibo));
                break;
            default:
                mToolbarTop.setTitle(getString(R.string.sendWeibo));
                break;
        }
        mToolbarTop.setSubtitle(getUserName());
        setSupportActionBar(mToolbarTop);
    }


    private void initAccessToken() {
        if (checkNetwork()) {
            long uid = getUserID();
            if (uid > 0) {
                if (mToken == null) {
                    StatusDao dao = new StatusDao(this);
                    AuthUser user = dao.getAuthUser(uid);
                    mToken = new Oauth2AccessToken(user.token, user.expires);
                }
            }
        }
    }

    private void initAPI() {
        if (mToken != null && mToken.isSessionValid())
            mStatusApi = new StatusesAPI(mToken);
        mCommentApi = new CommentsAPI(mToken);
    }

    /**
     * 发送微博
     */
    private void send() {
        String s = "";
        if (mStatusContent.getText() != null)
            s = mStatusContent.getText().toString();
        if (TextUtils.isEmpty(s)) {
            OauthUtils.showToast(this, getString(R.string.contentNull));
            return;
        }
        switch (mType) {
            case TYPE_COMMENT:
                /**
                 * 当评论转发微博时，是否评论给原微博，0：否、1：是，默认为0。
                 */
                mCommentApi.create(s, mStatusId, false, new SendRequestListener());
                break;
            case TYPE_REPOST:
                /**
                 * 是否在转发的同时发表评论，0：否、1：评论给当前微博、2：评论给原微博、3：都评论，默认为0
                 */
                mStatusApi.repost(mStatusId, s, 2, new SendRequestListener());
                break;
            case TYPE_SEND:
                mStatusApi.update(s, null, null, new SendRequestListener());
                break;
        }

    }

    /**
     * 查找所有控件
     */
    private void findViews() {
        mToolbarTop = (Toolbar) findViewById(R.id.sendWeiboToolbar);
        mPicture = (ImageView) findViewById(R.id.send_weibo_picture);
        mCamera = (ImageView) findViewById(R.id.send_weibo_camera);
        mAtFriends = (ImageView) findViewById(R.id.send_weibo_at_friends);
        mEmotions = (ImageView) findViewById(R.id.send_weibo_emotions);
        mStatusContent = (EditText) findViewById(R.id.status_content);
        mEmotionsGrid = (GridView) findViewById(R.id.emotions_grid);
        mEmotionsGridLayout = (RelativeLayout) findViewById(R.id.emotions_grid_layout);
        mSendButton = (ButtonFloat) findViewById(R.id.send_it);
        mStatusImage = (ImageView) findViewById(R.id.send_weibo_image);

        mSendButton.setBackgroundColor(getColor(R.attr.colorAccent));

        mPicture.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_white_36dp));
        mCamera.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_white_36dp));
        mEmotions.setImageDrawable(getResources().getDrawable(R.drawable.ic_tag_faces_white_36dp));
        mAtFriends.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_add_white_36dp));

        mCamera.setOnClickListener(this);
        mPicture.setOnClickListener(this);
        mEmotions.setOnClickListener(this);
        mAtFriends.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        mStatusContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideEmotionsGrid();
            }
        });
    }

    private void hideEmotionsGrid() {
        if (mEmotionsGridLayout.getVisibility() == View.VISIBLE)
            mEmotionsGridLayout.setVisibility(View.GONE);
    }

    private void hideOrShowEmotionsGrid() {
        if (mEmotionsGridLayout.getVisibility() == View.VISIBLE) {
            mEmotionsGridLayout.setVisibility(View.GONE);
            return;
        }
        mEmotionsGridLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏软键盘
     * <p/>
     * For Open Keyboard :
     * InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
     * imm.showSoftInput(edtView, InputMethodManager.SHOW_IMPLICIT);
     * For Close/Hide Keyboard :
     * InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     * imm.hideSoftInputFromWindow(edtView.getWindowToken(), 0);
     */
    private void hideWindowSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mStatusContent.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_weibo_at_friends:
                OauthUtils.showToast(this, getString(R.string.smartAtNotAvailable));
                break;
            case R.id.send_weibo_emotions:
                hideWindowSoftInput();
                hideOrShowEmotionsGrid();
                showEmotions();
                break;
            case R.id.send_weibo_picture:
                selectPhotoFromGallery();
                break;
            case R.id.send_weibo_camera:
                takePhoto();
                break;
            case R.id.send_it:
                send();
        }
    }

    /**
     * 从图库选择一张照片
     */
    private void selectPhotoFromGallery() {
        String path = createDirs();
        File image = new File(path, System.currentTimeMillis() + ".jpg");
        if (image.exists())
            image.delete();
        try {
            image.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageUri = Uri.fromFile(image);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 创建文件目录
     *
     * @return 创建的目录
     */
    private String createDirs() {
        String path = Environment.getExternalStorageDirectory() + RANDOM_TMP_PIC;
        FileUtils.createDir(path);
        return path;
    }

    /**
     * 弹出表情选择框
     */
    private void showEmotions() {
        if (mAdapter == null) {
            StatusDao dao = new StatusDao(SendWeiboActivity.this);
            mEmotionsUrlList = dao.getEmotions();
            mAdapter = new EmotionsGridAdapter();
            mEmotionsGrid.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
        mEmotionsGrid.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mImageUri, "image/*")
                            .putExtra("scale", true)
                            .putExtra("return-data", false)
                            .putExtra("noFaceDetection", true)
                            .putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                            .putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mStatusImage.setImageBitmap(bitmap);
                    mStatusImage.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 拍摄照片
     */
    private void takePhoto() {
        String path = createDirs();
        File image = new File(path, System.currentTimeMillis() + ".jpg");
        if (image.exists())
            image.delete();
        try {
            image.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageUri = Uri.fromFile(image);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 初始化微博和评论API
     */
    class initAPIThread implements Runnable {
        @Override
        public void run() {
            initAccessToken();
            initAPI();
        }
    }

    class SendRequestListener implements RequestListener {

        @Override
        public void onComplete(String s) {
            if (s.startsWith("{\"created_at\"")) {
                OauthUtils.showToast(SendWeiboActivity.this, mType == 2 ? getString(R.string.comments)
                        : getString(R.string.status) + getString(R.string.sendSucceed));
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            OauthUtils.showToast(SendWeiboActivity.this, getString(R.string.sendFailed) + e.getMessage());
        }
    }

    /**
     * 表情选择框adapter
     */
    class EmotionsGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mEmotionsUrlList.size();
        }

        @Override
        public Object getItem(int position) {
            return mEmotionsUrlList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView view;
            if (convertView == null)
                view = (ImageView) View.inflate(SendWeiboActivity.this, R.layout.emotions_grid_item, null);
            else
                view = (ImageView) convertView;
            view.setLayoutParams(new GridView.LayoutParams(55, 55));
            String[] urlStr = mEmotionsUrlList.get(position).split(",");
            ImageLoader.getInstance().displayImage(urlStr[1], view, UIL_OPTIONS);
            view.setTag(urlStr[0]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phrase = v.getTag().toString();
                    mStatusContent.append(phrase);
                }
            });
            return view;
        }
    }
}