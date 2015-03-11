package com.xiaoming.random.model;

import com.sina.weibo.sdk.openapi.models.User;

/**
 * 授权用户，继承自微博SDK的User类，扩展了两个字段token和expires:
 * token：授权用户的token
 * expires:token过期时间
 * 使用这两个字段可以生成一个新浪微博API的授权token
 */
public class AuthUser extends User {
    private static final long serialVersionUID = 1L;
    public String token;//授权token
    public String expires;//token过期时间
    public AuthUser(User user) {
        this.id = user.id;
        this.idstr = user.idstr;
        this.screen_name = user.screen_name;
        this.name = user.name;
        this.province = user.province;
        this.city = user.city;
        this.location = user.location;
        this.description = user.description;
        this.url = user.url;
        this.profile_image_url = user.profile_image_url;
        this.profile_url = user.profile_url;
        this.domain = user.domain;
        this.weihao = user.weihao;
        this.gender = user.gender;
        this.followers_count = user.followers_count;
        this.friends_count = user.friends_count;
        this.statuses_count = user.statuses_count;
        this.favourites_count = user.favourites_count;
        this.created_at = user.created_at;
        this.following = user.following;
        this.allow_all_act_msg = user.allow_all_act_msg;
        this.geo_enabled = user.geo_enabled;
        this.verified = user.verified;
        this.verified_type = user.verified_type;
        this.remark = user.remark;
        this.allow_all_comment = user.allow_all_comment;
        this.avatar_large = user.avatar_large;
        this.avatar_hd = user.avatar_hd;
        this.verified_reason = user.verified_reason;
        this.follow_me = user.follow_me;
        this.online_status = user.online_status;
        this.bi_followers_count = user.bi_followers_count;
        this.lang = user.lang;
        this.star = user.star;
        this.mbtype = user.mbtype;
        this.mbrank = user.mbrank;
        this.block_word = user.block_word;
    }
}
