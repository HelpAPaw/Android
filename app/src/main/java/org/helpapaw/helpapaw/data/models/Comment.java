package org.helpapaw.helpapaw.data.models;

/**
 * Created by iliyan on 8/4/16
 */
public class Comment {
    private String objectId;
    private String ownerFirstName;
    private String ownerLastName;
    private String dateCreated;
    private String text;

    public Comment(String objectId, String ownerFirstName, String ownerLastName, String dateCreated, String text) {
        this.objectId = objectId;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
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
}
