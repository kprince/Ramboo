package com.xiaoming.random.model;

import com.sina.weibo.sdk.openapi.models.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 微博用户实体
 * Created by XIAOM on 2015/3/14.
 */
public class WeiboUser implements Serializable {


    /**
     * 用户UID（int64）
     */
    public String id;
    /**
     * 字符串型的用户 UID
     */
    public String idStr;
    /**
     * 用户昵称
     */
    public String screenName;
    /**
     * 友好显示名称
     */
    public String name;
    /**
     * 用户所在省级ID
     */
    public int province;
    /**
     * 用户所在城市ID
     */
    public int city;
    /**
     * 用户所在地
     */
    public String location;
    /**
     * 用户个人描述
     */
    public String description;
    /**
     * 用户博客地址
     */
    public String url;
    /**
     * 用户头像地址，50×50像素
     */
    public String profileImageUrl;
    /**
     * 用户的微博统一URL地址
     */
    public String profileUrl;
    /**
     * 用户的个性化域名
     */
    public String domain;
    /**
     * 用户的微号
     */
    public String weihao;
    /**
     * 性别，m：男、f：女、n：未知
     */
    public String gender;
    /**
     * 粉丝数
     */
    public int followersCount;
    /**
     * 关注数
     */
    public int friendsCount;
    /**
     * 微博数
     */
    public int statusesCount;
    /**
     * 收藏数
     */
    public int favouritesCount;
    /**
     * 用户创建（注册）时间
     */
    public String createdAt;
    /**
     * 暂未支持
     */
    public boolean following;
    /**
     * 是否允许所有人给我发私信，true：是，false：否
     */
    public boolean allowAllActMsg;
    /**
     * 是否允许标识用户的地理位置，true：是，false：否
     */
    public boolean geoEnabled;
    /**
     * 是否是微博认证用户，即加V用户，true：是，false：否
     */
    public boolean verified;
    /**
     * 暂未支持
     */
    public int verifiedType;
    /**
     * 用户备注信息，只有在查询用户关系时才返回此字段
     */
    public String remark;
    /**
     * 用户的最近一条微博信息字段
     */
    public Status status;
    /**
     * 是否允许所有人对我的微博进行评论，true：是，false：否
     */
    public boolean allowAllComment;
    /**
     * 用户大头像地址
     */
    public String avatarLarge;
    /**
     * 用户高清大头像地址
     */
    public String avatarHd;
    /**
     * 认证原因
     */
    public String verifiedReason;
    /**
     * 该用户是否关注当前登录用户，true：是，false：否
     */
    public boolean followMe;
    /**
     * 用户的在线状态，0：不在线、1：在线
     */
    public int onlineStatus;
    /**
     * 用户的互粉数
     */
    public int biFollowersCount;
    /**
     * 用户当前的语言版本，zh-cn：简体中文，zh-tw：繁体中文，en：英语
     */
    public String lang;
    /**
     * 注意：以下字段暂时不清楚具体含义，OpenAPI 说明文档暂时没有同步更新对应字段
     */
    public String star;
    public String mbtype;
    public String mbrank;
    public String blockWord;


    public static WeiboUser parse(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static WeiboUser parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        WeiboUser user = new WeiboUser();
        user.id = jsonObject.optString("id", "");
        user.idStr = jsonObject.optString("idstr", "");
        user.screenName = jsonObject.optString("screen_name", "");
        user.name = jsonObject.optString("name", "");
        user.province = jsonObject.optInt("province", -1);
        user.city = jsonObject.optInt("city", -1);
        user.location = jsonObject.optString("location", "");
        user.description = jsonObject.optString("description", "");
        user.url = jsonObject.optString("url", "");
        user.profileImageUrl = jsonObject.optString("profile_image_url", "");
        user.profileUrl = jsonObject.optString("profile_url", "");
        user.domain = jsonObject.optString("domain", "");
        user.weihao = jsonObject.optString("weihao", "");
        user.gender = jsonObject.optString("gender", "");
        user.followersCount = jsonObject.optInt("followers_count", 0);
        user.friendsCount = jsonObject.optInt("friends_count", 0);
        user.statusesCount = jsonObject.optInt("statuses_count", 0);
        user.favouritesCount = jsonObject.optInt("favourites_count", 0);
        user.createdAt = jsonObject.optString("created_at", "");
        user.following = jsonObject.optBoolean("following", false);
        user.allowAllActMsg = jsonObject.optBoolean("allow_all_act_msg", false);
        user.geoEnabled = jsonObject.optBoolean("geo_enabled", false);
        user.verified = jsonObject.optBoolean("verified", false);
        user.verifiedType = jsonObject.optInt("verified_type", -1);
        user.remark = jsonObject.optString("remark", "");
        //user.status             = jsonObject.optString("status", ""); // XXX: NO Need ?
        user.allowAllComment = jsonObject.optBoolean("allow_all_comment", true);
        user.avatarLarge = jsonObject.optString("avatar_large", "");
        user.avatarHd = jsonObject.optString("avatar_hd", "");
        user.verifiedReason = jsonObject.optString("verified_reason", "");
        user.followMe = jsonObject.optBoolean("follow_me", false);
        user.onlineStatus = jsonObject.optInt("online_status", 0);
        user.biFollowersCount = jsonObject.optInt("bi_followers_count", 0);
        user.lang = jsonObject.optString("lang", "");
        // 注意：以下字段暂时不清楚具体含义，OpenAPI 说明文档暂时没有同步更新对应字段含义
        user.star = jsonObject.optString("star", "");
        user.mbtype = jsonObject.optString("mbtype", "");
        user.mbrank = jsonObject.optString("mbrank", "");
        user.blockWord = jsonObject.optString("block_word", "");
        return user;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{")
                .append("\"id\":\"").append(this.id).append("\",")
                .append("\"idstr\":\"").append(this.idStr).append("\",")
                .append("\"screen_name\":\"").append(this.screenName).append("\",")
                .append("\"name\":\"").append(this.name).append("\",")
                .append("\"province\":\"").append(this.province).append("\",")
                .append("\"city\":\"").append(this.city).append("\",")
                .append("\"location\":\"").append(this.location).append("\",")
                .append("\"description\":\"").append(this.description).append("\",")
                .append("\"url\":\"").append(this.url).append("\",")
                .append("\"profile_image_url\":\"").append(this.profileImageUrl).append("\",")
                .append("\"profile_url\":\"").append(this.profileUrl).append("\",")
                .append("\"domain\":\"").append(this.domain).append("\",")
                .append("\"weihao\":\"").append(this.weihao).append("\",")
                .append("\"gender\":\"").append(this.gender).append("\",")
                .append("\"followers_count\":\"").append(this.followersCount).append("\",")
                .append("\"friends_count\":\"").append(this.friendsCount).append("\",")
                .append("\"statuses_count\":\"").append(this.statusesCount).append("\",")
                .append("\"favourites_count\":\"").append(this.favouritesCount).append("\",")
                .append("\"created_at\":\"").append(this.createdAt).append("\",")
                .append("\"following\":\"").append(this.following).append("\",")
                .append("\"allow_all_act_msg\":\"").append(this.allowAllActMsg).append("\",")
                .append("\"geo_enabled\":\"").append(this.geoEnabled).append("\",")
                .append("\"verified\":\"").append(this.verified).append("\",")
                .append("\"verified_type\":\"").append(this.verifiedType).append("\",")
                .append("\"remark\":\"").append(this.remark).append("\",")
                .append("\"status\":\"").append(this.status).append("\",")
                .append("\"allow_all_comment\":\"").append(this.allowAllComment).append("\",")
                .append("\"avatar_large\":\"").append(this.avatarLarge).append("\",")
                .append("\"avatar_hd\":\"").append(this.avatarHd).append("\",")
                .append("\"verified_reason\":\"").append(this.verifiedReason).append("\",")
                .append("\"follow_me\":\"").append(this.followMe).append("\",")
                .append("\"online_status\":\"").append(this.onlineStatus).append("\",")
                .append("\"bi_followers_count\":\"").append(this.biFollowersCount).append("\",")
                .append("\"lang\":\"").append(this.lang).append("\",")
                .append("\"star\":\"").append(this.star).append("\",")
                .append("\"mbtype\":\"").append(this.mbtype).append("\",")
                .append("\"mbrank\":\"").append(this.mbrank).append("\",")
                .append("\"block_word\":\"").append(this.blockWord)
                .append("\"}");
        return result.toString();
    }
}
