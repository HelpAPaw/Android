package org.helpapaw.helpapaw.data.models;

import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by iliyan on 8/4/16
 */
public class Comment {
    private final static String TAG = Comment.class.getSimpleName();

    public final static String COMMENT_TYPE_USER_COMMENT = "user_comment";
    public final static String COMMENT_TYPE_STATUS_CHANGE = "status_change";

    private String objectId;
    private String ownerName;
    private Date   dateCreated;
    private String text;
    private String type;

    public Comment(String objectId, String ownerName, Date dateCreated, String text, String type) {
        this.objectId = objectId;
        this.ownerName = ownerName;
        this.dateCreated = dateCreated;
        this.text = text;
        this.type = type;

        if (this.dateCreated == null) {
            this.dateCreated = new Date(0);
        }

        if (this.type == null) {
            this.type = "";
        }
    }

    public String getObjectId() {
        return objectId;
    }

    public String getText() {
        return text;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getType() { return type; }

    public static int getNewStatusFromStatusChangeComment(Comment comment) {
        int newStatus = 0;
        try {
            JSONObject json = new JSONObject(comment.getText());
            newStatus = json.getInt("new");
        }
        catch (Exception ex) {
            Log.d(TAG, String.format("Failed to parse new status from comment. Not a status change comment? %s", ex.getMessage()));
        }

        return newStatus;
    }
}
