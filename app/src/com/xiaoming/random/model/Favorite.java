package com.xiaoming.random.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by XIAOM on 2015/3/15.
 */
public class Favorite {
    /** 我喜欢的微博信息 */
    public Status status;
    /** 我喜欢的微博的 Tag 信息 */
    public ArrayList<Tag> tags;
    /** 创建我喜欢的微博信息的时间 */
    public String favoriteTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"status\":\"").append(status).append("\",").append("\"favorted_time\":\"")
                .append(favoriteTime).append("\",").append("\"tags\":[");
        for (Tag tag:tags){
            sb.append(tag).append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 解析单条微博
     * @param jsonString
     * @return
     */
    public static Favorite parse(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            return Favorite.parse(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析单条微博
     * @param jsonObject
     * @return
     */
    public static Favorite parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        Favorite favorite = new Favorite();
        favorite.status         = Status.parse(jsonObject.optJSONObject("status"));
        favorite.favoriteTime = jsonObject.optString("favorited_time");
        JSONArray jsonArray    = jsonObject.optJSONArray("tags");
        if (jsonArray != null && jsonArray.length() > 0) {
            int length = jsonArray.length();
            favorite.tags = new ArrayList<Tag>(length);
            for (int ix = 0; ix < length; ix++) {
                favorite.tags.add(Tag.parse(jsonArray.optJSONObject(ix)));
            }
        }
        return favorite;
    }
    public static  class FavoriteList {
        /**
         * 微博列表
         */
        public ArrayList<Favorite> favoriteList;
        public int total_number;

    }

    /**
     * 解析微博列表
     * @param jsonString
     * @return
     */
    public static FavoriteList parseList(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        FavoriteList favorites = new FavoriteList();
        try {
            JSONObject jsonObject  = new JSONObject(jsonString);
            favorites.total_number = jsonObject.optInt("total_number", 0);
            JSONArray jsonArray    = jsonObject.optJSONArray("favorites");
            if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                favorites.favoriteList = new ArrayList<Favorite>(length);
                for (int ix = 0; ix < length; ix++) {
                    favorites.favoriteList.add(parse(jsonArray.optJSONObject(ix)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return favorites;
    }

    }
