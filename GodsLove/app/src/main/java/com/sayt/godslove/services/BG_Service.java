package com.sayt.godslove.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sayt.godslove.DBHelper;
import com.sayt.godslove.MainActivity;
import com.sayt.godslove.R;
import com.sayt.godslove.utils.Constants;
import com.sayt.godslove.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static androidx.core.app.NotificationCompat.BADGE_ICON_SMALL;

public class BG_Service extends Service {
    private Handler handler;
    private Runnable runnable;
    private RequestQueue requestQueue;
    private String TAG = "BackgroundService";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";

    private boolean getingMonthsBoolean = true;
    private boolean getingClassesBoolean = true;
    private boolean getingAppBoolean = true;
    private boolean checkSecreteKey = true;

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
                getApp();
//                getMonths();
//                getClasses();
                getStudentCheck();
                handler.postDelayed(this, 1500);
            }
        };
        handler.postDelayed(runnable, 8000);
    }

    private void getStudentCheck() {
        if (!checkSecreteKey)
        return;
        List<String> ids = getIdsStudents();
        if (ids.size() > 0){
            for (String id: ids){
                checkSecreteKey(id);
            }
        }else{
            Log.e(TAG, "getStudentCheck: No Student");
        }
    }
    private void checkSecreteKey(String secret_key) {

        checkSecreteKey = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.STUDENT,
                response -> {
                    if (response != null) {
//                        Log.e(TAG, "checkSecreteKey: " + response);
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONObject student = object.getJSONObject("student");

                            if (student.getBoolean("error")) {
                                Log.e(TAG, "checkSecreteKey: Error is true ooooo =====>"+ response );
                            } else {

                                String tid = student.getString("id");
                                String studentname = student.getString("student");
                                String _class = student.getString("_class");
                                String secret_k = student.getString("secret_key");
                                String month = student.getString("month");
                                String conferenceId = student.getString("conferenceId");
                                String permit = student.getString("permit");
                                String active = student.getString("active");


                                DBHelper dbHelper = new DBHelper(this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                Cursor cursor = db.rawQuery("SELECT * FROM 'classes' WHERE tid='"
                                        + tid+ "'", null);
                                cursor.moveToFirst();
                                if (cursor.moveToFirst() || cursor.getCount() != 0){
                                    ContentValues values = new ContentValues();

                                    String secret_k_ = cursor.getString(cursor.getColumnIndex("secret_key"));
                                    String studentname_ = cursor.getString(cursor.getColumnIndex("student"));
                                    String _class_ = cursor.getString(cursor.getColumnIndex("_class"));
                                    String active_ = cursor.getString(cursor.getColumnIndex("active"));
                                    String month_ = cursor.getString(cursor.getColumnIndex("month"));
                                    String permit_ = cursor.getString(cursor.getColumnIndex("permit"));
                                    String conferenceId_ = cursor.getString(cursor.getColumnIndex("conferenceId"));

                                    if (!active.equalsIgnoreCase(active_))
                                        values.put("active", active);
                                    if (!month.equalsIgnoreCase(month_))
                                        values.put("active", month);
                                    if (!studentname.equalsIgnoreCase(studentname_))
                                        values.put("student", studentname);
                                    if (!conferenceId.equalsIgnoreCase(conferenceId_))
                                        values.put("conferenceId", conferenceId);
                                    if (!_class.equalsIgnoreCase(_class_))
                                        values.put("_class", _class);
                                    if (!permit.equalsIgnoreCase(permit_))
                                        values.put("permit", permit);
                                    if (!secret_k.equalsIgnoreCase(secret_k_)){
                                        values.put("secret_key", secret_k);

                                        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        Uri sound = RingtoneManager.getActualDefaultRingtoneUri(this, TYPE_NOTIFICATION);
                                        long[] vibration = new long[]{500, 1500, 2000, 150};
                                        createNotification(notificationIntent,
                                                secret_k+" is "+ studentname+"'s new secret code.",
                                                sound, vibration);
                                    }

                                    String selection = "tid" + " LIKE ?";
                                    String[] selectionArgs = {tid};
                                    if (values.size() > 0)
                                        db.update("classes", values, selection, selectionArgs);
                                }

                                cursor.close();
                                db.close();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        checkSecreteKey = true;
                    } else {
                        checkSecreteKey = true;
                    }

                }, error -> {
            checkSecreteKey = true;
            Log.e(TAG, "checkSecreteKey: " + error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("secret_key", secret_key);
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


//    private void getClasses() {
//        if (!getingClassesBoolean)
//            return;
//        getingClassesBoolean = false;
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_URL,
//                response -> {
//                    if (response != null) {
////                        Log.e(TAG, "getClasses: " + response);
//                        Utils.saveInSharepref_String(getApplicationContext(), "classes", response);
//                        getingAppBoolean = true;
//                    } else {
//                        getingClassesBoolean = true;
//                    }
//
//                }, error -> {
//            getingClassesBoolean = true;
//            Log.e(TAG, "getClasses: " + error);
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> post = new HashMap<>();
//                post.put("classes", "");
//                return post;
//            }
//        };
//
//        if (requestQueue == null)
//            requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(stringRequest);
//    }
//
//    private void getMonths() {
//        if (!getingMonthsBoolean)
//            return;
//        getingMonthsBoolean = false;
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_URL,
//                response -> {
//                    if (response != null) {
////                        Log.e(TAG, "getMonths: " + response);
//                        Utils.saveInSharepref_String(getApplicationContext(), "months", response);
//                        getingAppBoolean = true;
//                    } else {
//                        getingMonthsBoolean = true;
//                    }
//
//                }, error -> {
//            getingMonthsBoolean = true;
//            Log.e(TAG, "getMonths: " + error);
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> post = new HashMap<>();
//                post.put("months", "");
//                return post;
//            }
//        };
//
//        if (requestQueue == null)
//            requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(stringRequest);
//    }


    private void getApp() {
        if (!getingAppBoolean)
            return;
        getingAppBoolean = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_URL,
                response -> {
//                    Log.e(TAG, "getApp: " + response);
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

    private List<String> getIdsStudents() {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"tid"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("classes", projection, null, null, null, null, sortOrder);

        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String tid = cursor.getString(cursor.getColumnIndexOrThrow("tid"));
            list.add(tid);
        }
        cursor.close();
        db.close();
        return list;
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

        Bitmap large_icon_bmp = ((BitmapDrawable) getApplicationContext().getResources()
                .getDrawable(R.mipmap.ic_launcher_round)).getBitmap();
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

}
