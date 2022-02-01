package org.helpapaw.helpapaw.data.models;

import static java.util.UUID.randomUUID;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

/**
 * Created by niya on 05/10/2021
 */

@Entity(tableName = "notifications")
public class Notification implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "signal_id")
    private String signalId;
    @ColumnInfo(name = "signal_photo_url")
    private String signalPhotoUrl;
    @ColumnInfo(name = "text")
    private String text;
    @TypeConverters({Converters.class})
    private Date dateReceived;

    public Notification() {
    }

    @Ignore
    public Notification(String signalId, String signalPhotoUrl, String text) {
        this.id = randomUUID().toString();
        this.signalId = signalId;
        this.signalPhotoUrl = signalPhotoUrl;
        this.text = text;
        this.dateReceived = new Date();
    }

    @Ignore
    protected Notification(Parcel in) {
        id = in.readString();
        signalId = in.readString();
        signalPhotoUrl = in.readString();
        text = in.readString();
        dateReceived = new Date(in.readLong());
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setSignalId(String signalId) {
        this.signalId = signalId;
    }

    public void setSignalPhotoUrl(String signalPhotoUrl) {
        this.signalPhotoUrl = signalPhotoUrl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getSignalId() {
        return signalId;
    }

    public String getSignalPhotoUrl() {
        return signalPhotoUrl;
    }

    public String getText() {
        return text;
    }

    public Date getDateReceived() {
         return dateReceived;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(signalId);
        dest.writeString(text);
        dest.writeLong(dateReceived.getTime());
    }
}
