package com.xiaoming.random.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;
import com.xiaoming.random.activities.BaseActivity;
import com.xiaoming.random.fragments.MainTimeLineFragment;
import com.xiaoming.random.fragments.UserProfileFragment;
import com.xiaoming.random.model.AuthUser;
import com.xiaoming.random.model.Emotion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatusDao {
    private UserSQLiteDBHelper mHelper;
    private long mAuthUserID;

    public StatusDao(Context context) {
//        System.out.println(context==null);
        mHelper = new UserSQLiteDBHelper(context);
        mAuthUserID = getAuthUserID(context);
    }

    /**
     * 获取数据库文件路径
     *
     * @return
     */
    public String getDatabasePath() {
        return mHelper.getReadableDatabase().getPath();
    }

    /**
     * 授权用户id
     *
     * @return 授权用户id
     */
    public long getAuthUserID(Context context) {
        SharedPreferences sp = context.getSharedPreferences(BaseActivity.USER_PREFERENCES, Context.MODE_PRIVATE);
        return sp.getLong(BaseActivity.SF_USER_UID, 0);
    }

    /**
     * 清除所有缓存
     */
    public void clearDatabase() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Log.v("StatusDao", "Clearing database.");
        db.beginTransaction();
        db.execSQL("delete from status");
        db.execSQL("delete from comments");
        db.execSQL("delete from sta_comments");
        db.execSQL("delete from user");
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * 保存表情
     *
     * @param emotions,新浪微博API返回的表情对象
     */
    public void saveEmotions(String emotions) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            JSONArray jsonArray = new JSONArray(emotions);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                Emotion emotion = Emotion.parse(obj);
//                if (emotion.type!="默认")
                String sql = "insert into emotions (type,value,url,category,phrase) values (?,?,?,?,?)";
                db.execSQL(sql, new String[]{emotion.type, emotion.value, emotion.url, emotion.category, emotion.phrase});
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public List getEmotions() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        List<String> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("select phrase||','||url from emotions where category ='默认'", null);
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return result;
    }


    /**
     * 保存授权用户
     *
     * @param userStr
     */
    public void saveAuthUser(String userStr, Oauth2AccessToken token) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("id", token.getUid());
            values.put("user_str", userStr);
            values.put("token", token.getToken());
            values.put("expires", token.getExpiresTime());
            db.replace("auth_user", null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 查询授权用户列表
     *
     * @return
     */
    public List<AuthUser> getAuthUserList() {
        List<AuthUser> list = new ArrayList<AuthUser>();
        AuthUser user = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select id ,user_str,token,expires from Auth_user ", null);
        while (cursor.moveToNext()) {

            try {
                JSONObject obj = new JSONObject(cursor.getString(cursor
                        .getColumnIndex("user_str")));
                user = new AuthUser(User.parse(obj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            user.token = cursor.getString(cursor.getColumnIndex("token"));
            user.expires = cursor.getString(cursor.getColumnIndex("expires"));
            list.add(user);
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 保存单个用户，在点击昵称时触发单个用户的名字
     *
     * @param userStr
     */
    public void saveSingleUser(String userStr) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            JSONObject json = new JSONObject(userStr);
            User user = User.parse(json);
            db.beginTransaction();
            db.execSQL("delete from user where auth_id = " + mAuthUserID + " and name = '" + user.name + "' and type ='USER'");
            ContentValues values = new ContentValues();
            values.put("id", user.id);
            values.put("user_str", json.toString());
            values.put("type", UserProfileFragment.USER);
            values.put("name", user.name);
            values.put("auth_id", mAuthUserID);
            db.replace("user", null, values);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 保存关注列表中的User
     *
     * @param userStr
     */
    public void saveUser(String userStr, String type) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("delete from user where auth_id = " + mAuthUserID + " and type ='" + type + "'");
            //   deleteExistsUsers(type);
            JSONObject json = new JSONObject(userStr);
            JSONArray array = json.optJSONArray("users");
            //关注列表界面
            if (array != null && array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    ContentValues values = new ContentValues();
                    JSONObject obj = array.optJSONObject(i);
                    User user = User.parse(obj);
                    values.put("id", user.id);
                    values.put("user_str", array.get(i).toString());
                    values.put("type", type);
                    values.put("name", user.name);
                    values.put("auth_id", mAuthUserID);
                    db.replace("user", null, values);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 根据昵称获取User
     *
     * @param
     * @return
     */
    public User getUserByName(String name, String type) {
        Log.e(StatusDao.class.getSimpleName(), "Query user by name : " + name);
        User user = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select id ,user_str from user where auth_id = " + mAuthUserID + " and type = '" + type + "' and name =  '" + name + "'", null);
        while (cursor.moveToNext()) {
            try {
                JSONObject obj = new JSONObject(cursor.getString(cursor
                        .getColumnIndex("user_str")));
                user = User.parse(obj);
                Log.e(StatusDao.class.getSimpleName(), "User : " + user.name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        db.close();
        return user;
    }

    /**
     * 根据ID获取User
     *
     * @param id
     * @return
     */
    public User getUserById(Long id) {
        User user = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select id ,user_str from user where auth_id = " + mAuthUserID + " and id =  " + id, null);
        while (cursor.moveToNext()) {
            try {
                JSONObject obj = new JSONObject(cursor.getString(cursor
                        .getColumnIndex("user_str")));
                user = User.parse(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        db.close();
        return user;
    }

    /**
     * 查询用户User列表
     *
     * @return
     */
    public List<User> getUserList(String type, int length) {
        List<User> list = new ArrayList<>();
        User user = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select user_str from user where auth_id = " + mAuthUserID + " and type = '" + type + "' limit  " + length, null);
        while (cursor.moveToNext()) {
            try {
                JSONObject obj = new JSONObject(cursor.getString(cursor
                        .getColumnIndex("user_str")));
                user = User.parse(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list.add(user);
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 根据ID获取AuthUser
     *
     * @param id
     * @return
     */
    public AuthUser getAuthUser(Long id) {
        AuthUser user = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select id ,user_str,token,expires from auth_user where id =  " + id, null);
        while (cursor.moveToNext()) {

            try {
                JSONObject obj = new JSONObject(cursor.getString(cursor
                        .getColumnIndex("user_str")));
                user = new AuthUser(User.parse(obj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            user.token = cursor.getString(cursor.getColumnIndex("token"));
            user.expires = cursor.getString(cursor.getColumnIndex("expires"));

        }
        cursor.close();
        db.close();
        return user;
    }

    /**
     * 保存微博
     *
     * @param objStr
     */
    public void saveStatus(String objStr, String type) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            //如果是收藏列表，则先删除全部收藏
            if (type.equals(MainTimeLineFragment.STATUS_FAVORITES))
                db.execSQL("delete from status where auth_id = '" + mAuthUserID + "' and type = '" + type + "'");
            JSONObject jsonObject = new JSONObject(objStr);
            JSONArray jsonArray;
            if (type.equals(MainTimeLineFragment.STATUS_FAVORITES)) {
                jsonArray = jsonObject.optJSONArray("favorites");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.optJSONObject(i).optJSONObject("status");
                    long id = Long.parseLong(obj.get("id").toString());
                    String status = obj.toString();
                    ContentValues values = new ContentValues();
                    values.put("id", id);
                    values.put("type", type);
                    values.put("status_str", status);
                    values.put("auth_id", mAuthUserID);
                    db.replace("status", null, values);
                }
            } else {
                jsonArray = jsonObject.optJSONArray("statuses");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    long id = Long.parseLong(obj.get("id").toString());
                    String status = obj.toString();
                    ContentValues values = new ContentValues();
                    values.put("id", id);
                    values.put("type", type);
                    values.put("status_str", status);
                    values.put("auth_id", mAuthUserID);
                    db.replace("status", null, values);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 保存评论
     *
     * @param objStr
     */
    public void saveComments(String objStr, String type) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            JSONObject jsonObject = new JSONObject(objStr);
            JSONArray jsonArray = jsonObject.optJSONArray("comments");

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject obj = jsonArray.getJSONObject(i);
                long id = Long.parseLong(obj.get("id").toString());
                String status = obj.toString();
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("type", type);
                values.put("auth_id", mAuthUserID);
                values.put("comments_str", status);
                db.replace("comments", null, values);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 保存微博评论
     *
     * @param objStr
     */
    public void saveStaComments(String objStr, long status_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            JSONObject jsonObject = new JSONObject(objStr);
            JSONArray jsonArray = jsonObject.optJSONArray("comments");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                long id = Long.parseLong(obj.get("id").toString());
                String status = obj.toString();
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("status_id", status_id);
                values.put("comments_str", status);
                values.put("auth_id", mAuthUserID);
                db.replace("sta_comments", null, values);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

    }

    /**
     * 获取最大微博id
     *
     * @return
     */
    public long getSinceId(String table, String type) {
        long sinceId = 0;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select max(id) id from " + table
                + " where  auth_id = " + mAuthUserID + " and type = '" + type + "'", null);
        while (cursor.moveToNext()) {
            sinceId = cursor.getLong(cursor.getColumnIndex("id"));
        }
        cursor.close();
        db.close();
        return sinceId;
    }

    /**
     * 获取最大微博id
     *
     * @return
     */
    public long getStaCommSinId(String table, long id) {
        long sinceId = 0;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select max(id) id from " + table
                + " where auth_id = " + mAuthUserID + " and status_id = '" + id + "'", null);
        while (cursor.moveToNext()) {
            sinceId = cursor.getLong(cursor.getColumnIndex("id"));
        }
        cursor.close();
        db.close();
        return sinceId;
    }

    /**
     * 读取微博
     *
     * @param length
     * @return
     */
    public List<Status> readStatus(int length, String type) {
        List<Status> staList = new ArrayList<Status>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select id ,type,status_str from status where auth_id = " + mAuthUserID + " and type = '" + type
                        + "' order by id desc limit " + length, null);
        while (cursor.moveToNext()) {
            Status status = Status.parse(cursor.getString(cursor
                    .getColumnIndex("status_str")));
            if (status.user != null)
                staList.add(status);
        }
        cursor.close();
        db.close();
        return staList;
    }

    /**
     * 读取评论
     *
     * @param length
     * @return
     * @throws Exception
     */
    public List<Comment> readComments(int length, String type) throws Exception {
        List<Comment> staList = new ArrayList<Comment>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select comments_str from comments where auth_id = " + mAuthUserID + " and type = '" + type
                        + "' order by id desc limit " + length, null);
        while (cursor.moveToNext()) {
            Comment comment = Comment.parse(new JSONObject(cursor
                    .getString(cursor.getColumnIndex("comments_str"))));
            staList.add(comment);
        }
        cursor.close();
        db.close();
        return staList;
    }

    /**
     * 读取微博评论
     *
     * @param length
     * @return
     * @throws Exception
     */
    public List<Comment> readStaComments(int length, long statusId)
            throws Exception {
        List<Comment> staList = new ArrayList<Comment>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select comments_str from sta_comments where auth_id = " + mAuthUserID + " and status_id = "
                        + statusId + " order by id desc limit " + length, null);
        while (cursor.moveToNext()) {
            Comment comment = Comment.parse(new JSONObject(cursor
                    .getString(cursor.getColumnIndex("comments_str"))));
            staList.add(comment);
        }
        cursor.close();
        db.close();
        return staList;
    }
}
