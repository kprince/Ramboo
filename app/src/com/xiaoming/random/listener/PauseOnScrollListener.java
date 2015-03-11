package com.xiaoming.random.listener;

import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * RecyclerView 互动时，暂停加载图片
 * Created by XM on 2015/3/10.
 */
public class PauseOnScrollListener extends RecyclerView.OnScrollListener {
    private ImageLoader mImageLoader;
    private boolean mPauseOnScroll;

    public PauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll) {
        mImageLoader = imageLoader;
        mPauseOnScroll = pauseOnScroll;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                mImageLoader.resume();
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (mPauseOnScroll)
                    mImageLoader.pause();
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                break;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }
}
