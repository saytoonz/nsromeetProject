package com.nsromapa.nsromeet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nsromapa.nsromeet.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String normalizePhoneNumber(String number, String country_code) {
        number = number.replaceAll("[^+0-9]", ""); // All weird characters such as /, -, ...
        if (number.substring(0, 1).compareTo("0") == 0 && number.substring(1, 2).compareTo("0") != 0) {
            number = "+" + country_code + number.substring(1); // e.g. 0172 12 34 567 -> + (country_code) 172 12 34 567
        }
        number = number.replaceAll("^[0]{1,4}", "+");// e.g. 004912345678 -> +4912345678
        return number;
    }


    public static void SharedPrefSchool(Context context, JSONObject object) {
        try {
            saveInSharepref_String(context, "school", object.getString("id"));
            saveInSharepref_String(context, "schoolName", object.getString("name"));
            saveInSharepref_String(context, "schoolLocation", object.getString("location"));
            saveInSharepref_String(context, "schoolConference", object.getString("conference_id"));
            saveInSharepref_String(context, "schoolCreated_by", object.getString("created_by"));
            saveInSharepref_String(context, "schoolDate_added", object.getString("date_added"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public static void SharedPrefFaculty(Context context, JSONObject object) {
        try {
            saveInSharepref_String(context, "faculty", object.getString("id"));
            saveInSharepref_String(context, "facultyName", object.getString("name"));
            saveInSharepref_String(context, "facultySchool", object.getString("school"));
            saveInSharepref_String(context, "facultyConference", object.getString("conference_id"));
            saveInSharepref_String(context, "facultyCreated_by", object.getString("created_by"));
            saveInSharepref_String(context, "facultyDate_added", object.getString("date_added"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void SharedPrefDepartment(Context context, JSONObject object) {
        try {
            saveInSharepref_String(context, "department", object.getString("id"));
            saveInSharepref_String(context, "departmentName", object.getString("name"));
            saveInSharepref_String(context, "departmentSchool", object.getString("school"));
            saveInSharepref_String(context, "departmentFaculty", object.getString("faculty"));
            saveInSharepref_String(context, "departmentConference", object.getString("conference_id"));
            saveInSharepref_String(context, "departmentCreated_by", object.getString("created_by"));
            saveInSharepref_String(context, "departmentDate_added", object.getString("date_added"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void SharedPrefClass(Context context, JSONObject object) {
        try {
            saveInSharepref_String(context, "class", object.getString("id"));
            saveInSharepref_String(context, "className", object.getString("name"));
            saveInSharepref_String(context, "classDepartment", object.getString("department"));
            saveInSharepref_String(context, "classSchool", object.getString("school"));
            saveInSharepref_String(context, "classFaculty", object.getString("faculty"));
            saveInSharepref_String(context, "classConference", object.getString("conference_id"));
            saveInSharepref_String(context, "classCreated_by", object.getString("created_by"));
            saveInSharepref_String(context, "classDate_added", object.getString("date_added"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
