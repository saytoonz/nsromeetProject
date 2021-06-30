package com.sayt.godslove;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.sayt.godslove.adapter.Added_Classes_Adapter;
import com.sayt.godslove.models.Added_Classes_Model;
import com.sayt.godslove.recording.RecorderActivity;
import com.sayt.godslove.services.BG_Service;
import com.sayt.godslove.utils.Constants;
import com.sayt.godslove.utils.Utils;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private RecyclerView recycler;
    private EditText editText;
    private Button button;
    private TextView empDataTV;
    private Added_Classes_Adapter adapter;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    private boolean checkSecreteKey = true;

    private List<Added_Classes_Model> classes_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("count", 0).apply();
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        empDataTV = findViewById(R.id.empDataTV);
        recycler = findViewById(R.id.recycler);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");





        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);


        String text = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getString("id", "");

        if (text.length() > 0) {
            Intent intent = new Intent(this, RecorderActivity.class);
            intent.putExtra("what", "call");
            intent.putExtra("id", text);
            startActivity(intent);
        }












        button.setOnClickListener(v -> {
            String secret_key = editText.getText().toString();
            if (TextUtils.isEmpty(secret_key)) {
                showSnackbar(getString(R.string.secret_code_cant_be_empty));
                return;
            }

            checkSecreteKey(secret_key);
        });

        classes_list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new Added_Classes_Adapter(this, classes_list);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);

        getAllAddedClasses();

    }

    private void checkSecreteKey(String secret_key) {
        if (!checkSecreteKey)
            return;
        checkSecreteKey = false;

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.STUDENT,
                response -> {
                    if (response != null) {
                        Log.e(TAG, "checkSecreteKey: " + response);
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONObject student = object.getJSONObject("student");

                            if (student.getBoolean("error")) {
                                showSnackbar(student.getString("msg"));
                            } else {

                                String tid = student.getString("id");
                                String studentname = student.getString("student");
                                String _class = student.getString("_class");
                                String secret_k = student.getString("secret_key");
                                String month = student.getString("month");
                                String conferenceId = student.getString("conferenceId");
                                String permit = student.getString("permit");
                                String date_added = student.getString("date_added");
                                String active = student.getString("active");


                                DBHelper dbHelper = new DBHelper(this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                Cursor cursor = db.rawQuery("SELECT * FROM 'classes' WHERE tid='"
                                        + tid+ "'", null);
                                cursor.moveToFirst();
                                if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                                    ContentValues values = new ContentValues();
                                    values.put("tid", tid);
                                    values.put("student", studentname);
                                    values.put("_class", _class);
                                    values.put("secret_key", secret_k);
                                    values.put("month", month);
                                    values.put("conferenceId", conferenceId);
                                    values.put("permit", permit);
                                    values.put("active", active);
                                    long newRowId = db.insert("classes", null, values);
                                    showSnackbar(studentname+ "added to your list");
                                } else {
                                    showSnackbar("Student already in your list");
                                }
                                editText.setText("");

                                cursor.close();
                                db.close();
                                getAllAddedClasses();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        checkSecreteKey = true;
                        progressDialog.dismiss();
                    } else {
                        checkSecreteKey = true;
                        progressDialog.dismiss();
                    }

                }, error -> {
            checkSecreteKey = true;
            progressDialog.dismiss();
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


    private void getAllAddedClasses() {
        classes_list.clear();
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"id", "tid", "student", "_class", "secret_key",
                "month", "conferenceId", "permit", "active"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("classes", projection, null, null, null, null, sortOrder);

        List<Added_Classes_Model> list = new ArrayList<>();
        while (cursor.moveToNext()) {

            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String tid = cursor.getString(cursor.getColumnIndexOrThrow("tid"));
            String student = cursor.getString(cursor.getColumnIndexOrThrow("student"));
            String _class = cursor.getString(cursor.getColumnIndexOrThrow("_class"));
            String secret_key = cursor.getString(cursor.getColumnIndexOrThrow("secret_key"));
            String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
            String conferenceId = cursor.getString(cursor.getColumnIndexOrThrow("conferenceId"));
            String permit = cursor.getString(cursor.getColumnIndexOrThrow("permit"));
            String active = cursor.getString(cursor.getColumnIndexOrThrow("active"));

            if (active.equals("yes") || permit.equals("granted"))
                list.add(new Added_Classes_Model(
                        String.valueOf(id), tid, student, _class, secret_key, month, conferenceId,
                        permit, active));

        }
        cursor.close();
        db.close();
        classes_list.addAll(list);
        adapter.notifyDataSetChanged();


        if (classes_list.size() < 1) {
            empDataTV.setVisibility(View.VISIBLE);
        } else {
            empDataTV.setVisibility(View.GONE);
        }
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

            if (!Constants.getServerUrl().equals(serverUrl)){
                Log.e(TAG, "checkOnApp: ======> "+serverUrl );
                Constants.setServerUrl(serverUrl);
            }

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
                            .show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message,
                Snackbar.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getAllAddedClasses();
        checkOnApp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BG_Service.class);
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
