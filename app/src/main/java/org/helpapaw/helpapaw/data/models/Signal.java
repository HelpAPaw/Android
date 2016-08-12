package org.helpapaw.helpapaw.data.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iliyan on 7/28/16
 */
public class Signal implements Parcelable{

    private String id;
    private String title;
    private String dateSubmitted;
    private String authorName;
    private String authorPhone;
    private String photoUrl;
    private int status;
    private double latitude;
    private double longitude;

    public Signal(String id, String title, String dateSubmitted, int status, String authorName, String authorPhone, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.dateSubmitted = dateSubmitted;
        this.authorName = authorName;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.authorPhone = authorPhone;
    }


    public Signal(String title, String dateSubmitted, int status, double latitude, double longitude) {
        this.title = title;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Signal(String title, String dateSubmitted, int status, String authorName, String authorPhone, double latitude, double longitude) {
        this.title = title;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.authorName = authorName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.authorPhone = authorPhone;
    }

    protected Signal(Parcel in) {
        id = in.readString();
        title = in.readString();
        dateSubmitted = in.readString();
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

    public String getDateSubmitted() {
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
        dest.writeString(dateSubmitted);
        dest.writeString(authorName);
        dest.writeString(authorPhone);
        dest.writeString(photoUrl);
        dest.writeInt(status);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
