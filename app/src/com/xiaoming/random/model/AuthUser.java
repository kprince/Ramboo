package com.xiaoming.random.model;

import com.sina.weibo.sdk.openapi.models.User;

/**
 * 授权用户，继承自微博SDK的User类，扩展了两个字段token和expires:
 * token：授权用户的token
 * expires:token过期时间
 * 使用这两个字段可以生成一个新浪微博API的授权token
 */
public class AuthUser{
    private static final long serialVersionUID = 1L;
    private WeiboUser user;
    public String expires;//token过期时间



    public AuthUser(WeiboUser user){
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String token;//授权token

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }
    public WeiboUser getUser() {
        return user;
    }

    public void setUser(WeiboUser user) {
        this.user = user;
    }
}
