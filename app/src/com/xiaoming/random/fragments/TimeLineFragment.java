package com.xiaoming.random.fragments;

import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.xiaoming.random.Constants;
import com.xiaoming.random.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimeLineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeLineFragment extends BaseFragment {
    private static final String TAG="TimeLineFragment";
    private static final String TYPE = "TYPE";
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;
    private String mType;//类型

    public TimeLineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimeLineFragment.
     */
    public static TimeLineFragment newInstance(String... params) {
        TimeLineFragment fragment = new TimeLineFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE, params[0]);
        bundle.putString(MainTimeLineFragment.SCREEN_NAME, params[1]);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TYPE, mType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mType = savedInstanceState.getString(TYPE);
        else
            initArguments();
    }

    /**
     * 初始化参数
     */
    private void initArguments() {
        Bundle bundle = getArguments();
        mType = bundle.getString(TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Constants.DEVELOPER_MODE)
            Debug.startMethodTracing(TAG);
        mRootView = inflateView(inflater, container);
        findViews(mRootView);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setUpComponent();
        if (Constants.DEVELOPER_MODE)
            Debug.stopMethodTracing();
        return mRootView;
    }

    /**
     * @param inflater
     * @param container
     * @return
     */
    private View inflateView(LayoutInflater inflater, ViewGroup container) {
        View rootView;
        switch (mType) {
            case MainTimeLineFragment.STATUS:
                rootView = inflater.inflate(R.layout.time_line, container, false);
                break;
            case CommentsFragment.COMMENTS:
                rootView = inflater.inflate(R.layout.comments_time_line, container, false);
                break;
            case FriendshipFragment.FRIEND:
                rootView = inflater.inflate(R.layout.friendship_time_line, container, false);
                break;
            default:
                rootView = inflater.inflate(R.layout.time_line, container, false);
                break;
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    /**
     * 初始化组件
     */
    private void setUpComponent() {
        mPager.setAdapter(new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {
            @Override
            public int getCount() {
                int count;
                switch (mType) {
                    case MainTimeLineFragment.STATUS:
                        count = Constants.TIME_LINE_TABS.length;
                        break;
                    case CommentsFragment.COMMENTS:
                        count = Constants.COMMENT_LINE_TABS.length;
                        break;
                    case FriendshipFragment.FRIEND:
                        count = Constants.FRIENDSHIP_TABS.length;
                        break;
                    default:
                        count = Constants.TIME_LINE_TABS.length;
                        break;
                }
                return count;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                String title;
                switch (mType) {
                    case MainTimeLineFragment.STATUS:
                        title = Constants.TIME_LINE_TABS[position];
                        break;
                    case CommentsFragment.COMMENTS:
                        title = Constants.COMMENT_LINE_TABS[position];
                        break;
                    case FriendshipFragment.FRIEND:
                        title = Constants.FRIENDSHIP_TABS[position];
                        break;
                    default:
                        title = Constants.TIME_LINE_TABS[position];
                        break;
                }
                return title;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
//                super.destroyItem(container, position, object);
            }

            @Override
            public Fragment getItem(int position) {
                Fragment fragment;
                switch (mType) {
                    case MainTimeLineFragment.STATUS:
                        fragment = MainTimeLineFragment.newInstance("", position, 0);
                        break;
                    case CommentsFragment.COMMENTS:
                        if (position == 0) {
                            fragment = MainTimeLineFragment.newInstance("", position, 1);
                        } else {
                            fragment = CommentsFragment.newInstance(position);
                        }
                        break;
                    case FriendshipFragment.FRIEND:
                        fragment = FriendshipFragment.newInstance(position);
                        break;
                    default:
                        fragment = MainTimeLineFragment.newInstance("", position, 0);
                        break;
                }
                return fragment;
            }
        });
        mTabs.setViewPager(mPager);
    }

    private void findViews(View rootView) {
        switch (mType) {
            case MainTimeLineFragment.STATUS:
                mPager = (ViewPager) rootView.findViewById(R.id.pager);
                mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
                break;
            case CommentsFragment.COMMENTS:
                mPager = (ViewPager) rootView.findViewById(R.id.cpager);
                mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.ctabs);
                break;
            case FriendshipFragment.FRIEND:
                mPager = (ViewPager) rootView.findViewById(R.id.fpager);
                mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.ftabs);
                break;
            default:
                mPager = (ViewPager) rootView.findViewById(R.id.pager);
                mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
                break;
        }
    }
}
