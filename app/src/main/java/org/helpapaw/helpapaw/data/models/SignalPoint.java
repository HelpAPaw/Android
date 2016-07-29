package org.helpapaw.helpapaw.data.models;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalPoint {
    private String title;
    private String dateSubmitted;
    private int status;
    private double latitude;
    private double longitude;
    private String author;

    public SignalPoint(String title, String dateSubmitted, int status, double latitude, double longitude, String author) {
        this.title = title;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }
}
