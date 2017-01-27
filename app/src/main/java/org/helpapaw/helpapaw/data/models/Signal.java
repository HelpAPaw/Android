package org.helpapaw.helpapaw.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by iliyan on 7/28/16
 */
public class Signal implements Parcelable{
    public static String KEY_SIGNAL ="signal";
    private String id;
    private String title;
    private Date   dateSubmitted;
    private String authorName;
    private String authorPhone;
    private String photoUrl;
    private int    status;
    private double latitude;
    private double longitude;

    public Signal(String id, String title, Date dateSubmitted, int status, String authorName, String authorPhone, double latitude, double longitude) {
        this(title, dateSubmitted, status, authorName, authorPhone, latitude, longitude);
        this.id = id;
    }


    public Signal(String title, Date dateSubmitted, int status, double latitude, double longitude) {
        this.title = title;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;

        if (dateSubmitted != null) {
            this.dateSubmitted = dateSubmitted;
        }
        else {
            this.dateSubmitted = new Date(0);
        }
    }

    public Signal(String title, Date dateSubmitted, int status, String authorName, String authorPhone, double latitude, double longitude) {
        this(title, dateSubmitted, status, latitude, longitude);
        this.authorName = authorName;
        this.authorPhone = authorPhone;
    }

    protected Signal(Parcel in) {
        id = in.readString();
        title = in.readString();
        dateSubmitted = new Date(in.readLong());
        authorName = in.readString();
        authorPhone = in.readString();
        photoUrl = in.readString();
        status = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Signal> CREATOR = new Creator<Signal>() {
        @Override
        public Signal createFromParcel(Parcel in) {
            return new Signal(in);
        }

        @Override
        public Signal[] newArray(int size) {
            return new Signal[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public int getStatus() {
        return status;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAuthorPhone() {
        return authorPhone;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeLong(dateSubmitted.getTime());
        dest.writeString(authorName);
        dest.writeString(authorPhone);
        dest.writeString(photoUrl);
        dest.writeInt(status);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
