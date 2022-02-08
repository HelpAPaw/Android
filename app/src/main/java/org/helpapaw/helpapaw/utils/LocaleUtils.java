package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import androidx.annotation.StringDef;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

public class LocaleUtils {

    public static final String ENGLISH = "en";
    public static final String BULGARIAN = "bg";

    public static void initialize(Context context, @LocaleDef String defaultLanguage) {
        setLocale(context, defaultLanguage);
    }

    public static boolean setLocale(Context context, @LocaleDef String language) {
        return updateResources(context, language);
    }

    private static boolean updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        context.createConfigurationContext(configuration);
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return true;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ENGLISH, BULGARIAN})
    public @interface LocaleDef {
        String[] SUPPORTED_LOCALES = {ENGLISH, BULGARIAN};
    }


    private static SharedPreferences getDefaultSharedPreference(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(PawApplication.getContext()) != null)
            return PreferenceManager.getDefaultSharedPreferences(PawApplication.getContext());
        else
            return null;
    }

    public static void setSelectedLanguageId(String id){
        Context context = PawApplication.getContext();
        final SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_for_app), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language_id", id);
        editor.apply();
    }

    public static String getSelectedLanguageId(){
        return getDefaultSharedPreference(PawApplication.getContext())
                .getString("language", "en");
    }
}
