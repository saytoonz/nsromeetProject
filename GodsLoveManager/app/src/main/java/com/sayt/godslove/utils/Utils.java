package com.sayt.godslove.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Utils {

    public static void shortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void saveInSharepref_String(Context context, String name, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(name, value).apply();
    }

    public static String getShareprefValue_String(Context context, String name) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(name, "");
    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
