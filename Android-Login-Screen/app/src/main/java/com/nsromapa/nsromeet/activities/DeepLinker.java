package com.nsromapa.nsromeet.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.databases.DBHelper;
import com.nsromapa.nsromeet.models.GroupListModel;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.nsromapa.nsromeet.utils.Utils.isConnectingToInternet;


public class DeepLinker extends AppCompatActivity {
    private TextView toolbarText;

    private TextView errorTV, cancelTV;
    private ImageView errorIV;
    private Button go_button;
    private ScrollView scrollView;
    private WebView webView;
    private ProgressBar progress_circular;

    private String dataLink;

    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_linker);

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> gotoMain());
        toolbarText = findViewById(R.id.toolbar_text);

        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getData() != null)
                dataLink = intent.getData().toString();
            else
                dataLink = "";
        } else return;


        errorIV = findViewById(R.id.errorIV);
        errorTV = findViewById(R.id.errorTV);
        cancelTV = findViewById(R.id.cancelTV);
        go_button = findViewById(R.id.go_button);
        scrollView = findViewById(R.id.scrollView);
        webView = findViewById(R.id.webView);
        progress_circular = findViewById(R.id.progress_circular);

        if (!isConnectingToInternet(this)) {
            Utils.shortToast(this, getString(R.string.no_internet_connection));
            finish();
        }
        if (!TextUtils.isEmpty(dataLink)) {
            if (dataLink.contains("meeting.nsromeet.com") || dataLink.contains("meet.jit.si")) {
                toolbarText.setText(getString(R.string.meeting_info));

                if (dataLink.contains("http")) {
                    String rep = dataLink
                            .replace("https://meet.jit.si/", "")
                            .replace("http://meet.meet.jit.si/", "")
                            .replace("https://meeting.nsromeet.com/", "")
                            .replace("http://meeting.nsromeet.com/", "");
                    checkLinkIfContainsStatic(rep);
                } else {
                    ShowErrorView(getString(R.string.unrecognised_url));
                }
            } else if (dataLink.contains("nsromeet.nsromapa.com")) {
                String rep = dataLink.replace("http://nsromeet.nsromapa.com/", "")
                        .replace("https://nsromeet.nsromapa.com/", "");
                if (rep.contains("group/")) {
                    toolbarText.setText(getString(R.string.group_info));

                    String groupLink = rep.replace("group/", "");
                    grabGroupDetailsWithGroupLink(groupLink);

                } else if (rep.contains("sched/")) {
                    String schedLink = rep.replace("sched/", "");
                    String[] separated = schedLink.split("/");
                    String tid = separated[0].trim();
                    if (TextUtils.isDigitsOnly(tid)) {
                        toolbarText.setText(getString(R.string.schedule_info));
                        getScheduleWithTid(tid);
                    } else {
                        toolbarText.setText(getString(R.string.error));
                        ShowErrorView(getString(R.string.unrecognised_url));
                    }
                } else {
                    checkLinkIfContainsStatic(rep);
                }
            } else {
                toolbarText.setText(getString(R.string.error));
                ShowErrorView(getString(R.string.unrecognised_url));
            }
        } else {
            toolbarText.setText(getString(R.string.error));
            ShowErrorView(getString(R.string.unrecognised_url));
        }
    }

    private void grabGroupDetailsWithGroupLink(String groupLink) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM 'groups' WHERE link='"
                + groupLink + "'", null);
        cursor.moveToFirst();
        if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
            getGroupInfoWithLink(groupLink);
            cursor.close();
            db.close();
        } else {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String forid = cursor.getString(cursor.getColumnIndexOrThrow("forid"));
            String db_id = cursor.getString(cursor.getColumnIndexOrThrow("db_id"));
            String gid = cursor.getString(cursor.getColumnIndexOrThrow("gid"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String conference_id = cursor.getString(cursor.getColumnIndexOrThrow("conference_id"));
            String privacy = cursor.getString(cursor.getColumnIndexOrThrow("privacy"));
            String link = cursor.getString(cursor.getColumnIndexOrThrow("link"));
            String created_by = cursor.getString(cursor.getColumnIndexOrThrow("created_by"));
            String creator_name = cursor.getString(cursor.getColumnIndexOrThrow("creator_name"));
            String created_idtype = cursor.getString(cursor.getColumnIndexOrThrow("created_idtype"));
            String date_added = cursor.getString(cursor.getColumnIndexOrThrow("date_added"));
            String active = cursor.getString(cursor.getColumnIndexOrThrow("active"));
            cursor.close();
            db.close();

            if (forid.equals(Utils.getShareprefValue_String(this, "uuid")) && active.equals("yes")) {
                GroupListModel item = new GroupListModel(String.valueOf(id), db_id, gid, name, description,
                        conference_id, privacy, link, created_by, creator_name, created_idtype,
                        date_added, active);

                Intent intent = new Intent(this, GroupInfoActivity.class);
                intent.putExtra("group_item", item);
                startActivity(intent);
                finish();
            } else {
                getGroupInfoWithLink(groupLink);
            }
        }
    }

    private void getGroupInfoWithLink(String groupLink) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null) {
                requestQueue.cancelAll(this);
            }
        });
        progressDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_SINGLE_GROUP,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject group = object.getJSONObject("group");

                                existingGroup(group);
                                progressDialog.dismiss();
                            } else {
                                ShowErrorView(object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }

                    } else {
                        ShowErrorView(getString(R.string.an_error));
                        progressDialog.dismiss();
                    }
                },
                error -> {
                    ShowErrorView(error.getMessage());
                    progressDialog.dismiss();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(DeepLinker.this, "uuid"));
                post.put("value", groupLink);
                post.put("where", "link");
                return post;
            }
        };
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }


    private void checkLinkIfContainsStatic(String rep) {
        if (rep.contains("static/dialInInfo.html?room=")) {
            scrollView.setVisibility(View.GONE);
            progress_circular.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progress_circular.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progress_circular.setVisibility(View.GONE);
                }
            });
            CookieManager manager = CookieManager.getInstance();
            manager.acceptCookie();
            webView.loadUrl(dataLink);
        } else {
            openMeeting(rep, String.format(getString(R.string.do_you_want_to_enter_meeting), "enter"));
        }
    }


    private void openMeeting(String meetingId, String message) {
        webView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        errorTV.setText(message);
        errorIV.setVisibility(View.GONE);
        progress_circular.setVisibility(View.GONE);
        cancelTV.setVisibility(View.VISIBLE);
        go_button.setText(getString(R.string.join_meet));
        cancelTV.setOnClickListener(v -> gotoMain());
        go_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, MeetingWithIDActivity.class);
            intent.putExtra("deep_link", meetingId);
            intent.putExtra("title", getString(R.string.join_meet));
            startActivity(intent);
            finish();
        });
    }


    private void ShowErrorView(String message) {
        webView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        errorTV.setText(message);
        errorIV.setVisibility(View.VISIBLE);
        cancelTV.setVisibility(View.GONE);
        progress_circular.setVisibility(View.GONE);
        go_button.setText(getString(R.string.go_back));
        go_button.setOnClickListener(v -> {
            gotoMain();
        });
    }


    private void getScheduleWithTid(String tid) {
        webView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        progress_circular.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_SINGLE_SCHEDULE,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject schedule = object.getJSONObject("schedule");
                                String status = schedule.getString("status");
                                String conference_id = schedule.getString("conference_id");

                                if (status.equals(getString(R.string.done))) {
                                    ShowErrorView(getString(R.string.meeting_done));
                                } else if (status.equals(getString(R.string.ongoing))) {
                                    openMeeting(conference_id,
                                            "Meeting is ongoing.\n" + String.format(getString(R.string.do_you_want_to_enter_meeting), "join"));
                                } else if (status.equals(getString(R.string.waiting))) {
                                    waitingSchedule(getString(R.string.meeting_not_start_add_schedule), schedule);
                                } else {
                                    gotoMain();
                                }
                                progress_circular.setVisibility(View.GONE);
                            } else {
                                ShowErrorView(object.getString("error_msg"));
                                progress_circular.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progress_circular.setVisibility(View.GONE);
                        }

                    } else {
                        ShowErrorView(getString(R.string.an_error));
                        progress_circular.setVisibility(View.GONE);
                    }
                },
                error -> {
                    ShowErrorView(error.getMessage());
                    progress_circular.setVisibility(View.GONE);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(DeepLinker.this, "uuid"));
                post.put("value", tid);
                post.put("where", "id");

                return post;
            }
        };
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void waitingSchedule(String message, JSONObject schedule) {
        webView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        errorTV.setText(message);
        errorIV.setVisibility(View.GONE);
        progress_circular.setVisibility(View.GONE);
        cancelTV.setVisibility(View.VISIBLE);
        go_button.setText(getString(R.string.schedule));
        cancelTV.setOnClickListener(v -> gotoMain());
        go_button.setOnClickListener(v -> insertSchedule(schedule));
    }

    private void existingGroup(JSONObject group) {
        webView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        errorIV.setVisibility(View.GONE);
        progress_circular.setVisibility(View.GONE);
        try {
            String name = group.getString("name");
            String description = group.getString("description");
            String privacy = group.getString("privacy");
            String date_added = group.getString("date_added");
            String link = group.getString("link");
            String tid = group.getString("tid");

            errorTV.setText(String.format(getString(R.string.group_shot_info), name, description, privacy, date_added));
            go_button.setOnClickListener(v -> joinGroup(tid, link));
        } catch (JSONException e) {
            e.printStackTrace();
            go_button.setVisibility(View.GONE);
        }
        cancelTV.setVisibility(View.VISIBLE);
        go_button.setText(getString(R.string.join_group));
        cancelTV.setOnClickListener(v -> gotoMain());
    }

    private void joinGroup(String tid, String link) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null) {
                requestQueue.cancelAll(this);
            }
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.ADD_GROUP_MEMBER,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                Utils.longToast(this, object.getString("error_msg"));
                                progressDialog.dismiss();
                                finish();
                            } else {
                                ShowErrorView(object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }

                    } else {
                        ShowErrorView(getString(R.string.an_error));
                        progressDialog.dismiss();
                    }
                },
                error -> {
                    ShowErrorView(error.getMessage());
                    progressDialog.dismiss();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(DeepLinker.this, "uuid"));
                post.put("tid", tid);
                post.put("link", link);
                return post;
            }
        };
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }


    private void insertSchedule(JSONObject schedule) {
        try {
            String tid = schedule.getString("tid");
            String title = schedule.getString("title");
            String conference_id = schedule.getString("conference_id");
            String date_string = schedule.getString("date_string");
            String duration_hours = schedule.getString("duration_hours");
            String duration_minutes = schedule.getString("duration_minutes");
            String time_stamp = schedule.getString("time_stamp");
            String host_type = schedule.getString("host_type");
            String status = schedule.getString("status");
            String created_by = schedule.getString("created_by");
            String creator_name = schedule.getString("creator_name");
            String created_idtype = schedule.getString("created_idtype");
            String active = schedule.getString("active");

            String sched_type_id = "ADDED";
            String sched_type_name = "You added";
            String sched_type = "ADDED";


            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("forid", Utils.getShareprefValue_String(this, "uuid"));
            values.put("db_id", tid);
            values.put("title", title);
            values.put("conference_id", conference_id);
            values.put("date_string", date_string);
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

            Utils.shortToast(this, getString(R.string.meeting_scheduled));
            gotoMain();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        gotoMain();
    }
}