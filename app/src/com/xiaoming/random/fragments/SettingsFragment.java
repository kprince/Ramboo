package com.xiaoming.random.fragments;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.dao.StatusDao;
import com.xiaoming.random.utils.FileUtils;
import com.xiaoming.random.utils.OauthUtils;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private TextView mCachedImgTxt, mCachedImgPath, mCachedContent, mCachedContentPath,
            mExpCardTxt, mExpCardPath;
    private ImageView mCachedImgDel, mCachedContentDel, mExpCardDel;
    private StatusDao mDao;
    private ButtonRectangle mBtnOne, mBtnTwo, mBtnThree, mBtnFour, mBtnFive, mBtnSix, mBtnSeven, mBtnEight;


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDao = new StatusDao(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        findViews(view);
        initAllComponents(view);
        return view;
    }


    private void initAllComponents(View view) {
        mCachedImgDel.setColorFilter(getColor(R.attr.colorPrimary), PorterDuff.Mode.MULTIPLY);
        mCachedContentDel.setColorFilter(getColor(R.attr.colorPrimary), PorterDuff.Mode.MULTIPLY);
        mExpCardDel.setColorFilter(getColor(R.attr.colorPrimary), PorterDuff.Mode.MULTIPLY);
        //图片缓存路径
        final File file = StorageUtils.getCacheDirectory(getActivity());
        dealCachedImg(file);
        //缓存到数据库的内容
        final String path = mDao.getDatabasePath();
        dealCachedContent(path);
        dealExpCards();
    }


    private void dealCachedContent(final String path) {
        mCachedContentPath.setText(path.substring(0, path.lastIndexOf("/")));
        File file1 = new File(path);
        String text1 = getString(R.string.weiboCacheSpace) + FileUtils.getFileSizeStr(file1.length());
        mCachedContent.setText(text1);
        mCachedContentDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDao.clearDatabase();
                OauthUtils.showToast(getActivity(), getString(R.string.weiboCleared));
                dealCachedContent(path);
            }

        });
    }

    private void dealCachedImg(final File file) {
        long imgTotalSpace = FileUtils.getDirSize(file);
        String text = getString(R.string.imageCacheSpace) + FileUtils.getFileSizeStr(imgTotalSpace);
        mCachedImgTxt.setText(text);
        mCachedImgPath.setText(file.getAbsolutePath());
        mCachedImgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!file.isDirectory()) {
                    file.delete();
                    return;
                }
                File[] files = file.listFiles();
                for (File f : files) {
                    if (!f.isDirectory())
                        f.delete();
                }
                OauthUtils.showToast(getActivity(), getString(R.string.imageCleared));
                File cImg = StorageUtils.getCacheDirectory(getActivity());
                dealCachedImg(cImg);
            }
        });
    }

    /**
     * 导出的微博卡片
     */
    private void dealExpCards() {
        String cardsPath = Environment.getExternalStorageDirectory() + FileUtils.FIlE_PATH_CARD;
        File file = new File(cardsPath);
        long space = FileUtils.getDirSize(file);
        String text = getString(R.string.exportedCardSpace) + FileUtils.getFileSizeStr(space);
        mExpCardTxt.setText(text);
        mExpCardPath.setText(cardsPath);

    }

    private void findViews(View view) {
        mCachedContentPath = (TextView) view.findViewById(R.id.cachedCPath);
        mCachedImgPath = (TextView) view.findViewById(R.id.cachedImgPath);
        mCachedContentDel = (ImageView) view.findViewById(R.id.cachedContentDel);
        mCachedImgDel = (ImageView) view.findViewById(R.id.cachedImgDel);
        mCachedContent = (TextView) view.findViewById(R.id.cachedContent);
        mCachedImgTxt = (TextView) view.findViewById(R.id.cachedImgText);
        mExpCardDel = (ImageView) view.findViewById(R.id.exported_card_img);
        mExpCardPath = (TextView) view.findViewById(R.id.exported_card_path);
        mExpCardTxt = (TextView) view.findViewById(R.id.exported_card_content);
        mBtnOne = (ButtonRectangle) view.findViewById(R.id.color_btn_one);
        mBtnTwo = (ButtonRectangle) view.findViewById(R.id.color_btn_two);
        mBtnThree = (ButtonRectangle) view.findViewById(R.id.color_btn_three);
        mBtnFour = (ButtonRectangle) view.findViewById(R.id.color_btn_four);
        mBtnFive = (ButtonRectangle) view.findViewById(R.id.color_btn_five);
        mBtnSix = (ButtonRectangle) view.findViewById(R.id.color_btn_six);
        mBtnSeven = (ButtonRectangle) view.findViewById(R.id.color_btn_seven);
        mBtnEight = (ButtonRectangle) view.findViewById(R.id.color_btn_eight);

        mBtnOne.setOnClickListener(this);
        mBtnTwo.setOnClickListener(this);
        mBtnThree.setOnClickListener(this);
        mBtnFour.setOnClickListener(this);
        mBtnFive.setOnClickListener(this);
        mBtnSix.setOnClickListener(this);
        mBtnSeven.setOnClickListener(this);
        mBtnEight.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        ButtonRectangle btn = (ButtonRectangle) v;
        SharedPreferences sp = getUserPref();
        String currentColor = sp.getString(BaseActivity.SF_THEME_COLOR, "Teal");
        if (currentColor.equals(btn.getText()))
            return;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(BaseActivity.SF_THEME_COLOR, btn.getText());
        editor.apply();
        getActivity().finish();
        startActivity(getActivity().getIntent());
//        android.os.Process.killProcess(android.os.Process.myPid());
    }
}