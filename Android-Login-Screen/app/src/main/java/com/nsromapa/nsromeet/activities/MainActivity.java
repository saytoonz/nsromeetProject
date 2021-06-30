package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.nsromapa.nsromeet.BuildConfig;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.services.BackgroundService;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.nsromapa.nsromeet.utils.Constants.setServerUrl;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mAuth = FirebaseAuth.getInstance();
        String uuid = Utils.getShareprefValue_String(this, "uuid");
        if (TextUtils.isEmpty(uuid)) {
            if (mAuth.getCurrentUser() == null) {
                Utils.longToast(this, getString(R.string.login_required));
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                getUserWithFirebaseUid(mAuth.getCurrentUser().getUid());
            }
        }


        TextView logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> logOut());

        if (TextUtils.isEmpty(Utils.getShareprefValue_String(this, "image")))
            Utils.saveInSharepref_String(this, "image", "user.png");


        LinearLayout start_meet = findViewById(R.id.start_meet);
        LinearLayout join_meet = findViewById(R.id.join_meet);
        LinearLayout schedule = findViewById(R.id.schedule);
        LinearLayout groups = findViewById(R.id.history);
        LinearLayout profile = findViewById(R.id.profile);
        LinearLayout terms_policies = findViewById(R.id.terms_policies);

        start_meet.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MeetingWithIDActivity.class);
            intent.putExtra("title", getString(R.string.start_meet));
            startActivity(intent);
        });

        join_meet.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.alert_join_meeting);
            Button school = dialog.findViewById(R.id.school_meetings);
            Button general = dialog.findViewById(R.id.general_meetings);
            TextView noticeTV = dialog.findViewById(R.id.noticeTV);

            school.setOnClickListener(a -> {
                if (!TextUtils.isEmpty(Utils.getShareprefValue_String(MainActivity.this, "school"))) {
                    Intent intent = new Intent(MainActivity.this, SchoolCallTrendActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                } else {
                    noticeTV.setText(getString(R.string.setup_notice));
                    school.setText(getText(R.string.setup_school));
                    general.setText(getText(R.string.cancel));
                    general.setOnClickListener(v1 -> dialog.dismiss());
                    school.setOnClickListener(v1 -> {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("setup", true);
                        startActivity(intent);
                        dialog.dismiss();
                    });
                }
            });

            general.setOnClickListener(a -> {
                Intent intent = new Intent(MainActivity.this, MeetingWithIDActivity.class);
                intent.putExtra("title", getString(R.string.join_meet));
                startActivity(intent);
                dialog.dismiss();
            });

            dialog.show();
        });
        schedule.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.alert_join_meeting);
            Button new_Schedule = dialog.findViewById(R.id.school_meetings);
            Button existing = dialog.findViewById(R.id.general_meetings);
            TextView noticeTV = dialog.findViewById(R.id.noticeTV);

            noticeTV.setText(getString(R.string.select_an_option));
            new_Schedule.setText(getString(R.string.add_new));
            existing.setText(getString(R.string.schedules));


            new_Schedule.setOnClickListener(a -> {
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                intent.putExtra("title", getString(R.string.new_schedule));
                startActivity(intent);
                dialog.dismiss();
            });

            existing.setOnClickListener(a -> {
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                intent.putExtra("title", getString(R.string.schedules));
                startActivity(intent);
                dialog.dismiss();
            });

            dialog.show();
        });


        groups.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.alert_join_meeting);
            Button new_group = dialog.findViewById(R.id.school_meetings);
            Button existing = dialog.findViewById(R.id.general_meetings);
            TextView noticeTV = dialog.findViewById(R.id.noticeTV);

            noticeTV.setText(getString(R.string.select_an_option));
            new_group.setText(getString(R.string.add_new));
            existing.setText(getString(R.string.groups));


            new_group.setOnClickListener(a -> {
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                intent.putExtra("title", getString(R.string.new_group));
                startActivity(intent);
                dialog.dismiss();
            });

            existing.setOnClickListener(a -> {
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                intent.putExtra("title", getString(R.string.groups));
                startActivity(intent);
                dialog.dismiss();
            });

            dialog.show();
        });
        profile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("setup", false);
            startActivity(intent);
        });
        terms_policies.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TermsAndPolicies.class);
            startActivity(intent);
        });
    }

    private void checkOnApp() {
        String app = Utils.getShareprefValue_String(this, "app");
        if (TextUtils.isEmpty(app))
            return;

        try {
            JSONObject object = new JSONObject(app);
            JSONObject appObj = object.getJSONObject("app");

            String version = appObj.getString("version");
            String notes = appObj.getString("notes");
            String what_is_new = appObj.getString("what_is_new");
            String download_url = appObj.getString("download_url");
            String is_force_update = appObj.getString("is_force_update");
            String serverUrl = appObj.getString("server");
            String active = appObj.getString("active");

            if (!Constants.SERVER_URL.equals(serverUrl))
                setServerUrl(serverUrl);

            if (active.equals("yes")) {
                int versionCode = BuildConfig.VERSION_CODE;
                String versionName = BuildConfig.VERSION_NAME;
                if (!versionName.equals(version)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Software Update")
                            .setMessage(what_is_new)
                            .setPositiveButton(R.string.update, (dialog, which) -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(download_url));
                                startActivity(browserIntent);
                            })

                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                                if (is_force_update.equals("true")) {
                                    finish();
                                }
                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void logOut() {
        mAuth.signOut();
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void getUserWithFirebaseUid(String fb_uid) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
                requestQueue.cancelAll(this);
        });


        if (!TextUtils.isEmpty(fb_uid)) {

            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.FB_USER,
                    response -> {
                        if (response != null) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if (!object.getBoolean("error")) {
                                    String uid = object.getString("uid");
                                    String fuid = object.getString("firebase_uid");
                                    JSONObject user = object.getJSONObject("user");
                                    String tid = user.getString("tid");
                                    String uname = user.getString("username");
                                    String name = user.getString("name");
                                    String phone = user.getString("phone");
                                    String image = user.getString("image");
                                    String active = user.getString("active");
                                    String id_type = user.getString("id_type");
                                    String level = user.getString("level");


                                    String school = user.getString("school");
                                    String faculty = user.getString("faculty");
                                    String department = user.getString("department");
                                    String class_ = user.getString("class");


                                    if (active.equalsIgnoreCase("yes")) {
                                        Utils.saveInSharepref_String(this, "uuid", uid);
                                        Utils.saveInSharepref_String(this, "fuid", fuid);
                                        Utils.saveInSharepref_String(this, "uname", uname);
                                        Utils.saveInSharepref_String(this, "idtype", id_type);
                                        Utils.saveInSharepref_String(this, "level", level);
                                        Utils.saveInSharepref_String(this, "name", name);
                                        Utils.saveInSharepref_String(this, "phone", phone);
                                        Utils.saveInSharepref_String(this, "image", image);
                                        Utils.saveInSharepref_String(this, "active", active);

                                        if (!TextUtils.isEmpty(school))
                                            Utils.SharedPrefSchool(this, object.getJSONObject("school"));

                                        if (!TextUtils.isEmpty(faculty))
                                            Utils.SharedPrefFaculty(this, object.getJSONObject("faculty"));

                                        if (!TextUtils.isEmpty(department))
                                            Utils.SharedPrefDepartment(this, object.getJSONObject("department"));

                                        if (!TextUtils.isEmpty(class_))
                                            Utils.SharedPrefClass(this, object.getJSONObject("class"));


                                        progressDialog.dismiss();

//                                        Intent intent = new Intent(this, MainActivity.class);
//                                        startActivity(intent);
//                                        finish();

                                    } else if (active.equalsIgnoreCase("n_v")) {
                                        Utils.saveInSharepref_String(this, "tid", tid);
                                        progressDialog.dismiss();

                                        Intent intent = new Intent(this, VerifyMobile.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Utils.longToast(this, getString(R.string.account_not_exist));
                                        progressDialog.dismiss();
                                    }

                                } else {
                                    Utils.shortToast(this, object.getString("error_msg"));
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();

                            }


                        } else {
                            logOut();
                            Utils.shortToast(this, getString(R.string.an_error));
                            progressDialog.dismiss();
                        }
                    }, error -> {
                logOut();
                Utils.shortToast(this, error.getMessage());
                progressDialog.dismiss();
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> postMap = new HashMap<>();
                    postMap.put("fuid", fb_uid);
                    return postMap;
                }
            };

            if (requestQueue == null)
                requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this));
            requestQueue.add(stringRequest);


        } else {
            logOut();
            Utils.shortToast(this, getString(R.string.an_error));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.getShareprefValue_String(this, "user_active").equals("finish")) {
            logOut();
        }
        checkOnApp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BackgroundService.class);
        if (startService(intent) == null)
            startService(intent);
    }

    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }
}
