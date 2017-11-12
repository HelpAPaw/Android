package org.helpapaw.helpapaw.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by iliyan on 7/28/16
 * edit: Alex-11.11.17
 */

@Entity(tableName = "signals")
public class Signal implements Parcelable {
    // The key used to keep the Id of the signal that should be focused
    @Ignore
    public static String KEY_FOCUSED_SIGNAL_ID = "FocusSignalId";
    @Ignore
    public static final int SOLVED = 2;
    @Ignore
    public static final int SOMEBODY_ON_THE_WAY = 1;
    @Ignore
    public static final int HELP_IS_NEEDED = 0;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "signal_id")
    private String id;
    @ColumnInfo(name = "title")
    private String title;
    @Ignore
    private Date dateSubmitted;
    @ColumnInfo(name = "authorName")
    private String authorName;
    @ColumnInfo(name = "authorPhone")
    private String authorPhone;
    @ColumnInfo(name = "photoUrl")
    private String photoUrl;
    @ColumnInfo(name = "status")
    private int status;
    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "longitude")
    private double longitude;

    public Signal() {
    }

    @Ignore
    public Signal(String id, String title, Date dateSubmitted, int status, String authorName, String authorPhone, double latitude, double longitude) {
        this(title, dateSubmitted, status, authorName, authorPhone, latitude, longitude);
        this.id = id;
    }

    @Ignore
    public Signal(String title, Date dateSubmitted, int status, double latitude, double longitude) {
        this.title = title;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;

        if (dateSubmitted != null) {
            this.dateSubmitted = dateSubmitted;
        } else {
            this.dateSubmitted = new Date(0);
        }
    }

    @Ignore
    public Signal(String title, Date dateSubmitted, int status, String authorName, String authorPhone, double latitude, double longitude) {
        this(title, dateSubmitted, status, latitude, longitude);
        this.authorName = authorName;
        this.authorPhone = authorPhone;
    }

    @Ignore
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

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorPhone(String authorPhone) {
        this.authorPhone = authorPhone;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

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
