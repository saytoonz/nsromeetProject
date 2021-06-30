package com.sayt.godslove;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.sayt.godslove.adapter.ClassesAdapter;
import com.sayt.godslove.adapter.StudentsAdapter;
import com.sayt.godslove.models.ClassesModel;
import com.sayt.godslove.models.StudentsModel;
import com.sayt.godslove.services.BG_Service;
import com.sayt.godslove.utils.Constants;
import com.sayt.godslove.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ManagementActivity extends AppCompatActivity {

    private static final String TAG = "ManagementActivity";
    private String[] addtype;
    private List<String> the_classes, the_months;
    private Spinner add_type, classes, month;
    private EditText name_or_id;
    private Button submit_student;
    private TextView view_students;
    private TextView view_classes, empDataTV;
    private boolean add_student = true;
    private RequestQueue requestQueue;
    private boolean sendStudentSignal = true;
    private ProgressDialog progressDialog;
    private ProgressBar progress_circular;
    private RecyclerView recycler;
    private LinearLayout add_updateLL;
    private RelativeLayout student_classRL;
    private boolean backToClose = true;

    private StudentsAdapter studentsAdapter;
    List<StudentsModel>_list_ = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        addtype = new String[]{"Add Student", "Update"};
        the_classes = getClasses();
        the_months = getMonths();
        studentsAdapter = new StudentsAdapter(this,   _list_);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);


        add_type = findViewById(R.id.add_type);
        name_or_id = findViewById(R.id.name_or_id);
        classes = findViewById(R.id.classes);
        month = findViewById(R.id.month);
        submit_student = findViewById(R.id.submit_student);
        view_students = findViewById(R.id.view_students);
        view_classes = findViewById(R.id.view_classes);
        add_updateLL = findViewById(R.id.add_updateLL);
        student_classRL = findViewById(R.id.student_classRL);
        progress_circular = findViewById(R.id.progress_circular);
        empDataTV = findViewById(R.id.empDataTV);
        recycler = findViewById(R.id.recycler);
        getStudents();
        add_updateSetup();

    }

    private void add_updateSetup() {
        add_updateLL.setVisibility(View.VISIBLE);
        student_classRL.setVisibility(View.GONE);

        backToClose = true;

        add_type.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, addtype));
        classes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, the_classes));
        month.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, the_months));

        add_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = add_type.getSelectedItem().toString().trim();
                if (item.equalsIgnoreCase("Add Student")) {
                    add_student = true;
                    name_or_id.setHint("Enter Student name");
                    name_or_id.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (item.equalsIgnoreCase("Update")) {
                    add_student = false;
                    name_or_id.setHint("App id or Secret code..");
                    name_or_id.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        submit_student.setOnClickListener(v -> {
            String typed = name_or_id.getText().toString();
            if (TextUtils.isEmpty(typed)) {
                Snackbar.make(findViewById(android.R.id.content), "Name or ID required", Snackbar.LENGTH_LONG).show();
                return;
            }
            sendStudentSignal(typed);
        });


        view_students.setOnClickListener(v -> viewStudents());
        view_classes.setOnClickListener(v -> viewClasses());
    }

    private void viewStudents() {
        backToClose = false;
        add_updateLL.setVisibility(View.GONE);
        student_classRL.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
        recycler.removeAllViews();

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(studentsAdapter);
        recycler.setHasFixedSize(true);
        studentsAdapter.notifyDataSetChanged();

        if ( _list_.size() < 1) {
            empDataTV.setVisibility(View.VISIBLE);
            progress_circular.setVisibility(View.GONE);
        } else {
            empDataTV.setVisibility(View.GONE);
            progress_circular.setVisibility(View.GONE);
        }

    }


    private void viewClasses() {
        backToClose = false;
        add_updateLL.setVisibility(View.GONE);
        student_classRL.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
        recycler.removeAllViews();

        List<ClassesModel> list = grabClasses();
        ClassesAdapter adapter = new ClassesAdapter(this, list);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);
        adapter.notifyDataSetChanged();

        if (list.size() < 1) {
            empDataTV.setVisibility(View.VISIBLE);
            progress_circular.setVisibility(View.GONE);
        } else {
            empDataTV.setVisibility(View.GONE);
            progress_circular.setVisibility(View.GONE);
        }

    }

    private List<ClassesModel> grabClasses() {
        List<ClassesModel> grabed = new ArrayList<>();
        String classes = Utils.getShareprefValue_String(this, "classes");
        if (TextUtils.isEmpty(classes))
            return grabed;

        try {
            JSONObject object = new JSONObject(classes);
            JSONArray array = object.getJSONArray("chatLists");

            for (int i = 0; i < array.length(); i++) {
                JSONObject appObj = array.getJSONObject(i);

                String id = appObj.getString("id");
                String name = appObj.getString("name");
                String conference_id = appObj.getString("conference_id");
                String date_added = appObj.getString("date_added");
                String active = appObj.getString("active");

                if (active.equals("yes")) {
                    grabed.add(new ClassesModel(name, conference_id, date_added));
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return grabed;
    }


    private void getStudents() {
        progress_circular.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_URL,
                response -> {
                    Log.e(TAG, "getStudents: "+ response);
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONArray array = object.getJSONArray("students");

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject student = array.getJSONObject(i);

                                String id = student.getString("id");
                                String name = student.getString("student");
                                String _class = student.getString("_class");
                                String secret_key = student.getString("secret_key");
                                String month = student.getString("month");
                                String conferenceId = student.getString("conferenceId");
                                String permit = student.getString("permit");
                                String last_update = student.getString("last_update");
                                String date_added = student.getString("date_added");
                                String active = student.getString("active");

                                _list_.add(new StudentsModel(id, name, _class, secret_key, month,
                                        conferenceId, permit, last_update, date_added, active));

                                studentsAdapter.notifyDataSetChanged();

                                Log.e(TAG, "getStudents: "+_list_);

                            }

                            progress_circular.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackbar("Unknown Error...");
                            progress_circular.setVisibility(View.GONE);

                        }

                    } else {
                        progress_circular.setVisibility(View.GONE);
                        showSnackbar("Error....");
                    }

                }, error -> {
            progress_circular.setVisibility(View.GONE);
            showSnackbar("Error occured....");
            Log.e(TAG, "getStudents: " + error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("students", "");
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void sendStudentSignal(String typed) {
        String addOrUpdate = "update";
        if (add_student) {
            addOrUpdate = "add";
        }
        String studentClass = classes.getSelectedItem().toString().trim();
        String getMonth = month.getSelectedItem().toString().trim();

        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
                requestQueue.cancelAll(this);
        });
        progressDialog.show();

        if (!sendStudentSignal) {
            return;
        }
        sendStudentSignal = false;
        String finalAddOrUpdate = addOrUpdate;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.STUDENT_ADD_UPDATE,
                response -> {
                    if (response != null) {

                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getBoolean("error")) {
                                showSnackbar(object.getString("msg"));
                            } else {
                                name_or_id.setInputType(InputType.TYPE_CLASS_TEXT);
                                name_or_id.setText(object.getString("code"));
                                showSnackbar(object.getString("msg"));
                            }
                            if (progressDialog != null) progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackbar("There was an error...");
                            sendStudentSignal = true;
                            if (progressDialog != null) progressDialog.dismiss();

                        }
                    } else {
                        sendStudentSignal = true;
                        if (progressDialog != null) progressDialog.dismiss();
                        showSnackbar("There was an error...");
                    }
                }, error -> {
            sendStudentSignal = true;
            if (progressDialog != null) progressDialog.dismiss();
            showSnackbar("There was an error...");
            Log.e(TAG, "sendStudentSignal: " + error);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("addOrUpdate", finalAddOrUpdate);
                post.put("_class", studentClass);
                post.put("month", getMonth);
                post.put("typed", typed);
                return post;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private List<String> getClasses() {
        List<String> grabed = new ArrayList<>();
        String classes = Utils.getShareprefValue_String(this, "classes");
        if (TextUtils.isEmpty(classes))
            return grabed;

        try {
            JSONObject object = new JSONObject(classes);
            JSONArray array = object.getJSONArray("chatLists");

            for (int i = 0; i < array.length(); i++) {
                JSONObject appObj = array.getJSONObject(i);

                String id = appObj.getString("id");
                String name = appObj.getString("name");
                String conference_id = appObj.getString("conference_id");
                String date_added = appObj.getString("date_added");
                String active = appObj.getString("active");

                if (active.equals("yes")) {
                    grabed.add(name);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return grabed;
    }

    private List<String> getMonths() {
        List<String> grabed = new ArrayList<>();
        String classes = Utils.getShareprefValue_String(this, "months");
        if (TextUtils.isEmpty(classes))
            return grabed;

        try {
            JSONObject object = new JSONObject(classes);
            JSONArray array = object.getJSONArray("months");

            for (int i = 0; i < array.length(); i++) {
                JSONObject appObj = array.getJSONObject(i);

                String id = appObj.getString("id");
                String name = appObj.getString("name");
                String year = appObj.getString("year");
                String date_added = appObj.getString("date_added");
                String active = appObj.getString("active");

                if (active.equals("yes")) {
                    String text = name + " " + year;
                    grabed.add(text);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return grabed;
    }


    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message,
                Snackbar.LENGTH_SHORT).show();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BG_Service.class);
        if (startService(intent) == null)
            startService(intent);
    }


    @Override
    public void onBackPressed() {
        if (!backToClose)
            add_updateSetup();
        else
            super.onBackPressed();
    }
}
