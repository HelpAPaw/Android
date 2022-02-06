package org.helpapaw.helpapaw.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;

public class SettingsRepository implements ISettingsRepository {

    private final static String RADIUS_FIELD = "signalRadius";
    private final static String TIMEOUT_FIELD = "signalTimeout";
    private final static String SIGNAL_TYPES_FIELD = "signalTypes";
    private final static String LANGUAGE_FIELD = "language";
    private final static String LAST_SHOWN_LATITUDE_FIELD = "lastShownLatitude";
    private final static String LAST_SHOWN_LONGITUDE_FIELD = "lastShownLongitude";
    private final static String LAST_SHOWN_ZOOM_FIELD = "lastShownZoom";
    private final static String HAS_SHOWN_FOREGROUND_LOCATION_RATIONALE = "HAS_SHOWN_FOREGROUND_LOCATION_RATIONALE";
    private final static String HAS_SHOWN_BACKGROUND_LOCATION_RATIONALE = "HAS_SHOWN_BACKGROUND_LOCATION_RATIONALE";
    private final static String HAS_DENIED_FOREGROUND_LOCATION_RATIONALE = "HAS_DENIED_FOREGROUND_LOCATION_RATIONALE";
    private final static String HAS_DENIED_BACKGROUND_LOCATION_RATIONALE = "HAS_DENIED_BACKGROUND_LOCATION_RATIONALE";
    private final static String HAS_SHOWN_HIBERNATION_EXEMPTION_DIALOG = "HAS_SHOWN_HIBERNATION_EXEMPTION_DIALOG";
    private final static String DEVICE_BACKENDLESS_TOKEN = "deviceBackendlessToken";

    private final SharedPreferences preferences;

    public SettingsRepository() {

        //for (app) preferences use getDefaultSharedPreferences() (see doc)
        Context context = PawApplication.getContext();
        this.preferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_for_app), Context.MODE_PRIVATE);
    }

    @Override
    public void saveRadius(int radius) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(RADIUS_FIELD, radius);
        editor.apply();
    }

    @Override
    public void saveTimeout(int timeout) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(TIMEOUT_FIELD, timeout);
        editor.apply();
    }

    @Override
    public void saveSignalTypes(int signalTypes) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SIGNAL_TYPES_FIELD, signalTypes);
        editor.apply();
    }

    @Override
    public void saveLanguage(int languageIndex) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LANGUAGE_FIELD, languageIndex);
        editor.apply();
    }

    @Override
    public int getRadius() {
        return preferences.getInt(RADIUS_FIELD, 10);
    }

    @Override
    public int getTimeout() {
        return preferences.getInt(TIMEOUT_FIELD, 7);
    }

    @Override
    public int getSignalTypes() {
        return preferences.getInt(SIGNAL_TYPES_FIELD, 65535);
    }

    @Override
    public int getLanguageIndex() {
        return preferences.getInt(LANGUAGE_FIELD, 0);
    }

    @Override
    public double getLastShownLatitude() {
        String lat = preferences.getString(LAST_SHOWN_LATITUDE_FIELD, "0");
        return Double.parseDouble(lat);
    }

    @Override
    public void setLastShownLatitude(double latitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_SHOWN_LATITUDE_FIELD, String.valueOf(latitude));
        editor.apply();
    }

    @Override
    public double getLastShownLongitude() {
        String longi = preferences.getString(LAST_SHOWN_LONGITUDE_FIELD, "0");
        return Double.parseDouble(longi);
    }

    @Override
    public void setLastShownLongitude(double longitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_SHOWN_LONGITUDE_FIELD, String.valueOf(longitude));
        editor.apply();
    }

    @Override
    public float getLastShownZoom() {
        return preferences.getFloat(LAST_SHOWN_ZOOM_FIELD, 0f);
    }

    @Override
    public void setLastShownZoom(float zoom) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(LAST_SHOWN_ZOOM_FIELD, zoom);
        editor.apply();
    }

    @Override
    public void clearLocationData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(LAST_SHOWN_LATITUDE_FIELD);
        editor.remove(LAST_SHOWN_LONGITUDE_FIELD);
        editor.remove(LAST_SHOWN_ZOOM_FIELD);
        editor.commit();
    }

    //Save Backendless device-token to local-preferences
    @Override
    public void saveTokenToPreferences(String deviceToken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DEVICE_BACKENDLESS_TOKEN, deviceToken);
        editor.apply();
    }

    @Override
    public void deleteTokenFromPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(DEVICE_BACKENDLESS_TOKEN);
        editor.apply();
    }

    //Get Backendless device-token from local-preferences
    @Override
    public String getTokenFromPreferences() {
        return preferences.getString(DEVICE_BACKENDLESS_TOKEN, "-1");
    }

    @Override
    public boolean getHasShownForegroundLocationRationale() {
        return preferences.getBoolean(HAS_SHOWN_FOREGROUND_LOCATION_RATIONALE, false);
    }

    @Override
    public void setHasShownForegroundLocationRationale(boolean newValue) {
        preferences.edit().putBoolean(HAS_SHOWN_FOREGROUND_LOCATION_RATIONALE, newValue).apply();
    }

    @Override
    public boolean getHasShownBackgroundLocationRationale() {
        return preferences.getBoolean(HAS_SHOWN_BACKGROUND_LOCATION_RATIONALE, false);
    }

    @Override
    public void setHasShownBackgroundLocationRationale(boolean newValue) {
        preferences.edit().putBoolean(HAS_SHOWN_BACKGROUND_LOCATION_RATIONALE, newValue).apply();
    }

    @Override
    public boolean getHasDeniedForegroundLocationRationale() {
        return preferences.getBoolean(HAS_DENIED_FOREGROUND_LOCATION_RATIONALE, false);
    }

    @Override
    public void setHasDeniedForegroundLocationRationale(boolean newValue) {
        preferences.edit().putBoolean(HAS_DENIED_FOREGROUND_LOCATION_RATIONALE, newValue).apply();
    }

    @Override
    public boolean getHasDeniedBackgroundLocationRationale() {
        return preferences.getBoolean(HAS_DENIED_BACKGROUND_LOCATION_RATIONALE, false);
    }

    @Override
    public void setHasDeniedBackgroundLocationRationale(boolean newValue) {
        preferences.edit().putBoolean(HAS_DENIED_BACKGROUND_LOCATION_RATIONALE, newValue).apply();
    }

    @Override
    public boolean getHasShownHibernationExemptionDialog() {
        return preferences.getBoolean(HAS_SHOWN_HIBERNATION_EXEMPTION_DIALOG, false);
    }

    @Override
    public void setHasShownHibernationExemptionDialog(boolean newValue) {
        preferences.edit().putBoolean(HAS_SHOWN_HIBERNATION_EXEMPTION_DIALOG, newValue).apply();
    }
}
