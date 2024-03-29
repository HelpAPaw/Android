package org.helpapaw.helpapaw.data.models.backendless;

import com.backendless.BackendlessUser;

/**
 * Created by iliyan on 8/4/16
 */
public class FINComment {

    private String objectId;
    private String signalID;
    private String text;
    private String created;
    private String type;
    private BackendlessUser author;
    private String photo;

    public FINComment() {
    }

    public FINComment(String text, String signalID, String type, BackendlessUser author) {
        this.text = text;
        this.author = author;
        this.signalID = signalID;
        this.type = type;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getText() {
        return text;
    }

    public String getCreated() {
        return created;
    }

    public BackendlessUser getAuthor() {
        return author;
    }

    public String getPhoto() {
        return photo;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setAuthor(BackendlessUser author) {
        this.author = author;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSignalID() {
        return signalID;
    }

    public void setSignalID(String signalID) {
        this.signalID = signalID;
    }

    public String getType() {
        return type;
    }
}
