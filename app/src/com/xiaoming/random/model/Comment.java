package com.xiaoming.random.model;

import android.text.TextUtils;

import com.sina.weibo.sdk.openapi.models.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by XIAOM on 2015/3/15.
 */
public class Comment {
    /** 评论创建时间 */
    public String createdAt;
    /** 评论的 ID */
    public String id;
    /** 评论的内容 */
    public String text;
    /** 评论的来源 */
    public String source;
    /** 评论作者的用户信息字段 */
    public User user;
    /** 评论的 MID */
    public String mid;
    /** 字符串型的评论 ID */
    public String idStr;
    /** 评论的微博信息字段 */
    public Status status;
    /** 评论来源评论，当本评论属于对另一评论的回复时返回此字段 */
    public Comment replyComment;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"created_at\":\"").append(createdAt).append("\",").append("\"id\":\"").append(id).append("\",")
          .append("\"text\":\"").append(text).append("\"source\":\"").append(source).append("\",").append("\"user\":\"").append(user).append("\",")
                .append("\"mid\":\"").append(mid).append("\",").append("\"idStr\":\"").append(idStr).append("\",").append("\"status\":\"")
                .append(status).append("\",").append("\"replyComment\":\"").append(replyComment).append("}");
        return sb.toString();
    }

    /**
     * 评论列表
     */
    public  static  class CommentList {

        /** 微博列表 */
        public ArrayList<Comment> commentList;
        public String previousCursor;
        public String nextCursor;
        public int totalNumber;

    }

    /**
     * 解析评论列表
     * @param jsonString
     * @return
     */
    public static CommentList parseList(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        CommentList comments = new CommentList();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            comments.previousCursor = jsonObject.optString("previous_cursor", "0");
            comments.nextCursor     = jsonObject.optString("next_cursor", "0");
            comments.totalNumber    = jsonObject.optInt("total_number", 0);

            JSONArray jsonArray      = jsonObject.optJSONArray("comments");
            if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                comments.commentList = new ArrayList<Comment>(length);
                for (int ix = 0; ix < length; ix++) {
                    comments.commentList.add(Comment.parse(jsonArray.optJSONObject(ix)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * 解析单条评论
     * @param jsonObject
     * @return
     */
    public static Comment parse(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        Comment comment = new Comment();
        comment.createdAt    = jsonObject.optString("created_at");
        comment.id            = jsonObject.optString("id");
        comment.text          = jsonObject.optString("text");
        comment.source        = jsonObject.optString("source");
        comment.user          = User.parse(jsonObject.optJSONObject("user"));
        comment.mid           = jsonObject.optString("mid");
        comment.idStr         = jsonObject.optString("idstr");
        comment.status        = Status.parse(jsonObject.optJSONObject("status"));
        comment.replyComment = Comment.parse(jsonObject.optJSONObject("reply_comment"));
        return comment;
    }
}
