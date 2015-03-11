package com.xiaoming.random.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.xiaoming.random.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends BaseActivity {
    private ViewPager mPager;
    private TextView mImageCount;
    private List<View> mImageList = new ArrayList<View>();
    private List<String> mUriList = new ArrayList<String>();
    private PagerAdapter mAdapter;
    private ImageView mImage;
    private ImageView mGif;
    private ProgressBar mProgressBar;
    private int mPosition;
    private boolean mMultiImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> list = getIntent().getStringArrayListExtra("uriList");
        mPosition = getIntent().getIntExtra("position", 0);
        mMultiImage = getIntent().getBooleanExtra("multi", false);
        for (int i = 0; i < list.size(); i++) {
            String uri = list.get(i);
            mUriList.add(mMultiImage ? uri.replace("thumbnail", "large") : uri);
            View view = View.inflate(this, R.layout.image_layout, null);
            mImageList.add(view);
        }
        mAdapter = new GalleryPagerAdapter();
        setContentView(R.layout.imageview_pager);
        mImageCount = (TextView) findViewById(R.id.image_count);
        mImageCount.setText((mPosition + 1) + "/" + mUriList.size());
        mPager = (ViewPager) findViewById(R.id.image_container);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                mImageCount.setText((arg0 + 1) + "/" + (mUriList.size()));
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mPosition);
        mAdapter.notifyDataSetChanged();
    }

    public class GalleryPagerAdapter extends PagerAdapter {
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg1.equals(arg0);
        }

        @Override
        public int getCount() {
//			System.out.println(mImageList.size());
            return mImageList.size();
        }

        @Override
        public int getItemPosition(Object object) {

            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(mImageList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mImageList.get(position);
            mImage = (ImageView) view.findViewById(R.id.image_view);
            mGif = (ImageView) view.findViewById(R.id.gif_imageview);
//			if (mAttacher==null) {
//			 mAttacher = new PhotoViewAttacher(mImage);
//			}
            if (mUriList.get(position).substring(mUriList.get(position).length() - 3, mUriList.get(position).length()).equals("gif")) {
                ImageLoader.getInstance().displayImage(mUriList.get(position),
                        mGif, UIL_OPTIONS);
                mImage.setVisibility(View.GONE);
                mGif.setVisibility(View.VISIBLE);
            } else {

                ImageLoader.getInstance().displayImage(mUriList.get(position),
                        mImage, UIL_OPTIONS, new ImageLoadListener());
                // mAttacher.update();
                mImage.setVisibility(View.VISIBLE);
                mGif.setVisibility(View.GONE);
            }
            container.addView(view, 0);
            return view;
        }
    }

    public class ImageLoadListener implements ImageLoadingListener {

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {

        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
//			ViewGroup.LayoutParams params= getLayoutParams(arg2, getWindowManager().getDefaultDisplay().getWidth());
//			ImageView imageView = (ImageView) arg1.findViewById(R.id.image_view);
//			imageView.setLayoutParams(params);
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {

        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {

        }

    }

    public class ImageLoadProgressListener implements ImageLoadingProgressListener {

        @Override
        public void onProgressUpdate(String arg0, View arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub

        }

    }

//    public static ViewGroup.LayoutParams getLayoutParams(Bitmap bitmap, int screenWidth) {
//
//        float rawWidth = bitmap.getWidth();
//        float rawHeight = bitmap.getHeight();
//
//        float width = 0;
//        float height = 0;
//
//        Log.i("hello", "原始图片高度：" + rawHeight + "原始图片宽度：" + rawWidth);
//        Log.i("hello", "原始高宽比：" + (rawHeight / rawWidth));
//
//        if (rawWidth > screenWidth) {
//            height = (rawHeight / rawWidth) * screenWidth;
//            width = screenWidth;
//        } else {
//            width = rawWidth;
//            height = rawHeight;
//        }
//        Log.i("hello", "处理后图片高度：" + height + "处理后图片宽度：" + width);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) width, (int) height);
//
//        return layoutParams;
//    }
}
