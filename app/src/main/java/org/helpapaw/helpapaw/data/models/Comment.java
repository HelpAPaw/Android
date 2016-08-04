package org.helpapaw.helpapaw.data.models;

/**
 * Created by iliyan on 8/4/16
 */
public class Comment {
    private String objectId;
    private String ownerName;
    private String dateCreated;
    private String text;

    public Comment(String objectId, String ownerName, String dateCreated, String text) {
        this.objectId = objectId;
        this.ownerName = ownerName;
        this.dateCreated = dateCreated;
        this.text = text;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getText() {
        return text;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
