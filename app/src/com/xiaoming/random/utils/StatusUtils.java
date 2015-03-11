package com.xiaoming.random.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.sina.weibo.sdk.openapi.models.Status;
import com.xiaoming.random.R;
import com.xiaoming.random.activities.UserProfileActivity;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StatusUtils {
    private static final int BITMAP_HEIGHT = 100, BITMAP_WIDTH = 100;
    private static final String TAG_CACHE = "ImageCache";
    public static DisplayImageOptions UIL_OPTIONS = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.menu_background)
            .showImageOnLoading(R.drawable.menu_background)
            .resetViewBeforeLoading(true).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).build();


//    public static void dealStatusText(Context context,  TextView tv,String text){
//
//
//    }

    public static void toggleTagOnImageView(ImageView imageView, String imageUri) {
        imageView.setTag(imageUri);
    }

    /**
     * 替换微博字符串中的表情占位符为表情图片,添加超链接等
     *
     * @param context context用于查询数据库
     * @param tv      TextView
     * @param text    微博字符串
     */
    public static void dealStatusText(final Context context, TextView tv, String text, final String userName) {
        AssetManager assetManager = context.getAssets();
        SpannableString sss = new SpannableString(TextUtils.isEmpty(userName) ? text : "@" + userName + "：" + text);
        //匹配@用户昵称
        final Matcher atMatcher = Pattern.compile("@[^.,:;!?\\s#@。，：；！？]+").matcher(sss);
        while (atMatcher.find()) {
            String matchUserName = atMatcher.group();
            ClickableSpan nameSpan = new AtClickableSpan(context, matchUserName.substring(1, matchUserName.length()));
            sss.setSpan(nameSpan, atMatcher.start(), atMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        //WEB URL 匹配
        Matcher webURLMatcher = Patterns.WEB_URL.matcher(sss);
        while (webURLMatcher.find()) {
            NoLineClickableSpan span = new NoLineClickableSpan(context, webURLMatcher.group());
            sss.setSpan(span, webURLMatcher.start(), webURLMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //正则表达式匹配所有表情
        Matcher matcher = Pattern.compile("\\[[\\u4e00-\\u9fa5 a-z]+\\]").matcher(sss);
        while (matcher.find()) {
            //Bitmap bitmap = null;
            Drawable d = null;
            String s = matcher.group();
            String name = s.substring(1, s.length() - 1);
            try {
                String pinyin = PinYinGenerator.formatToPinYin(name);
                // bitmap = BitmapFactory.decodeStream(assetManager.open(pinyin + ".gif"));
                d = Drawable.createFromStream(assetManager.open(pinyin + ".gif"), "");
                // scaleBitmap(bitmap,BITMAP_WIDTH,BITMAP_HEIGHT);
            } catch (Exception e) {
                d = null;
            }
            if (d != null) {
                d.setBounds(0, 0, tv.getLineHeight(), tv.getLineHeight());
                ImageSpan span = new ImageSpan(d);
                sss.setSpan(span, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tv.setText(sss);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int h, int w) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) h) / width;
        float scaleHeight = ((float) w) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true);
        return bitmap;
    }

    private static JSONObject stauts2JSONObject(Status status) {
        JSONObject object = new JSONObject();
        //todo：微博对象转化成JSONObject
        return object;
    }

    static class AtClickableSpan extends ClickableSpan {
        Context context;
        String name;

        public AtClickableSpan(Context context, String name) {
            super();
            this.context = context;
            this.name = name;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra(StatusViewHolder.SCREEN_NAME, name);
            context.startActivity(intent);
        }

    }

    static class NoLineClickableSpan extends ClickableSpan {
        String text;
        Context context;

        public NoLineClickableSpan(Context context, String text) {
            super();
            this.text = text;
            this.context = context;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false); //去掉下划线
        }

        @Override
        public void onClick(View widget) {
            processHyperLinkClick(text); //点击超链接时调用
        }

        private void processHyperLinkClick(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            context.startActivity(intent);
        }
    }

}
