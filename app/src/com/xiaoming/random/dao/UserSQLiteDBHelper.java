package com.xiaoming.random.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserSQLiteDBHelper extends SQLiteOpenHelper {

    public UserSQLiteDBHelper(Context context) {
        super(context, "status_obj.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        String sql = "create table userToken(uid integer primary key,token varchar(100),expires_date integer)";
        arg0.execSQL(sql);
        sql = "create table status(id number(32),auth_id number(32),type varchar(32),status_str varchar(4000))";
        arg0.execSQL(sql);
        sql = "create table comments(id number(32),auth_id number(32),type varchar(32),status_id number(32),comments_str varchar(4000))";
        arg0.execSQL(sql);
        sql = "create table sta_comments(id number(32),auth_id number(32),status_id number(32),comments_str varchar(4000))";
        arg0.execSQL(sql);
        sql = "create table auth_user(id number(32) primary key,auth_id number(32),user_str varchar(4000),token varchar(100),expires number(64))";
        arg0.execSQL(sql);
        sql = "create table user(id number(32),user_str varchar(4000),auth_id number(32),type varchar(32),name varchar(100))";
        arg0.execSQL(sql);
        sql = "create table emotions(id number(32),value varchar(32),auth_id number(32),url varchar(256),type varchar(16),category varchar(32),phrase varchar(32))";
        arg0.execSQL(sql);
//        arg0.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
