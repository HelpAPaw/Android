package org.helpapaw.helpapaw.data.models;

import java.util.Date;

/**
 * Created by iliyan on 8/4/16
 */
public class Comment {
    private String objectId;
    private String ownerName;
    private Date   dateCreated;
    private String text;

    public Comment(String objectId, String ownerName, Date dateCreated, String text) {
        this.objectId = objectId;
        this.ownerName = ownerName;
        this.dateCreated = dateCreated;
        this.text = text;

        if (dateCreated != null) {
            this.dateCreated = dateCreated;
        }
        else {
            this.dateCreated = new Date(0);
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
}
