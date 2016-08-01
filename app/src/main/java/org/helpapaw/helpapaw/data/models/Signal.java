package org.helpapaw.helpapaw.data.models;

/**
 * Created by iliyan on 7/28/16
 */
public class Signal {

    private String id;
    private String title;
    private String dateSubmitted;
    private int status;
    private double latitude;
    private double longitude;

    public Signal(String id, String title, String dateSubmitted, int status, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public Signal(String title, String dateSubmitted, int status, double latitude, double longitude) {
        this.title = title;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

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
}
