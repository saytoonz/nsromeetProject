package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.google.android.material.snackbar.Snackbar;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.adapters.NewScheduleRecyclerAdapter;
import com.nsromapa.nsromeet.adapters.ScheduleAdapter;
import com.nsromapa.nsromeet.databases.DBHelper;
import com.nsromapa.nsromeet.models.NewScheduleRecyclerModel;
import com.nsromapa.nsromeet.models.ScheduleListModel;
import com.nsromapa.nsromeet.models.SchoolsetupModel;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recycler, class_list;
    private ScrollView newScheduleSV;
    private RelativeLayout listSchedulesRL;
    private EditText schedule_title;
    private SingleDateAndTimePicker date_time;
    private RadioGroup video_or_audio;
    private TextView errorTV;
    private static TextView noticeTV;
    private ProgressBar list_progress;
    private Button add_schedule, cancel_action;
    private Spinner duration_hrs, duration_min, schedule_spanner;
    private static List<NewScheduleRecyclerModel> selectedList = new ArrayList<>();

    private RequestQueue requestQueue;
    private DBHelper dbHelper;
    private static List<ScheduleListModel> scheduleListModels;
    private static List<NewScheduleRecyclerModel> newScheduleRecyclerModels = new ArrayList<>();
    private String[] duration_hrsList;
    private String[] duration_minList;
    private String[] schedule_withList;
    private static ScheduleAdapter adapter;
    private static NewScheduleRecyclerAdapter newScheduleRecyclerAdapter;
    private static RequestQueue requestQueue2;
    private static Context context;
    private static Activity activity;
    private static ProgressDialog progressDialog;
    private static String TAG = "ScheduleActivity";


    public static void addToSelectedList(NewScheduleRecyclerModel item) {
        selectedList.add(item);
    }

    public static void removeFromSelectedList(NewScheduleRecyclerModel item) {
        selectedList.remove(item);
    }

    public static void showNoSchedule() {
        noticeTV.setVisibility(View.VISIBLE);
    }

    public static void hideNoSchedule() {
        noticeTV.setVisibility(View.GONE);
    }

    public static void startMeetingNow(ScheduleListModel item) {
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(context);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.UPDATE_SINGLE_SCHEDULE_VALUE,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                DBHelper dbHelper = new DBHelper(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("status", context.getString(R.string.ongoing));

                                String selection = "id" + " LIKE ?";
                                String[] selectionArgs = {item.getId()};
                                int count = db.update("schedules", values, selection, selectionArgs);
                                scheduleListModels.get(item.getIndex()).setStatus(context.getString(R.string.ongoing));
                                adapter.notifyDataSetChanged();
                                if (progressDialog != null) progressDialog.dismiss();
                                openMeetingNow(item);
                            } else {
                                Utils.shortToast(context, object.getString("error_msg"));
                                if (progressDialog != null) progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (progressDialog != null) progressDialog.dismiss();

                        }
                    } else {
                        Utils.shortToast(context, context.getString(R.string.an_error));
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                }, error -> {
            Utils.shortToast(context, error.getMessage());
            if (progressDialog != null) progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("uuid", Utils.getShareprefValue_String(context, "uuid"));
                postMap.put("where", "status");
                postMap.put("tid", item.getTid());
                postMap.put("success_msg", "Starting meeting now...");
                postMap.put("value", context.getString(R.string.ongoing));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(context);
        requestQueue2.add(stringRequest);

    }

    public static void openMeetingNow(ScheduleListModel item) {
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(item.getConference_id())
                .setSubject(item.getTitle())
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeetActivity.launch(context, options);
    }

    public static void endMeeting(ScheduleListModel item) {
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(context);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.UPDATE_SINGLE_SCHEDULE_VALUE,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                DBHelper dbHelper = new DBHelper(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("status", context.getString(R.string.done));

                                String selection = "id" + " LIKE ?";
                                String[] selectionArgs = {item.getId()};
                                int count = db.update("schedules", values, selection, selectionArgs);
//                                try {
//                                    scheduleListModels.get(item.getIndex()).setStatus(context.getString(R.string.ongoing));
//                                    adapter.notifyDataSetChanged();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
                                if (progressDialog != null) progressDialog.dismiss();
                            } else {
                                Utils.shortToast(context, object.getString("error_msg"));
                                if (progressDialog != null) progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (progressDialog != null) progressDialog.dismiss();

                        }
                    } else {
                        Utils.shortToast(context, context.getString(R.string.an_error));
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                }, error -> {
            Utils.shortToast(context, error.getMessage());
            if (progressDialog != null) progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("uuid", Utils.getShareprefValue_String(context, "uuid"));
                postMap.put("where", "status");
                postMap.put("tid", item.getTid());
                postMap.put("success_msg", "Meeting Ended successfully...");
                postMap.put("value", context.getString(R.string.done));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(context);
        requestQueue2.add(stringRequest);

    }

    public static void deleteCreatedSchedule(ScheduleListModel item) {
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(context);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.UPDATE_SINGLE_SCHEDULE_VALUE,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {

                                DBHelper dbHelper = new DBHelper(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("deleted", "yes");
                                String selection = "id" + " LIKE ?";
                                String[] selectionArgs = {item.getId()};
                                db.update("schedules", values, selection, selectionArgs);
                                db.close();

                                if (progressDialog != null) progressDialog.dismiss();
                            } else {
                                Utils.shortToast(context, object.getString("error_msg"));
                                if (progressDialog != null) progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (progressDialog != null) progressDialog.dismiss();

                        }
                    } else {
                        Utils.shortToast(context, context.getString(R.string.an_error));
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                }, error -> {
            Utils.shortToast(context, error.getMessage());
            if (progressDialog != null) progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("uuid", Utils.getShareprefValue_String(context, "uuid"));
                postMap.put("where", "active");
                postMap.put("tid", item.getTid());
                postMap.put("success_msg", "Schedule deleted successfully...");
                postMap.put("value", "no");
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(context);
        requestQueue2.add(stringRequest);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        context = this;
        activity = this;

        if (Utils.getShareprefValue_String(this, "idtype").equals(getString(R.string.idtype_lecturer))) {
            schedule_withList = new String[]{getString(R.string.faculty), getString(R.string.department), getString(R.string.classs), getString(R.string.groups)};
        } else {
            schedule_withList = new String[]{getString(R.string.groups)};
        }

        duration_hrsList = new String[]{"0 hour", "1 hour", "2 hours", "3 hours", "4 hours",
                "5 hours", "6 hours", "7 hours", "8 hours", "9 hours", "10 hours"};

        duration_minList = new String[]{"0 minute", "15 minute", "30 minute", "45 minute"};

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getIntent().getStringExtra("title"));

        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            serverURL = new URL(Constants.SERVER_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }

        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        try {
            userInfo.setDisplayName(Utils.getShareprefValue_String(this, "uname"));
            userInfo.setAvatar(new URL(Constants.BASEURL + Utils.getShareprefValue_String(this, "image")));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setUserInfo(userInfo)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);


        listSchedulesRL = findViewById(R.id.listSchedulesRL);
        recycler = findViewById(R.id.recycler);
        noticeTV = findViewById(R.id.noticeTV);
        newScheduleSV = findViewById(R.id.newScheduleSV);
        schedule_title = findViewById(R.id.schedule_title);
        date_time = findViewById(R.id.date_time);
        class_list = findViewById(R.id.class_list);
        video_or_audio = findViewById(R.id.video_or_audio);
        errorTV = findViewById(R.id.errorTV);
        list_progress = findViewById(R.id.list_progress);
        add_schedule = findViewById(R.id.add_schedule);
        cancel_action = findViewById(R.id.cancel_action);
        duration_hrs = findViewById(R.id.duration_hrs);
        duration_min = findViewById(R.id.duration_mins);
        schedule_spanner = findViewById(R.id.schedule_spanner);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);

        if (Objects.equals(getIntent().getStringExtra("title"), getString(R.string.schedules))) {
            setUpExistingSchedule();
        } else {
            setUpNewSchedule();
        }

    }

    private void setUpExistingSchedule() {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("count", 0).apply();
        listSchedulesRL.setVisibility(View.VISIBLE);
        newScheduleSV.setVisibility(View.GONE);

        dbHelper = new DBHelper(this);
        scheduleListModels = getSchedules();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new ScheduleAdapter(this, scheduleListModels);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setUpNewSchedule() {
        listSchedulesRL.setVisibility(View.GONE);
        newScheduleSV.setVisibility(View.VISIBLE);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        newScheduleRecyclerAdapter = new NewScheduleRecyclerAdapter(this, newScheduleRecyclerModels);
        class_list.setHasFixedSize(true);
        class_list.setLayoutManager(manager);
        class_list.setAdapter(newScheduleRecyclerAdapter);
        newScheduleRecyclerAdapter.notifyDataSetChanged();


        duration_hrs.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, duration_hrsList));
        duration_min.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, duration_minList));
        schedule_spanner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, schedule_withList));

        cancel_action.setOnClickListener(v -> finish());
        add_schedule.setOnClickListener(v -> {
            String title = schedule_title.getText().toString();
            String timeStamp = String.valueOf(date_time.getDate().getTime());//.substring(0,10);
            String meetingType = "audio";
            String durationHr = duration_hrs.getSelectedItem().toString().trim();
            String durationMin = duration_min.getSelectedItem().toString().trim();
            String date_string = date_time.getDate().toString();

            if (video_or_audio.getCheckedRadioButtonId() == R.id.video_host) {
                meetingType = "video";
            }

            if (TextUtils.isEmpty(title)) {
                Utils.shortToast(this, "Title your schedule!");
                return;
            }

            if (durationHr.equals(duration_hrsList[0]) && durationMin.equals(duration_minList[0])) {
                Utils.shortToast(this, "Please set duration time");
                return;
            }
            createNewSchedule(title, timeStamp, date_string, meetingType, durationHr, durationMin);
        });


        schedule_spanner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = schedule_spanner.getSelectedItem().toString().trim();
                if (item.equalsIgnoreCase(getString(R.string.faculty))) {
                    if (!TextUtils.isEmpty(Utils.getShareprefValue_String(context, "school")))
                        getFaculties();
                    else
                        Utils.shortToast(context, getString(R.string.setup_school_first));

                } else if (item.equalsIgnoreCase(getString(R.string.department))) {
                    if (!TextUtils.isEmpty(Utils.getShareprefValue_String(context, "faculty")))
                        getDepartments();
                    else
                        Utils.shortToast(context, getString(R.string.setup_faculty_first));

                } else if (item.equalsIgnoreCase(getString(R.string.classs))) {
                    if (!TextUtils.isEmpty(Utils.getShareprefValue_String(context, "department")))
                        getClasses();
                    else
                        Utils.shortToast(context, getString(R.string.setup_department_first));
                } else if (item.equalsIgnoreCase(getString(R.string.groups))) {
                    getGroupsFromLocalDB();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getGroupsFromLocalDB() {
        newScheduleRecyclerModels.clear();
        selectedList.clear();
        errorTV.setVisibility(View.GONE);
        list_progress.setVisibility(View.VISIBLE);

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"id", "forid", "db_id", "gid", "name", "description",
                "conference_id", "privacy", "link", "created_by", "creator_name",
                "created_idtype", "date_added", "active"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("groups", projection, null, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            String forid = cursor.getString(cursor.getColumnIndexOrThrow("forid"));
            String db_id = cursor.getString(cursor.getColumnIndexOrThrow("db_id"));
            String gid = cursor.getString(cursor.getColumnIndexOrThrow("gid"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            if (forid.equals(Utils.getShareprefValue_String(this, "uuid"))) {
                newScheduleRecyclerModels.add(new NewScheduleRecyclerModel(
                        db_id,
                        getString(R.string.group),
                        name));
                newScheduleRecyclerAdapter.notifyDataSetChanged();
            }
        }
        cursor.close();
        db.close();

        list_progress.setVisibility(View.GONE);
        if (newScheduleRecyclerModels.size() < 1) {
            errorTV.setVisibility(View.VISIBLE);
            errorTV.setText(getString(R.string.no_result_found_));
        }
    }

    private void getClasses() {
        newScheduleRecyclerModels.clear();
        selectedList.clear();
        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");
        String department_id = Utils.getShareprefValue_String(this, "department");
        errorTV.setVisibility(View.GONE);
        list_progress.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONArray jsonArray = object.getJSONArray("classes");
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject class_ = jsonArray.getJSONObject(i);
                                    newScheduleRecyclerModels.add(new NewScheduleRecyclerModel(
                                            class_.getString("id"),
                                            getString(R.string.classs),
                                            class_.getString("name")));
                                    newScheduleRecyclerAdapter.notifyDataSetChanged();
                                }

                                list_progress.setVisibility(View.GONE);

                                if (newScheduleRecyclerModels.size() < 1) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getString(R.string.no_result_found_));
                                }
                            } else {
                                errorTV.setVisibility(View.VISIBLE);
                                errorTV.setText(object.getString("error_msg"));
                                list_progress.setVisibility(View.GONE);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                            errorTV.setVisibility(View.VISIBLE);
                            errorTV.setText(getString(R.string.an_error));
                            list_progress.setVisibility(View.GONE);
                        }


                    } else {

                        errorTV.setVisibility(View.VISIBLE);
                        errorTV.setText(getString(R.string.an_error));
                        list_progress.setVisibility(View.GONE);
                    }
                }, error -> {
            Utils.longToast(this, error.getMessage());

            errorTV.setVisibility(View.VISIBLE);
            errorTV.setText(getString(R.string.an_error));
            list_progress.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("all_classes", "true");
                postMap.put("cschool", school_id);
                postMap.put("cfaculty", faculty_id);
                postMap.put("cdepartment", department_id);
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getDepartments() {
        newScheduleRecyclerModels.clear();
        selectedList.clear();

        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");

        errorTV.setVisibility(View.GONE);
        list_progress.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("departments");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject department = jsonArray.getJSONObject(i);

                                newScheduleRecyclerModels.add(new NewScheduleRecyclerModel(
                                        department.getString("id"),
                                        getString(R.string.department),
                                        department.getString("name")));
                                newScheduleRecyclerAdapter.notifyDataSetChanged();
                            }

                            list_progress.setVisibility(View.GONE);

                            if (newScheduleRecyclerModels.size() < 1) {
                                errorTV.setVisibility(View.VISIBLE);
                                errorTV.setText(getString(R.string.no_result_found_));
                            }
                        } else {
                            errorTV.setVisibility(View.VISIBLE);
                            errorTV.setText(object.getString("error_msg"));
                            list_progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        errorTV.setVisibility(View.VISIBLE);
                        errorTV.setText(getString(R.string.an_error));
                        list_progress.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            errorTV.setVisibility(View.VISIBLE);
            errorTV.setText(getString(R.string.an_error));
            list_progress.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("all_departments", "true");
                postMap.put("dschool", school_id);
                postMap.put("dfaculty", faculty_id);
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getFaculties() {
        newScheduleRecyclerModels.clear();
        selectedList.clear();

        String school_id = Utils.getShareprefValue_String(this, "school");
        errorTV.setVisibility(View.GONE);
        list_progress.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("faculties");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject faculty = jsonArray.getJSONObject(i);
                                newScheduleRecyclerModels.add(new NewScheduleRecyclerModel(
                                        faculty.getString("id"),
                                        getString(R.string.faculty),
                                        faculty.getString("name")));
                                newScheduleRecyclerAdapter.notifyDataSetChanged();
                            }
                            list_progress.setVisibility(View.GONE);

                            if (newScheduleRecyclerModels.size() < 1) {
                                errorTV.setVisibility(View.VISIBLE);
                                errorTV.setText(getString(R.string.no_result_found_));
                            }
                        } else {
                            errorTV.setVisibility(View.VISIBLE);
                            errorTV.setText(object.getString("error_msg"));
                            list_progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        errorTV.setVisibility(View.VISIBLE);
                        errorTV.setText(getString(R.string.an_error));
                        list_progress.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());
            errorTV.setVisibility(View.VISIBLE);
            errorTV.setText(getString(R.string.an_error));
            list_progress.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("all_faculties", "true");
                postMap.put("fschool", school_id);
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private List<ScheduleListModel> getSchedules() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"id", "forid", "db_id", "title", "conference_id", "date_string", "sched_type", "sched_type_id", "sched_type_name", "deleted",
                "duration_hours", "duration_minutes", "time_stamp", "host_type", "status", "created_by", "creator_name", "created_idtype", "active"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("schedules", projection, null, null, null, null, sortOrder);

        List<ScheduleListModel> list = new ArrayList<>();
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
            String sched_type_name = cursor.getString(cursor.getColumnIndexOrThrow("sched_type_name"));
            String deleted = cursor.getString(cursor.getColumnIndexOrThrow("deleted"));
            String created_idtype = cursor.getString(cursor.getColumnIndexOrThrow("created_idtype"));
            String active = cursor.getString(cursor.getColumnIndexOrThrow("active"));

            if (forid.equals(Utils.getShareprefValue_String(this, "uuid")) && deleted.equals("no")) {
                list.add(new ScheduleListModel(String.valueOf(id), forid, tid, title, conference_id,
                        date_string, duration_hours, duration_minutes,
                        time_stamp, host_type, status, created_by, creator_name, created_idtype, sched_type,
                        sched_type_id, sched_type_name, deleted, active));
            }
        }
        cursor.close();

        db.close();
        return list;

    }

    private void createNewSchedule(String title, String timeStamp, String date_string,
                                   String meetingType, String durationHr, String durationMin) {
        String scheduled_with_ = "";

        if (selectedList.size() < 1) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.select_those_involve), Snackbar.LENGTH_LONG).show();
            return;
        } else {
            for (NewScheduleRecyclerModel item : selectedList) {
                String pass = String.format("%s:%s", item.getType().toUpperCase().substring(0, 3), item.getTid());
                if (!scheduled_with_.contains(pass)) {
                    if (TextUtils.isEmpty(scheduled_with_)) {
                        scheduled_with_ = pass;
                    } else {
                        scheduled_with_ = String.format("%s,%s", scheduled_with_, pass);
                    }
                }
            }
        }
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
                requestQueue.cancelAll(this);
        });
        progressDialog.show();

        String finalScheduled_with_ = scheduled_with_;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.NEW_SCHEDULE,
                response -> {
                    if (response != null) {

                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
//                                JSONObject schedule = object.getJSONObject("schedule");
//                                String tid = schedule.getString("tid");
//                                String title_ = schedule.getString("title");
//                                String conference_id = schedule.getString("conference_id");
//                                String dateString_ = schedule.getString("date_string");
//                                String duration_hours = schedule.getString("duration_hours");
//                                String duration_minutes = schedule.getString("duration_minutes");
//                                String time_stamp = schedule.getString("time_stamp");
//                                String host_type = schedule.getString("host_type");
//                                String status = schedule.getString("status");
//                                String scheduled_with = schedule.getString("scheduled_with");
//                                String created_by = schedule.getString("created_by");
//                                String creator_name = schedule.getString("creator_name");
//                                String sched_type_id = schedule.getString("sched_type_id");
//                                String sched_type = schedule.getString("sched_type");
//                                String created_idtype = schedule.getString("created_idtype");
//                                String active = schedule.getString("active");
//
//                                DBHelper dbHelper = new DBHelper(this);
//                                SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//                                ContentValues values = new ContentValues();
//                                values.put("forid", Utils.getShareprefValue_String(this, "uuid"));
//                                values.put("db_id", tid);
//                                values.put("title", title_);
//                                values.put("conference_id", conference_id);
//                                values.put("date_string", dateString_);
//                                values.put("duration_hours", duration_hours);
//                                values.put("duration_minutes", duration_minutes);
//                                values.put("time_stamp", time_stamp);
//                                values.put("host_type", host_type);
//                                values.put("status", status);
//                                values.put("created_by", created_by);
//                                values.put("creator_name", creator_name);
//                                values.put("created_idtype", created_idtype);
//                                values.put("sched_type_id", sched_type_id);
//                                values.put("sched_type", sched_type);
//                                values.put("deleted", "no");
//                                values.put("active", active);
//                                long newRowId = db.insert("schedules", null, values);

                                if (progressDialog != null) progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Creating Schedule, you would be notified when done...", Snackbar.LENGTH_LONG).show();
                                try {
                                    Thread.sleep(4000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                finish();
                            } else {
                                Utils.shortToast(this, object.getString("error_msg"));
                                if (progressDialog != null) progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (progressDialog != null) progressDialog.dismiss();

                        }
                    } else {
                        Utils.shortToast(this, this.getString(R.string.an_error));
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                },
                error -> {
                    Utils.shortToast(this, error.getMessage());
                    if (progressDialog != null) progressDialog.dismiss();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(ScheduleActivity.this, "uuid"));
                post.put("title", title);
                post.put("conference_id", "");
                post.put("date_string", date_string);
                post.put("duration_hours", durationHr);
                post.put("duration_minutes", durationMin);
                post.put("time_stamp", timeStamp);
                post.put("host_type", meetingType);
                post.put("scheduled_with", finalScheduled_with_);
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        if (requestQueue2 != null)
            requestQueue2.cancelAll(this);
        super.onDestroy();
    }
}
