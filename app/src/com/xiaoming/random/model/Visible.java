package com.xiaoming.random.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by XIAOM on 2015/3/14.
 */
public class Visible implements Serializable {
    private static final long serialVersionUID = 2565955382087762795L;
    public static final int VISIBLE_NORMAL  = 0;
    public static final int VISIBLE_PRIVACY = 1;
    public static final int VISIBLE_GROUPED = 2;
    public static final int VISIBLE_FRIEND  = 3;

    /** type 取值，0：普通微博，1：私密微博，3：指定分组微博，4：密友微博 */
    public int type;
    /** 分组的组号 */
    public int listId;

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{").append("\"type\":\"").append(this.type).append("\",")
         .append("\"list_id\":\"").append(this.listId).append("\"}");
        return s.toString();
    }

    public static Visible parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        Visible visible = new Visible();
        visible.type    = jsonObject.optInt("type", 0);
        visible.listId = jsonObject.optInt("list_id", 0);
        return visible;
    }
}
