package com.xiaoming.random.activities;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.widgets.Dialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaoming.random.Constants;
import com.xiaoming.random.R;
import com.xiaoming.random.fragments.CommentsFragment;
import com.xiaoming.random.fragments.FriendshipFragment;
import com.xiaoming.random.fragments.MainTimeLineFragment;
import com.xiaoming.random.fragments.SettingsFragment;
import com.xiaoming.random.fragments.TimeLineFragment;
import com.xiaoming.random.fragments.UserProfileFragment;
import com.xiaoming.random.utils.OauthUtils;

public class AppMainActivity extends BaseActivity {
    public static final String FIRST_TIME_FLAG = "firstTimeFlag";
    static final int HOME_INDEX = 0, COMMENTS_INDEX = 1, FRIENDS_INDEX = 2,
            PROFILE_INDEX = 3, LOGOUT_INDEX = 4, SETTINGS_INDEX = 5;//对应侧滑抽屉菜单的index
    private static final String TAG = "AppMainActivity";
    private static final String MENU_POSITION = "MENU_POSITION";//菜单位置
    LinearLayout mMainContent;
    private long mBackPressedTime = 0;
    private String mUserName;
    private Toolbar mToolbar;
    private ListView mDrawer;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private float lastTranslate = 0.0f;
    private FragmentManager mFragMan;
    private SparseArray<Fragment> mRightFragments = new SparseArray();
    private Dialog mLogoutDialog;
    private int mMenuPosition;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_status:
                Intent intent = new Intent(this, SendWeiboActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
//        Debug.startMethodTracing(TAG);
        super.onCreate(savedInstanceState);
        checkFirstTimeUse();
        mUserName = getUserName();
        setContentView(R.layout.main);
        if (savedInstanceState == null) {
            setInitFragment();
        } else {
            mFragMan = getSupportFragmentManager();
            restoreFragments(HOME_INDEX, MainTimeLineFragment.class.getSimpleName());
            restoreFragments(COMMENTS_INDEX, CommentsFragment.class.getSimpleName());
            restoreFragments(PROFILE_INDEX, UserProfileFragment.class.getSimpleName());
            restoreFragments(FRIENDS_INDEX, FriendshipFragment.class.getSimpleName());
            restoreFragments(SETTINGS_INDEX, SettingsFragment.class.getSimpleName());
            mMenuPosition = savedInstanceState.getInt(MENU_POSITION, 1);
            switchContent(mMenuPosition);
        }
        findViews();
        mainContentAnimation();
        initDrawer();
        setToolBar(mToolbar, BaseActivity.INDEX, mUserName);
//        Debug.stopMethodTracing();
    }
    /**
     * 非正常退出时恢复Fragments到SparseArray
     *
     * @param index fragment的index
     * @param tag   fragment的tag
     */
    private void restoreFragments(int index, String tag) {
        Fragment fragment = mFragMan.findFragmentByTag(tag);
        if (fragment != null)
            mRightFragments.append(index, fragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(MENU_POSITION, mMenuPosition);
        super.onSaveInstanceState(outState);
    }

    /**
     * 初始化主界面
     */
    private void setInitFragment() {
        mMenuPosition = 1;
        Fragment fragment = TimeLineFragment.newInstance(MainTimeLineFragment.STATUS, mUserName);
        mRightFragments.append(HOME_INDEX, fragment);
        mFragMan = getSupportFragmentManager();
        FragmentTransaction ft = mFragMan.beginTransaction();
        ft.add(R.id.container, fragment, MainTimeLineFragment.class.getSimpleName());
        ft.commit();
    }


    /**
     * drawer事件侦听
     */
    private void mainContentAnimation() {
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.id.action_bar) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float moveFactor = (mDrawer.getWidth() * slideOffset);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mMainContent.setTranslationX(moveFactor);
                } else {
                    TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                    anim.setDuration(0);
                    anim.setFillAfter(true);
                    mMainContent.startAnimation(anim);
                    lastTranslate = moveFactor;
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switchContent(mMenuPosition);
            }
        };
        mDrawerLayout.setDrawerListener(mToggle);
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawer = (ListView) findViewById(R.id.left_drawer);
        mMainContent = (LinearLayout) findViewById(R.id.main_content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    /**
     * 初始化Navigation Drawer
     */
    private void initDrawer() {
        View view = View.inflate(this, R.layout.drawer_hearder, null);
        ImageView avatarView = (ImageView) view.findViewById(R.id.drawerAvatar);
        TextView nameView = (TextView) view.findViewById(R.id.drawerName);
        nameView.setText(mUserName);
        ImageLoader.getInstance().displayImage(getUserAvatar(), avatarView);
        mDrawer.addHeaderView(view);
        mDrawer.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(AppMainActivity.this, R.layout.drawer_item, null);
                    holder = new ViewHolder();
                    holder.drawerItemName = (TextView) convertView.findViewById(R.id.drawerItemName);
                    holder.drawerItemImage = (ImageView) convertView.findViewById(R.id.drawerItemImage);
                    holder.drawerItemImage.setColorFilter(getResources().getColor(R.color.grey_500), PorterDuff.Mode.MULTIPLY);
                    convertView.setTag(holder);
                }
                holder = (ViewHolder) convertView.getTag();
                holder.drawerItemName.setText(Constants.MENU_ITEMS[position]);
                switch (position) {
                    case HOME_INDEX:
                        holder.drawerItemImage.setImageResource(R.drawable.ic_home_white_36dp);
                        if (mMenuPosition-1==HOME_INDEX) {
                            change2Selected(holder);
                        }
                        break;
                    case COMMENTS_INDEX:
                        holder.drawerItemImage.setImageResource(R.drawable.ic_email_white_36dp);
                        if (mMenuPosition-1==COMMENTS_INDEX) {
                            change2Selected(holder);
                        }
                        break;
                    case FRIENDS_INDEX:
                        holder.drawerItemImage.setImageResource(R.drawable.ic_group_white_36dp);
                        if (mMenuPosition-1==FRIENDS_INDEX) {
                            change2Selected(holder);
                        }
                        break;
                    case PROFILE_INDEX:
                        holder.drawerItemImage.setImageResource(R.drawable.ic_account_circle_white_36dp);
                        if (mMenuPosition-1==PROFILE_INDEX) {
                            change2Selected(holder);
                        }
                        break;
                    case LOGOUT_INDEX:
                        holder.drawerItemImage.setImageResource(R.drawable.ic_exit_to_app_white_36dp);
                        if (mMenuPosition-1==LOGOUT_INDEX) {
                            change2Selected(holder);
                        }
                        break;
                    case SETTINGS_INDEX:
                        holder.drawerItemImage.setImageResource(R.drawable.ic_settings_white_36dp);
                        if (mMenuPosition-1==SETTINGS_INDEX) {
                            change2Selected(holder);
                        }
                        break;
                }
                return convertView;
            }

            private void change2Selected(ViewHolder holder) {
                holder.drawerItemImage.setColorFilter(getColor(R.attr.colorPrimary), PorterDuff.Mode.MULTIPLY);
                holder.drawerItemName.setTextColor(getColor(R.attr.colorPrimary));
            }

            @Override
            public long getItemId(int position) {

                return 0;
            }

            @Override
            public Object getItem(int position) {
                return Constants.MENU_ITEMS[position];
            }

            @Override
            public int getCount() {
                return Constants.MENU_ITEMS.length;
            }

            class ViewHolder {
                TextView drawerItemName;
                ImageView drawerItemImage;
            }
        });
        mDrawerLayout.setStatusBarBackgroundColor(getColor(R.attr.colorPrimaryDark));
        mDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //屏蔽header点击
                if (position==0)return;
                for (int i = 1; i <= Constants.MENU_ITEMS.length; i++) {
                    View child = mDrawer.getChildAt(i);
                    cancelSelect(child);
                }
                selected(view);
                mMenuPosition = position;
                if (mDrawer.isShown())
                    mDrawerLayout.closeDrawer(mDrawer);
            }

            /**
             * 取消选中状态
             * @param child
             */
            private void cancelSelect(View child) {
                ImageView image = (ImageView) child.findViewById(R.id.drawerItemImage);
                TextView text = (TextView) child.findViewById(R.id.drawerItemName);
                image.setColorFilter(getResources().getColor(R.color.grey_500), PorterDuff.Mode.MULTIPLY);
                text.setTextColor(getResources().getColor(R.color.grey_500));
            }

            /**
             * 选中状态
             * @param view
             */
            private void selected(View view) {
                ImageView image = (ImageView) view.findViewById(R.id.drawerItemImage);
                TextView text = (TextView) view.findViewById(R.id.drawerItemName);
                image.setColorFilter(getColor(R.attr.colorPrimary), PorterDuff.Mode.MULTIPLY);
                text.setTextColor(getColor(R.attr.colorPrimary));
            }
        });
    }

    /**
     * 打开navigation Drawer
     * showNavigationDrawer
     */
    public void showNavigationDrawer() {
        if (!mDrawer.isShown()) {
            mDrawerLayout.openDrawer(mDrawer);
        }
    }

    /**
     * 切换主界面内容
     */
    private void switchContent(int position) {
        if (position == 0) return;
        Fragment fragment;
        if (mFragMan == null)
            mFragMan = getSupportFragmentManager();
        if (position - 1 != LOGOUT_INDEX)
            hideAllFragments(mFragMan);
        FragmentTransaction ft = mFragMan.beginTransaction();
        switch (position - 1) {
            case HOME_INDEX:
                getSupportActionBar().setTitle(mUserName);
                fragment = mRightFragments.get(HOME_INDEX);
                if (fragment == null) {
                    fragment = TimeLineFragment.newInstance(MainTimeLineFragment.STATUS, mUserName);
                    ft.add(R.id.container, fragment, MainTimeLineFragment.class.getSimpleName());
                    mRightFragments.append(HOME_INDEX, fragment);
                } else
                    ft.show(fragment);
                break;
            case COMMENTS_INDEX:
                getSupportActionBar().setTitle(mUserName);
                fragment = mRightFragments.get(COMMENTS_INDEX);
                if (fragment == null) {
                    fragment = TimeLineFragment.newInstance(CommentsFragment.COMMENTS, mUserName);
                    ft.add(R.id.container, fragment, CommentsFragment.class.getSimpleName());
                    mRightFragments.append(COMMENTS_INDEX, fragment);
                } else
                    ft.show(fragment);
                break;
            case FRIENDS_INDEX:
                getSupportActionBar().setTitle(mUserName);
                fragment = mRightFragments.get(FRIENDS_INDEX);
                if (fragment == null) {
                    fragment = TimeLineFragment.newInstance(FriendshipFragment.FRIEND, mUserName);
                    ft.add(R.id.container, fragment, FriendshipFragment.class
                            .getSimpleName());
                    mRightFragments.append(FRIENDS_INDEX, fragment);
                } else
                    ft.show(fragment);
                break;
            case PROFILE_INDEX:
                getSupportActionBar().setTitle(getString(R.string.userDetail));
                fragment = mRightFragments.get(PROFILE_INDEX);
                if (fragment == null) {
                    fragment = UserProfileFragment.newInstance(mUserName);
                    ft.add(R.id.container, fragment, UserProfileFragment.class
                            .getSimpleName());
                    mRightFragments.append(PROFILE_INDEX, fragment);
                } else
                    ft.show(fragment);
                break;
            case LOGOUT_INDEX:
                if (mLogoutDialog == null)
                    mLogoutDialog = new Dialog(this, getString(R.string.logout2Account), getString(R.string.confirmLogout));
                mLogoutDialog.show();
                ButtonFlat accept = mLogoutDialog.getButtonAccept();
                accept.setText(getString(R.string.accept));
                mLogoutDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLogoutDialog.dismiss();
                        Intent intent = new Intent(AppMainActivity.this, AccountsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case SETTINGS_INDEX:
                getSupportActionBar().setTitle(getString(R.string.settings));
                fragment = mRightFragments.get(SETTINGS_INDEX);
                if (fragment == null) {
                    fragment = SettingsFragment.newInstance();
                    ft.add(R.id.container, fragment, SettingsFragment.class
                            .getSimpleName());
                    mRightFragments.append(SETTINGS_INDEX, fragment);
                } else
                    ft.show(fragment);
                break;
        }
        ft.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //退出activity时释放dialog
        if (mLogoutDialog != null) {
            mLogoutDialog.dismiss();
            mLogoutDialog = null;
        }
    }

    /**
     * 隐藏所有已add到ft的fragment
     *
     * @param fm FragmentManager
     */
    private void hideAllFragments(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        if (mRightFragments.get(COMMENTS_INDEX) != null)
            ft.hide(mRightFragments.get(COMMENTS_INDEX));
        if (mRightFragments.get(FRIENDS_INDEX) != null)
            ft.hide(mRightFragments.get(FRIENDS_INDEX));
        if (mRightFragments.get(PROFILE_INDEX) != null)
            ft.hide(mRightFragments.get(PROFILE_INDEX));
        if (mRightFragments.get(SETTINGS_INDEX) != null)
            ft.hide(mRightFragments.get(SETTINGS_INDEX));
        if (mRightFragments.get(HOME_INDEX) != null)
            ft.hide(mRightFragments.get(HOME_INDEX));
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 首次使用需要授权
     */
    private void checkFirstTimeUse() {
        boolean firstTimeFlag = getUserPref().getBoolean(FIRST_TIME_FLAG, true);
        if (firstTimeFlag) {
            OauthUtils.showToast(this, getString(R.string.firstTime));
            authenticate();
        }
    }


    private void authenticate() {
        Intent intent = new Intent(this, AccountsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        //菜单为打开状态，则关闭菜单
        if (mDrawer.isShown()) {
            mDrawerLayout.closeDrawer(mDrawer);
            return;
        }

        long t = System.currentTimeMillis();
        if (t - mBackPressedTime > 2000) {    // 2 secs
            mBackPressedTime = t;
            Toast.makeText(this, getString(R.string.pressAgain),
                    Toast.LENGTH_SHORT).show();
        } else {    // this guy is serious
            // clean up
            super.onBackPressed();       // bye
        }

    }
}
