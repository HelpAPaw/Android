package org.helpapaw.helpapaw.data.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VetClinic implements Parcelable {

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String address;

    public VetClinic(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static final Creator<VetClinic> CREATOR = new Creator<VetClinic>() {
        @Override
        public VetClinic createFromParcel(Parcel in) {
            return new VetClinic(in);
        }

        @Override
        public VetClinic[] newArray(int size) {
            return new VetClinic[size];
        }
    };

    protected VetClinic(Parcel in) {
        id = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        phoneNumber = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(phoneNumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
