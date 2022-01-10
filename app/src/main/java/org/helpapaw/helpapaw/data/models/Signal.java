package org.helpapaw.helpapaw.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Date;

/**
 * Created by iliyan on 7/28/16
 * edit: Alex-11.11.17
 */

@Entity(tableName = "signals")
public class Signal implements Parcelable {
    // The key used to keep the Id of the signal that should be focused
    @Ignore
    public static String    KEY_SIGNAL_ID       = "signalId";
    @Ignore
    public static final int SOLVED              = 2;
    @Ignore
    public static final int SOMEBODY_ON_THE_WAY = 1;
    @Ignore
    public static final int HELP_IS_NEEDED      = 0;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "signal_id")
    private String id;
    @ColumnInfo(name = "title")
    private String title;
    @Ignore
    private Date dateSubmitted;
    @ColumnInfo(name = "author_id")
    private String authorId;
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
    @ColumnInfo(name = "seen")
    private boolean seen;
    @ColumnInfo(name = "signalType")
    private int type;
    @ColumnInfo(name = "isDeleted")
    private boolean isDeleted;


    public Signal() {
    }

    @Ignore
    public Signal(@NonNull String id, String title, Date dateSubmitted, int status, String authorId,
                  String authorName, String authorPhone, double latitude, double longitude,
                  boolean seen, int type) {
        this(title, dateSubmitted, status, authorId, authorName, authorPhone, latitude, longitude, type);
        this.id = id;
        this.seen = seen;
    }

    @Ignore
    public Signal(String title, String authorPhone, Date dateSubmitted, int status, double latitude,
                  double longitude, int type) {
        this.title = title;
        this.authorPhone = authorPhone;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;

        if (dateSubmitted != null) {
            this.dateSubmitted = dateSubmitted;
        } else {
            this.dateSubmitted = new Date(0);
        }
    }

    @Ignore
    public Signal(String title, Date dateSubmitted, int status, String authorId, String authorName,
                  String authorPhone, double latitude, double longitude, int type) {
        this(title, authorPhone, dateSubmitted, status, latitude, longitude, type);
        this.authorId = authorId;
        this.authorName = authorName;
    }

    @Ignore
    protected Signal(Parcel in) {
        id = in.readString();
        title = in.readString();
        dateSubmitted = new Date(in.readLong());
        authorId = in.readString();
        authorName = in.readString();
        authorPhone = in.readString();
        photoUrl = in.readString();
        status = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        seen = in.readByte() != 0;
        isDeleted = in.readByte() != 0;
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeLong(dateSubmitted.getTime());
        dest.writeString(authorId);
        dest.writeString(authorName);
        dest.writeString(authorPhone);
        dest.writeString(photoUrl);
        dest.writeInt(status);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeByte((byte) (seen ? 1 : 0));
        dest.writeByte((byte) (isDeleted ? 1 : 0));
        dest.writeInt(type);
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

    @Override
    public int describeContents() {
        return 0;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
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

    @NonNull
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

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean getSeen() { return seen; }

    public void setSeen(boolean seen) { this.seen = seen; }

    public int getType() { return type; }

    public void setType(int type) { this.type = type; }
}
