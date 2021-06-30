package com.nsromapa.nsromeet.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class SettingsUtils {

    public static void saveInSharepref_Bool(Context context, String name, Boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(name, value).apply();
    }


    public static Boolean getShareprefValue_Bool(Context context, String name) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(name, false);
    }

    public static void saveInSharepref_String(Context context, String name, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(name, value).apply();
    }
    public static String getShareprefValue_String(Context context, String name) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(name, "");
    }
}
