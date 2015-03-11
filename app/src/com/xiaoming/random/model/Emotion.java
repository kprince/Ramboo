package com.xiaoming.random.model;

import android.text.TextUtils;

import org.json.JSONObject;

/**
 * 新浪微博表情
 * Created by XM on 2015/2/10.
 */
public class Emotion {
    public String url;
    public String value;
    public String type;
    public boolean common;
    public boolean hot;
    public String category;
    public String phrase;


    public static Emotion parse(JSONObject obj) {
        Emotion e = new Emotion();
        e.url = obj.optString("url", "");
        e.value = obj.optString("value", "");
        e.type = obj.optString("type", "");
        e.common = obj.optBoolean("common", true);
        e.hot = obj.optBoolean("hot", false);
        String sss = obj.optString("category", "");
        sss = TextUtils.isEmpty(sss) ? "默认" : sss;
        e.category = sss.equals(" ") ? "默认" : sss;
        e.phrase = obj.optString("phrase", "");
        return e;
    }
}
