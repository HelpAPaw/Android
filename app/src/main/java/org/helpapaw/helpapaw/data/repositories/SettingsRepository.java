package org.helpapaw.helpapaw.data.repositories;

import android.content.SharedPreferences;

public class SettingsRepository implements ISettingsRepository {

    private SharedPreferences preferences;

    public SettingsRepository(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void saveRadius(int radius) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("radius", radius);
        editor.apply();
    }

    @Override
    public void saveTimeout(int timeout) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("timeout", timeout);
        editor.apply();
    }

    @Override
    public int getRadius() {
        return preferences.getInt("radius", 10);
    }

    @Override
    public int getTimeout() {
        return preferences.getInt("timeout", 7);
    }

    @Override
    public double getLastShownLatitude() {
        String lat = preferences.getString("latitude", "0");
        return Double.valueOf(lat);
    }

    @Override
    public void setLastShownLatitude(double latitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("latitude", String.valueOf(latitude));
        editor.apply();
    }

    @Override
    public double getLastShownLongitude() {
        String longi = preferences.getString("longitude", "0");
        return Double.valueOf(longi);
    }

    @Override
    public void setLastShownLongitude(double longitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("longitude", String.valueOf(longitude));
        editor.apply();
    }

    @Override
    public float getLastShownZoom() {
        return preferences.getFloat("zoom", 0f);
    }

    @Override
    public void setLastShownZoom(float zoom) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("zoom", zoom);
        editor.apply();
    }

    @Override
    public void clearLocationData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("longitude");
        editor.remove("latitude");
        editor.remove("zoom");
        editor.apply();
    }
}
