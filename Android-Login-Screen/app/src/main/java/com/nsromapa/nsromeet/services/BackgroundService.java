package com.nsromapa.nsromeet.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.activities.LoginActivity;
import com.nsromapa.nsromeet.activities.ScheduleActivity;
import com.nsromapa.nsromeet.databases.DBHelper;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.SettingsUtils;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.internal.Util;

import static android.media.RingtoneManager.TYPE_ALARM;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static androidx.core.app.NotificationCompat.BADGE_ICON_SMALL;

public class BackgroundService extends Service {
    private Handler handler;
    private Runnable runnable;
    private RequestQueue requestQueue;
    private String TAG = "BackgroundService";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    private boolean getingBoolean = true;
    private boolean getingGroupBoolean = true;
    private boolean getingAppBoolean = true;
    private boolean getingUserBoolean = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (handler != null & runnable != null)
            handler.removeCallbacks(runnable);

        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleStart(intent, startId);
        return START_STICKY;
    }


    private void handleStart(Intent intent, int startId) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (Utils.isConnectingToInternet(getApplicationContext())) {
                    if (!TextUtils.isEmpty(Utils.getShareprefValue_String(getApplicationContext(), "uuid"))) {
                        getSchedules();
                        getGroups();
                        trackSchedules();
                        userCheck();
                    }

                    getApp();
                }
                handler.postDelayed(this, 15000);
            }
        };
        handler.postDelayed(runnable, 8000);
    }

    private void userCheck() {
        if (!getingUserBoolean)
            return;
        getingUserBoolean = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.USER,
                response -> {
                    Log.e(TAG, "getUser: " + response);
                    Utils.saveInSharepref_String(getApplicationContext(), "user", response);
                    if (!Utils.getShareprefValue_String(getApplicationContext(),"user").equals(response)){
                        try {
                            JSONObject user = new JSONObject(response);
                            String active = user.getString("active");
                            String id_type = user.getString("id_type");
                            String uid = user.getString("uid");
                            String fuid = user.getString("firebase_uid");
                            String uname = user.getString("username");
                            String name = user.getString("name");
                            String phone = user.getString("phone");
                            String image = user.getString("image");
                            String level = user.getString("level");


                            if (user.getString("active").equals("no")){
                                FirebaseAuth.getInstance().signOut();
                                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                                Utils.saveInSharepref_String(getApplicationContext(), "user_active", "finish");
                                return;
                            }
                            if (!Utils.getShareprefValue_String(getApplicationContext(), "id_type").equals(id_type)){
                                Uri sound = RingtoneManager.getActualDefaultRingtoneUri(this, TYPE_NOTIFICATION);
                                long[] vibration = new long[]{500, 1500, 2000, 150};

                                Intent notificationIntent = new Intent(getApplicationContext(), ScheduleActivity.class);
                                notificationIntent.putExtra("title", getApplication().getString(R.string.app_name));
                                notificationIntent.putExtra("backToMain", true);
                                createNotification(notificationIntent, "Your ID has Changed", sound, vibration);
                            }


                            Utils.saveInSharepref_String(getApplicationContext(), "uuid", uid);
                            Utils.saveInSharepref_String(getApplicationContext(), "fuid", fuid);
                            Utils.saveInSharepref_String(getApplicationContext(), "uname", uname);
                            Utils.saveInSharepref_String(getApplicationContext(), "idtype", id_type);
                            Utils.saveInSharepref_String(getApplicationContext(), "level", level);
                            Utils.saveInSharepref_String(getApplicationContext(), "name", name);
                            Utils.saveInSharepref_String(getApplicationContext(), "phone", phone);
                            Utils.saveInSharepref_String(getApplicationContext(), "image", image);
                            Utils.saveInSharepref_String(getApplicationContext(), "active", active);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    getingUserBoolean = true;
                }, error -> {
            getingUserBoolean = true;
            Log.e(TAG, "getUser: " + error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("user", "");
                post.put("uuid", Utils.getShareprefValue_String(getApplicationContext(), "uuid"));
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getApp() {
        if (!getingAppBoolean)
            return;
        getingAppBoolean = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.APP,
                response -> {
                    Log.e(TAG, "getApp: " + response);
                    Utils.saveInSharepref_String(getApplicationContext(), "app", response);
                    getingAppBoolean = true;
                }, error -> {
            getingAppBoolean = true;
            Log.e(TAG, "getApp: " + error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("app", "");
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void trackSchedules() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"id", "forid", "db_id", "title", "conference_id", "date_string", "sched_type", "sched_type_id", "deleted",
                "duration_hours", "duration_minutes", "time_stamp", "host_type", "status", "created_by", "creator_name", "created_idtype", "active"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("schedules", projection, null, null, null, null, sortOrder);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String tid = cursor.getString(cursor.getColumnIndexOrThrow("db_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String conference_id = cursor.getString(cursor.getColumnIndexOrThrow("conference_id"));
            String date_string = cursor.getString(cursor.getColumnIndexOrThrow("date_string"));
            String duration_hours = cursor.getString(cursor.getColumnIndexOrThrow("duration_hours"));
            String duration_minutes = cursor.getString(cursor.getColumnIndexOrThrow("duration_minutes"));
            String time_stamp = cursor.getString(cursor.getColumnIndexOrThrow("time_stamp"));
            String host_type = cursor.getString(cursor.getColumnIndexOrThrow("host_type"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            String forid = cursor.getString(cursor.getColumnIndexOrThrow("forid"));
            String created_by = cursor.getString(cursor.getColumnIndexOrThrow("created_by"));
            String creator_name = cursor.getString(cursor.getColumnIndexOrThrow("creator_name"));
            String sched_type = cursor.getString(cursor.getColumnIndexOrThrow("sched_type"));
            String sched_type_id = cursor.getString(cursor.getColumnIndexOrThrow("sched_type_id"));
            String deleted = cursor.getString(cursor.getColumnIndexOrThrow("deleted"));
            String created_idtype = cursor.getString(cursor.getColumnIndexOrThrow("created_idtype"));
            String active = cursor.getString(cursor.getColumnIndexOrThrow("active"));

            if (forid.equals(Utils.getShareprefValue_String(this, "uuid")) && deleted.equals("no")) {
                if (String.valueOf(System.currentTimeMillis()).substring(0, 10).equals(time_stamp.substring(0, 10))) {

                    Uri sound = RingtoneManager.getActualDefaultRingtoneUri(this, TYPE_ALARM);
                    long[] vibration = new long[]{500, 1500, 2000, 150};

                    if (SettingsUtils.getShareprefValue_Bool(getApplicationContext(), "mute_" + id)) {
                        vibration = null;
                        sound = null;
                    }


                    Intent notificationIntent = new Intent(getApplicationContext(), ScheduleActivity.class);
                    notificationIntent.putExtra("title", getString(R.string.schedules));
                    notificationIntent.putExtra("backToMain", true);
                    createNotification(notificationIntent, "Schedule Time up: " + title, sound, vibration);
                }
            }
        }
        cursor.close();
        db.close();
    }


    private void getSchedules() {
        if (!getingBoolean)
            return;

        getingBoolean = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_USER_SCHEDULES,
                response -> {
                    Log.e(TAG, "getSchedules: " + response);
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONArray array = object.getJSONArray("schedules");

                                DBHelper dbHelper = new DBHelper(this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject schedule = array.getJSONObject(i);
                                    Cursor cursor = db.rawQuery("SELECT * FROM 'schedules' WHERE db_id='"
                                            + schedule.getString("tid") + "'", null);
                                    cursor.moveToFirst();
                                    if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                                        insertSchedule(schedule);
                                    } else {
                                        String status_ = schedule.getString("status");
                                        String active_ = schedule.getString("active");
                                        String active = cursor.getString(cursor.getColumnIndex("active"));
                                        String status = cursor.getString(cursor.getColumnIndex("status"));

                                        if (!status.equalsIgnoreCase(status_) || !active.equalsIgnoreCase(active_)) {
                                            ContentValues values = new ContentValues();
                                            values.put("status", status_);
                                            values.put("active", active_);
                                            String selection = "db_id" + " LIKE ?";
                                            String[] selectionArgs = {schedule.getString("tid")};
                                            db.update("schedules", values, selection, selectionArgs);
                                        }
                                    }
                                    cursor.close();
                                }

                                db.close();
                                getingBoolean = true;

                            } else {
                                Log.e(TAG, "getSchedules: " + object.getString("error_msg"));
                                getingBoolean = true;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            getingBoolean = true;
                        }
                    } else {
                        getingBoolean = true;
                    }

                }, error -> {
            Log.e(TAG, "getSchedules: " + error);
            getingBoolean = true;
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(getApplicationContext(), "uuid"));
                post.put("id_list", getIdLists());
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getGroups() {
        if (!getingGroupBoolean)
            return;
        getingGroupBoolean = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_USER_GROUPS,
                response -> {
                    Log.e(TAG, "getGroups: " + response);

                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONArray array = object.getJSONArray("groups");

                                DBHelper dbHelper = new DBHelper(this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject group = array.getJSONObject(i);
                                    Cursor cursor = db.rawQuery("SELECT * FROM 'groups' WHERE db_id='"
                                            + group.getString("tid") + "'", null);
                                    cursor.moveToFirst();
                                    if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                                        insertGroup(group);
                                    } else {

                                        ContentValues values = new ContentValues();
                                        String tid = group.getString("tid");
                                        String name = group.getString("name");
                                        String description = group.getString("description");
                                        String conference_id = group.getString("conference_id");
                                        String privacy = group.getString("privacy");
                                        String link = group.getString("link");
                                        String active = group.getString("active");

                                        String active_ = cursor.getString(cursor.getColumnIndex("active"));
                                        String name_ = cursor.getString(cursor.getColumnIndex("name"));
                                        String description_ = cursor.getString(cursor.getColumnIndex("description"));
                                        String conference_id_ = cursor.getString(cursor.getColumnIndex("conference_id"));
                                        String privacy_ = cursor.getString(cursor.getColumnIndex("privacy"));
                                        String link_ = cursor.getString(cursor.getColumnIndex("link"));

                                        if (!active.equalsIgnoreCase(active_))
                                            values.put("active", active_);
                                        if (!name.equalsIgnoreCase(name_))
                                            values.put("name", name_);
                                        if (!description.equalsIgnoreCase(description_))
                                            values.put("description", description_);
                                        if (!conference_id.equalsIgnoreCase(conference_id_))
                                            values.put("conference_id", conference_id_);
                                        if (!privacy.equalsIgnoreCase(privacy_))
                                            values.put("privacy", privacy_);
                                        if (!link.equalsIgnoreCase(link_))
                                            values.put("link", link_);

                                        String selection = "db_id" + " LIKE ?";
                                        String[] selectionArgs = {tid};
                                        if (values.size() > 0)
                                            db.update("groups", values, selection, selectionArgs);
                                    }
                                    cursor.close();
                                }

                                db.close();
                                getingGroupBoolean = true;

                            } else {
                                Log.e(TAG, "getGroups: " + object.getString("error_msg"));
                                getingGroupBoolean = true;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            getingGroupBoolean = true;
                        }
                    } else {
                        getingGroupBoolean = true;
                    }

                }, error -> {
            getingGroupBoolean = true;
            Log.e(TAG, "getGroups: " + error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(getApplicationContext(), "uuid"));
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void insertGroup(JSONObject group) {
        try {
            String tid = group.getString("tid");
            String gid = group.getString("gid");
            String name = group.getString("name");
            String description = group.getString("description");
            String conference_id = group.getString("conference_id");
            String privacy = group.getString("privacy");
            String link = group.getString("link");
            String created_by = group.getString("created_by");
            String creator_name = group.getString("creator_name");
            String created_idtype = group.getString("created_idtype");
            String date_added = group.getString("date_added");
            String active = group.getString("active");

            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("forid", Utils.getShareprefValue_String(this, "uuid"));
            values.put("db_id", tid);
            values.put("gid", gid);
            values.put("name", name);
            values.put("description", description);
            values.put("conference_id", conference_id);
            values.put("privacy", privacy);
            values.put("link", link);
            values.put("created_by", created_by);
            values.put("creator_name", creator_name);
            values.put("created_idtype", created_idtype);
            values.put("date_added", date_added);
            values.put("active", active);
            db.insert("groups", null, values);
            db.close();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void insertSchedule(JSONObject schedule) {
        try {
            String tid = schedule.getString("tid");
            String title_ = schedule.getString("title");
            String conference_id = schedule.getString("conference_id");
            String dateString_ = schedule.getString("date_string");
            String duration_hours = schedule.getString("duration_hours");
            String duration_minutes = schedule.getString("duration_minutes");
            String time_stamp = schedule.getString("time_stamp");
            String host_type = schedule.getString("host_type");
            String status = schedule.getString("status");
            String scheduled_with = schedule.getString("scheduled_with");
            String created_by = schedule.getString("created_by");
            String creator_name = schedule.getString("creator_name");
            String sched_type_id = schedule.getString("sched_type_id");
            String sched_type_name = schedule.getString("sched_type_name");
            String sched_type = schedule.getString("sched_type");
            String created_idtype = schedule.getString("created_idtype");
            String active = schedule.getString("active");

            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("forid", Utils.getShareprefValue_String(this, "uuid"));
            values.put("db_id", tid);
            values.put("title", title_);
            values.put("conference_id", conference_id);
            values.put("date_string", dateString_);
            values.put("duration_hours", duration_hours);
            values.put("duration_minutes", duration_minutes);
            values.put("time_stamp", time_stamp);
            values.put("host_type", host_type);
            values.put("status", status);
            values.put("created_by", created_by);
            values.put("creator_name", creator_name);
            values.put("created_idtype", created_idtype);
            values.put("sched_type_id", sched_type_id);
            values.put("sched_type", sched_type);
            values.put("sched_type_name", sched_type_name);
            values.put("deleted", "no");
            values.put("active", active);
            db.insert("schedules", null, values);
            db.close();


            Intent notificationIntent = new Intent(getApplicationContext(), ScheduleActivity.class);
            notificationIntent.putExtra("title", getString(R.string.schedules));
            notificationIntent.putExtra("backToMain", true);

            Uri sound = RingtoneManager.getActualDefaultRingtoneUri(this, TYPE_NOTIFICATION);
            long[] vibration = new long[]{500, 1500, 2000, 150};
            if (SettingsUtils.getShareprefValue_Bool(this, "vibration")) {
                vibration = null;
            }
            if (SettingsUtils.getShareprefValue_Bool(this, "sound")) {
                sound = null;
            }
            createNotification(notificationIntent, getString(R.string.you_have_a_new_schedule), sound, vibration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createNotification(Intent notificationIntent, String body, Uri sound, long[] vibration) {
        int count = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("count", 0);
        count++;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("count", count).apply();


        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), default_notification_channel_id);

        mBuilder.setContentTitle(getString(R.string.app_name));
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentText(body);

        // ICONS
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);

//        Bitmap large_icon_bmp = ((BitmapDrawable) getApplicationContext().getResources()
//                .getDrawable(R.mipmap.ic_launcher_round)).getBitmap();
        Bitmap large_icon_bmp = getBitmapFromDrawable(getApplicationContext().getResources()
                .getDrawable(R.mipmap.ic_launcher_round));
        mBuilder.setLargeIcon(large_icon_bmp);

        mBuilder.setAutoCancel(true);
        mBuilder.setBadgeIconType(BADGE_ICON_SMALL);

        if (vibration != null) {
            mBuilder.setVibrate(vibration);
        }
        if (sound != null)
            mBuilder.setSound(sound);

        mBuilder.setNumber(count);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "NOTIFICATION_CHANNEL_NAME", importance);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0, mBuilder.build());

    }

    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private String getIdLists() {
        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");
        String department_id = Utils.getShareprefValue_String(this, "department");
        String class_id = Utils.getShareprefValue_String(this, "class");

        String idlists = "";

        if (!TextUtils.isEmpty(school_id) && !TextUtils.isEmpty(faculty_id)) {
            String pass = String.format("%s:%s", "FAC", faculty_id);
            if (!idlists.contains(pass)) {
                if (TextUtils.isEmpty(idlists)) idlists = pass;
                else idlists = String.format("%s,%s", idlists, pass);
            }
        }
        if (!TextUtils.isEmpty(school_id) && !TextUtils.isEmpty(faculty_id) && !TextUtils.isEmpty(department_id)) {
            String pass = String.format("%s:%s", "DEP", department_id);
            if (!idlists.contains(pass)) {
                if (TextUtils.isEmpty(idlists)) idlists = pass;
                else idlists = String.format("%s,%s", idlists, pass);
            }
        }
        if (!TextUtils.isEmpty(school_id) && !TextUtils.isEmpty(faculty_id) &&
                !TextUtils.isEmpty(department_id) && !TextUtils.isEmpty(class_id)) {
            String pass = String.format("%s:%s", "CLA", class_id);
            if (!idlists.contains(pass)) {
                if (TextUtils.isEmpty(idlists)) idlists = pass;
                else idlists = String.format("%s,%s", idlists, pass);
            }
        }

        if (getGroupsIdList().size() > 0) {
            for (String tid : getGroupsIdList()) {
                String pass = String.format("%s:%s", "GRO", tid);
                if (!idlists.contains(pass)) {
                    if (TextUtils.isEmpty(idlists)) {
                        idlists = pass;
                    } else {
                        idlists = String.format("%s,%s", idlists, pass);
                    }
                }
            }
        }

        Log.e(TAG, "getIdLists: " + idlists);

        return idlists;
    }

    private List<String> getGroupsIdList() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"forid", "db_id", "active"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("groups", projection, null, null, null, null, sortOrder);

        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String forid = cursor.getString(cursor.getColumnIndexOrThrow("forid"));
            String db_id = cursor.getString(cursor.getColumnIndexOrThrow("db_id"));
            String active = cursor.getString(cursor.getColumnIndexOrThrow("active"));
            if (forid.equals(Utils.getShareprefValue_String(this, "uuid")) && active.equals("yes"))
                list.add(db_id);

        }
        cursor.close();
        db.close();
        return list;

    }
}
