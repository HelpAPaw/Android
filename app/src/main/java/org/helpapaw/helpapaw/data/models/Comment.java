package org.helpapaw.helpapaw.data.models;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
    private String authorId;
    private String authorName;
    private String signalId;
    private Date   dateCreated;
    private String text;
    private String type;
    private String photoUrl;

    public Comment(String objectId, String signalId, String authorId, String authorName, String photoUrl, Date dateCreated, String text, String type) {
        this.objectId = objectId;
        this.signalId = signalId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.photoUrl = photoUrl;
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

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getSignalId() {
        return signalId;
    }

    public String getType() { return type; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public static int getNewStatusFromStatusChangeComment(Comment comment) {
        int newStatus = 0;
        try {
            JSONObject json = new JSONObject(comment.getText());
            newStatus = json.getInt("new");
        }
        catch (Exception ex) {
            // Failed to parse new status from comment. Not a status change comment?
            FirebaseCrashlytics.getInstance().recordException(ex);
        }

        return newStatus;
    }
}
