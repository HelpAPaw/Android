package org.helpapaw.helpapaw.data.repositories;

import android.content.SharedPreferences;
import android.util.Log;

public class SettingsRepository implements ISettingsRepository {

    private static String TAG = SettingsRepository.class.getSimpleName();

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
        editor.putInt("radius", timeout);
        editor.apply();
    }

    @Override
    public int getRadius() {
        return preferences.getInt("radius", 0);
    }

    @Override
    public int getTimeout() {
        return preferences.getInt("timeout", 0);
    }
}
