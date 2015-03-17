package com.xiaoming.random.model;


import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/** 微博实体类
 * Created by XIAOM on 2015/3/14.
 */
public class Status implements Serializable{

    /** 微博创建时间 */
    public String createdAt;
    /** 微博ID */
    public String id;
    /** 微博MID */
    public String mid;
    /** 字符串型的微博ID */
    public String idStr;
    /** 微博信息内容 */
    public String text;
    /** 微博来源 */
    public String source;
    /** 是否已收藏，true：是，false：否  */
    public boolean favorited;
    /** 是否被截断，true：是，false：否 */
    public boolean truncated;
    /**（暂未支持）回复ID */
    public String reply2StatusID;
    /**（暂未支持）回复人UID */
    public String reply2UserID ;
    /**（暂未支持）回复人昵称 */
    public String reply2ScreenName ;
    /** 缩略图片地址（小图），没有时不返回此字段 */
    public String thumbnail;
    /** 中等尺寸图片地址（中图），没有时不返回此字段 */
    public String bmiddle;
    /** 原始图片地址（原图），没有时不返回此字段 */
    public String original;
    /** 地理信息字段 */
    public Geo geo;
    /** 微博作者的用户信息字段 */
    public WeiboUser user;
    /** 被转发的原微博信息字段，当该微博为转发微博时返回 */
    public Status repostStatus;
    /** 转发数 */
    public int repostCount;
    /** 评论数 */
    public int commentCount;
    /** 表态数 */
    public int attitudeCount;
    /** 暂未支持 */
    public int mlevel;
    /**
     * 微博的可见性及指定可见分组信息。该 object 中 type 取值，
     * 0：普通微博，1：私密微博，3：指定分组微博，4：密友微博；
     * list_id为分组的组号
     */
    public Visible visible;
    /** 微博配图地址。多图时返回多图链接。无配图返回"[]" */
    public ArrayList<String> pic_urls;
    /** 微博流内的推广微博ID */
    //public Ad ad;


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{")
                .append("\"created_at\":\"")              .append(this.createdAt       ).append("\",")
                .append("\"id\":\"")                      .append(this.id              ).append("\",")
                .append("\"mid\":\"")                     .append(this.mid             ).append("\",")
                .append("\"idstr\":\"")                   .append(this.idStr           ).append("\",")
                .append("\"text\":\"")                    .append(this.text            ).append("\",")
                .append("\"source\":\"")                  .append(this.source          ).append("\",")
                .append("\"favorited\":\"")               .append(this.favorited       ).append("\",")
                .append("\"truncated\"\"")               .append(this.truncated       ).append("\",")
                .append("\"in_reply_to_status_id\":\"")   .append(this.reply2StatusID  ).append("\",")
                .append("\"in_reply_to_user_id\":\"")     .append(this.reply2UserID    ).append("\",")
                .append("\"in_reply_to_screen_name\":\"") .append(this.reply2ScreenName).append("\",")
                .append("\"thumbnail_pic\":\"")           .append(this.thumbnail       ).append("\",")
                .append("\"bmiddle_pic\":\"")             .append(this.bmiddle         ).append("\",")
                .append("\"original_pic\":\"")            .append(this.original        ).append("\",")
                .append("\"geo\":\"")                     .append(this.geo             ).append("\",")
                .append("\"user\":\"")                    .append(this.user            ).append("\",")
                .append("\"retweeted_status\":\"")        .append(this.repostStatus    ).append("\",")
                .append("\"reposts_count\":\"")           .append(this.repostCount     ).append("\",")
                .append("\"comments_count\":\"")          .append(this.commentCount    ).append("\",")
                .append("\"attitudes_count\":\"")         .append(this.attitudeCount   ).append("\",")
                .append("\"mlevel\":\"")                  .append(this.mlevel          ).append("\",")
                .append("\"visible\":\"")                 .append(this.visible         ).append("\",")
                .append("\"pic_urls\":[");
                for (String url:pic_urls){
                    s.append("{\"thumbnail_pic\":\"").append(url).append("\"},");
                }
                s.append("]}");
        return s.toString();

    }



    public static Status parse(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return Status.parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Status parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        Status status = new Status();
        status.createdAt       = jsonObject.optString("created_at");
        status.id               = jsonObject.optString("id");
        status.mid              = jsonObject.optString("mid");
        status.idStr            = jsonObject.optString("idstr");
        status.text             = jsonObject.optString("text");
        status.source           = jsonObject.optString("source");
        status.favorited        = jsonObject.optBoolean("favorited", false);
        status.truncated        = jsonObject.optBoolean("truncated", false);
        // Have NOT supported
        status.reply2StatusID   = jsonObject.optString("in_reply_to_status_id");
        status.reply2UserID     = jsonObject.optString("in_reply_to_user_id");
        status.reply2ScreenName = jsonObject.optString("in_reply_to_screen_name");

        status.thumbnail    = jsonObject.optString("thumbnail_pic");
        status.bmiddle      = jsonObject.optString("bmiddle_pic");
        status.original     = jsonObject.optString("original_pic");
        status.geo              = Geo.parse(jsonObject.optJSONObject("geo"));
        status.user             = WeiboUser.parse(jsonObject.optJSONObject("user"));
        status.repostStatus = Status.parse(jsonObject.optJSONObject("retweeted_status"));
        status.repostCount    = jsonObject.optInt("reposts_count");
        status.commentCount   = jsonObject.optInt("comments_count");
        status.attitudeCount  = jsonObject.optInt("attitudes_count");
        status.mlevel           = jsonObject.optInt("mlevel", -1);    // Have NOT supported
        status.visible          = Visible.parse(jsonObject.optJSONObject("visible"));
        JSONArray picUrlsArray = jsonObject.optJSONArray("pic_urls");
        if (picUrlsArray != null && picUrlsArray.length() > 0) {
            int length = picUrlsArray.length();
            status.pic_urls = new ArrayList<String>(length);
            JSONObject tmpObject = null;
            for (int ix = 0; ix < length; ix++) {
                tmpObject = picUrlsArray.optJSONObject(ix);
                if (tmpObject != null) {
                    status.pic_urls.add(tmpObject.optString("thumbnail_pic"));
                }
            }
        }
        return status;
    }
    public static class StatusList {

        /** 微博列表 */
        public ArrayList<Status> statusList;
        public Status statuses;
        public boolean hasvisible;
        public String previousCursor;
        public String nextCursor;
        public int totalNumber;
        public Object[] advertises;


    }
    public static StatusList parseList(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        StatusList statuses = new StatusList();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            statuses.hasvisible      = jsonObject.optBoolean("hasvisible", false);
            statuses.previousCursor = jsonObject.optString("previous_cursor", "0");
            statuses.nextCursor     = jsonObject.optString("next_cursor", "0");
            statuses.totalNumber    = jsonObject.optInt("total_number", 0);

            JSONArray jsonArray      = jsonObject.optJSONArray("statuses");
            if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                statuses.statusList = new ArrayList<Status>(length);
                for (int ix = 0; ix < length; ix++) {
                    statuses.statusList.add(Status.parse(jsonArray.getJSONObject(ix)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return statuses;
    }
}
