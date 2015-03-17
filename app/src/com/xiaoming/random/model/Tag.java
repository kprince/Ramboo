package com.xiaoming.random.model;

import org.json.JSONObject;

/**
 * Created by XIAOM on 2015/3/15.
 */
public class Tag {
    /** type 取值，0：普通微博，1：私密微博，3：指定分组微博，4：密友微博 */
    public int id;
    /** 分组的组号 */
    public String tag;

    @Override
    public String toString() {
        return new StringBuilder().append("{").append("\"id\":\"")
                .append("\",").append(id).append("\"tag\":\"")
                .append("\",").append("}").toString();
    }

    public static Tag parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }

        Tag tag = new Tag();
        tag.id  = jsonObject.optInt("id", 0);
        tag.tag = jsonObject.optString("tag", "");

        return tag;
    }
}
