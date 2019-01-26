package org.helpapaw.helpapaw.data.repositories;

import android.content.SharedPreferences;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class SettingsRepository implements ISettingsRepository {

    private final static String RADIUS_FIELD = "signalRadius";
    private final static String TIMEOUT_FIELD = "signalTimeout";
    private final static String LAST_SHOWN_LATITUDE_FIELD = "lastShownLatitude";
    private final static String LAST_SHOWN_LONGITUDE_FIELD = "lastShownLongitude";
    private final static String LAST_SHOWN_ZOOM_FIELD = "lastShownZoom";

    private SharedPreferences preferences;

    public SettingsRepository(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void saveRadius(int radius) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(RADIUS_FIELD, radius);
        editor.apply();

        // Save in Backendless to use for cloud notifications
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user != null)
        {
            user.setProperty(RADIUS_FIELD, radius);
            Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>()
            {
                public void handleResponse(BackendlessUser user)
                {
                    // Success - do nothing
                }

                public void handleFault(BackendlessFault fault)
                {
                    // Failure - do nothing
                }
            });
        }
    }

    @Override
    public void saveTimeout(int timeout) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(TIMEOUT_FIELD, timeout);
        editor.apply();

        // Save in Backendless to use for cloud notifications
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user != null)
        {
            user.setProperty(TIMEOUT_FIELD, timeout);
            Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>()
            {
                public void handleResponse(BackendlessUser user)
                {
                    // Success - do nothing
                }

                public void handleFault(BackendlessFault fault)
                {
                    // Failure - do nothing
                }
            });
        }
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
    public double getLastShownLatitude() {
        String lat = preferences.getString(LAST_SHOWN_LATITUDE_FIELD, "0");
        return Double.valueOf(lat);
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
        return Double.valueOf(longi);
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
        editor.apply();
    }
}
