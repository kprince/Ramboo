package com.xiaoming.random.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiaoming.random.R;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OauthUtils {
    private static boolean mShowImage;

    public static String encodeUrl(Map<String, String> param) {
        if (param == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        Set<String> keys = param.keySet();
        boolean first = true;

        for (String key : keys) {
            String value = param.get(key);
            //pain...EditMyProfileDao params' values can be empty
            if (!TextUtils.isEmpty(value) || key.equals("description") || key.equals("url")) {
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }
                try {
                    sb.append(URLEncoder.encode(key, "UTF-8")).append("=")
                            .append(URLEncoder.encode(param.get(key), "UTF-8"));
                } catch (UnsupportedEncodingException e) {

                }
            }


        }
        return sb.toString();
    }

    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                try {
                    params.putString(URLDecoder.decode(v[0], "UTF-8"),
                            URLDecoder.decode(v[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }
        return params;
    }

    /**
     * Parse a URL query and fragment parameters into a key-value bundle.
     */
    public static Bundle parseUrl(String url) {
        // hack to prevent MalformedURLException
        url = url.replace("weiboconnect", "http");
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

    }

    @SuppressLint("SimpleDateFormat")
    public static String formatCreateAt(String createAt) {
        @SuppressWarnings("deprecation")
        Date createDate = new Date(createAt);
        String result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createDate);
        return result;
    }

    public static String splitAndFilterString(String input) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
        // 去掉所有html元素,  
        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
                "<[^>]*>", "").replaceAll("[(/>)<]", "");
        return str;
    }

    public static void stripUnderlines(TextView textView) {
        SpannableString s = new SpannableString(textView.getText().toString());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    public static void showTimeLineImage(Context context, ImageView imageAware,
                                         String uri, DisplayImageOptions options, ImageLoadingListener listener) {
        if (!mShowImage) {
            mShowImage = context.getSharedPreferences("appPref", Activity.MODE_PRIVATE).getBoolean("show_image", false);
        }
        if (mShowImage) {
            if (listener != null) {
                ImageLoader.getInstance().displayImage(uri, imageAware, options, listener);
            } else {
                ImageLoader.getInstance().displayImage(uri, imageAware, options);
            }
        } else {
            imageAware.setImageResource(R.drawable.menu_background);
        }
    }

    public static void doLinkify(TextView tv) {
        String schema = "user://?SCREEN_NAME=";
        Linkify.addLinks(tv, Linkify.WEB_URLS);
        Pattern pattern = Pattern.compile("@[^.,:;!?\\s#@。，：；！？]+");
        Pattern pattern2 = Pattern.compile("#(\\w+?)#");
        Linkify.addLinks(tv, pattern, schema, new RandomMatchFilter(), new RandomTransformFilter());
        Linkify.addLinks(tv, pattern2, null);
        // OauthUtils.stripUnderlines(statusTextView);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected())
            return true;
        return false;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        //获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) { //listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); //计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight(); //统计所有子项的总高度
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        //params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    public static class RandomMatchFilter implements MatchFilter {

        @Override
        public boolean acceptMatch(CharSequence s, int start, int end) {
            return true;
        }

    }

    public static class RandomTransformFilter implements TransformFilter {

        @Override
        public String transformUrl(Matcher match, String url) {
            return url;
        }

    }


}

