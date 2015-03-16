package com.xiaoming.random.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by XIAOM on 2015/3/14.
 */
public class Geo {
    private static final long serialVersionUID = -7036595375395616649L;
    /** 经度坐标 */
    public String longitude;
    /** 维度坐标 */
    public String latitude;
    /** 所在城市的城市代码 */
    public String city;
    /** 所在省份的省份代码 */
    public String province;
    /** 所在城市的城市名称 */
    public String cityName;
    /** 所在省份的省份名称 */
    public String provinceName;
    /** 所在的实际地址，可以为空 */
    public String address;
    /** 地址的汉语拼音，不是所有情况都会返回该字段 */
    public String pinyin;
    /** 更多信息，不是所有情况都会返回该字段 */
    public String more;


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{")
        .append("\"longitude\":\"")    .append(this.longitude   ).append("\",")
                .append("\"latitude\":\"")     .append(this.latitude    ).append("\",")
                .append("\"city\":\"")         .append(this.city        ).append("\",")
                .append("\"province\":\"")     .append(this.province    ).append("\",")
                .append("\"city_name\":\"")    .append(this.cityName    ).append("\",")
                .append("\"province_name\":\"").append(this.provinceName).append("\",")
                .append("\"address\":\"")      .append(this.address     ).append("\",")
                .append("\"pinyin\":\"")       .append(this.pinyin      ).append("\",")
                .append("\"more\":\"")         .append(this.more        ).append("\"}");
        return s.toString();
    }

    public static Geo parse(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        Geo geo = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            geo = parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return geo;
    }

    public static Geo parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }

        Geo geo = new Geo();
        geo.longitude       = jsonObject.optString("longitude");
        geo.latitude        = jsonObject.optString("latitude");
        geo.city            = jsonObject.optString("city");
        geo.province        = jsonObject.optString("province");
        geo.cityName       = jsonObject.optString("city_name");
        geo.provinceName   = jsonObject.optString("province_name");
        geo.address         = jsonObject.optString("address");
        geo.pinyin          = jsonObject.optString("pinyin");
        geo.more            = jsonObject.optString("more");

        return geo;
    }
}
