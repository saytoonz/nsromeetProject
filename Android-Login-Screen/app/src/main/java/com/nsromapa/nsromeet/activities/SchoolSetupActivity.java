package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.adapters.SchoolSetUpAdapter;
import com.nsromapa.nsromeet.models.SchoolsetupModel;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SchoolSetupActivity extends AppCompatActivity {

    private TextView createTV;
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private LinearLayout error_container, retry;
    private TextView noticeTV;
    private List<SchoolsetupModel> modelList = new ArrayList<>();
    private SchoolSetUpAdapter adapter;
    private RequestQueue requestQueue, requestQueue2;
    private static Intent grabedIntent = null;
    private static Context context = null;
    private static Activity activity = null;
    private NestedScrollView selectionNested;
    private ScrollView additionScrollview;
    private Button create;
    private EditText location, name;
    private ImageView back;
    private String pageTitle;
    private TextView toolbarText;
    private ProgressDialog progressDialog;


    public static void Chosen(SchoolsetupModel item, String alertQuestion) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.alert_ok_alert);
        TextView alertText = dialog.findViewById(R.id.alertText);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
        Button okBtn = dialog.findViewById(R.id.okBtn);
        alertText.setText(alertQuestion);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        okBtn.setOnClickListener(v -> {
            Intent activityResult = grabedIntent;
            activityResult = activityResult == null ? new Intent() : activityResult;
            activityResult.putExtra("item", item);
            activity.setResult(Activity.RESULT_OK, activityResult);
            dialog.dismiss();
            activity.finish();
        });

        dialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_setup);

        grabedIntent = getIntent();
        context = this;
        activity = this;

        selectionNested = findViewById(R.id.selectionNested);
        additionScrollview = findViewById(R.id.additionScrollview);

        back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        toolbarText = findViewById(R.id.toolbar_text);
        pageTitle = getIntent().getStringExtra("title");
        toolbarText.setText(pageTitle);

        error_container = findViewById(R.id.error_container);
        retry = findViewById(R.id.retry);
        createTV = findViewById(R.id.createTV);
        progressBar = findViewById(R.id.progressBar);
        noticeTV = findViewById(R.id.noticeTV);
        recycler = findViewById(R.id.recycler);

        create = findViewById(R.id.create);
        location = findViewById(R.id.location);
        name = findViewById(R.id.name);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new SchoolSetUpAdapter(this, modelList);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);

        switch (Objects.requireNonNull(getIntent().getStringExtra("awt"))) {
            case "school":
                selectionNested.setVisibility(View.VISIBLE);
                additionScrollview.setVisibility(View.GONE);
                noticeTV.setText(getString(R.string.select_or_create_school));
                createTV.setText(getString(R.string.create_school));
                getSchools();
                retry.setOnClickListener(v -> getSchools());
                createTV.setOnClickListener(v -> createNewSchool());
                break;

            case "faculty":
                selectionNested.setVisibility(View.VISIBLE);
                additionScrollview.setVisibility(View.GONE);
                noticeTV.setText(getString(R.string.select_or_create_faculty));
                createTV.setText(getString(R.string.create_faculty));
                getFaculties();
                retry.setOnClickListener(v -> getFaculties());
                createTV.setOnClickListener(v -> createNewFaculty());
                break;

            case "department":
                selectionNested.setVisibility(View.VISIBLE);
                additionScrollview.setVisibility(View.GONE);
                noticeTV.setText(getString(R.string.select_or_create_department));
                createTV.setText(getString(R.string.create_department));
                getDepartments();
                retry.setOnClickListener(v -> getDepartments());
                createTV.setOnClickListener(v -> createNewDepartment());
                break;

            case "class":
                selectionNested.setVisibility(View.VISIBLE);
                additionScrollview.setVisibility(View.GONE);
                noticeTV.setText(getString(R.string.select_or_create_class));
                createTV.setText(getString(R.string.create_class));
                getClasses();
                retry.setOnClickListener(v -> getClasses());
                createTV.setOnClickListener(v -> createNewClass());
                break;

            case "level":
                noticeTV.setText(getString(R.string.select_your_level));
                createTV.setVisibility(View.GONE);
                getLevels();
                retry.setOnClickListener(v -> getLevels());
                break;

            case "idType":
                noticeTV.setText(getString(R.string.select_your_id));
                createTV.setVisibility(View.GONE);
                getIDtype();
                retry.setOnClickListener(v -> getIDtype());
                break;

            default:
                Utils.longToast(this, getString(R.string.some_error_occurred));
                finish();
        }

    }


    private void createNewSchool() {
        selectionNested.setVisibility(View.GONE);
        additionScrollview.setVisibility(View.VISIBLE);
        toolbarText.setText(String.format(getString(R.string.create_new_), getString(R.string.school)));

        create.setText(String.format(getString(R.string.create_new_), getString(R.string.school)));
        location.setVisibility(View.VISIBLE);
        name.setHint(String.format(getString(R.string.enter_name), getString(R.string.school)));
        back.setOnClickListener(v -> {
            setUPScreen();
        });
        create.setOnClickListener(v -> {
            String name_ = name.getText().toString();
            String location_ = location.getText().toString();
            name.setText("");
            location.setText("");
            if (!TextUtils.isEmpty(name_)) {
                if (!TextUtils.isEmpty(location_)) {
                    tryToCreateNewSchool(name_, location_);
                } else {
                    Utils.shortToast(this, "Please Enter location of school");
                }
            } else {
                Utils.shortToast(this, String.format("%s " + getString(R.string.enter_name), "Please", "School"));
            }
        });
    }

    private void tryToCreateNewSchool(String name_, String location_) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(this);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.ADD_SCHOOL_URL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject school = object.getJSONObject("school");
                                progressDialog.dismiss();
                                Utils.longToast(this, "School created successfully");

                                SchoolsetupModel item = new SchoolsetupModel(
                                        getString(R.string.its_school),
                                        school.getString("tid"),
                                        school.getString("name"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        school.getString("conference_id"),
                                        "0",
                                        school.getString("created_by"),
                                        school.getString("date_added"),
                                        school.getString("location")
                                );
                                AddItemToRecycler(item);
                                Chosen(item, String.format(context.getString(R.string.confirm_your_choice),
                                        item.getSchoolName(), context.getString(R.string.school)));
                            } else {
                                Utils.shortToast(this, object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                        }

                    } else {
                        Utils.shortToast(this, this.getString(R.string.an_error));
                        progressDialog.dismiss();
                    }
                }, error -> Utils.shortToast(this, error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("name", name_);
                postMap.put("location", location_);
                postMap.put("uuid", Utils.getShareprefValue_String(SchoolSetupActivity.this, "uuid"));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(this);
        requestQueue2.add(stringRequest);
    }

    private void createNewFaculty() {
        selectionNested.setVisibility(View.GONE);
        additionScrollview.setVisibility(View.VISIBLE);
        toolbarText.setText(String.format(getString(R.string.create_new_), getString(R.string.faculty)));

        create.setText(String.format(getString(R.string.create_new_), getString(R.string.faculty)));
        name.setHint(String.format(getString(R.string.enter_name), getString(R.string.faculty)));
        back.setOnClickListener(v -> {
            setUPScreen();
        });

        create.setOnClickListener(v -> {
            String name_ = name.getText().toString();
            name.setText("");
            if (!TextUtils.isEmpty(name_)) {
                tryToCreateNewFaculty(name_);
            } else {
                Utils.shortToast(this, String.format("%s " + getString(R.string.enter_name), "Please", "Faculty"));
            }
        });
    }

    private void tryToCreateNewFaculty(String name_) {
        String school_id = Utils.getShareprefValue_String(this, "school");
        if (TextUtils.isEmpty(school_id)) {
            Utils.longToast(this, "Sorry, we had a problem with your school, you need to choose a valid School");
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(this);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.ADD_FACULTY_URL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject faculty = object.getJSONObject("school");
                                progressDialog.dismiss();
                                Utils.longToast(this, "Faculty created successfully");

                                SchoolsetupModel item = new SchoolsetupModel(
                                        getString(R.string.its_faculty),
                                        faculty.getString("tid"),
                                        faculty.getString("school"),
                                        faculty.getString("name"),
                                        "",
                                        "",
                                        "",
                                        faculty.getString("conference_id"),
                                        faculty.getString("reports"),
                                        faculty.getString("created_by"),
                                        faculty.getString("date_added"),
                                        ""
                                );
                                AddItemToRecycler(item);
                                Chosen(item, String.format(
                                        context.getString(R.string.confirm_your_choice), item.getFacultyName(),
                                        context.getString(R.string.faculty)));
                            } else {
                                Utils.shortToast(this, object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                        }

                    } else {
                        Utils.shortToast(this, this.getString(R.string.an_error));
                        progressDialog.dismiss();
                    }
                }, error -> Utils.shortToast(this, error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("name", name_);
                postMap.put("school", school_id);
                postMap.put("uuid", Utils.getShareprefValue_String(SchoolSetupActivity.this, "uuid"));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(this);
        requestQueue2.add(stringRequest);
    }


    private void createNewDepartment() {
        selectionNested.setVisibility(View.GONE);
        additionScrollview.setVisibility(View.VISIBLE);
        toolbarText.setText(String.format(getString(R.string.create_new_), getString(R.string.department)));

        create.setText(String.format(getString(R.string.create_new_), getString(R.string.department)));
        name.setHint(String.format(getString(R.string.enter_name), getString(R.string.department)));
        back.setOnClickListener(v -> setUPScreen());

        create.setOnClickListener(v -> {
            String name_ = name.getText().toString();
            name.setText("");
            if (!TextUtils.isEmpty(name_)) {
                tryToCreateNewDepartment(name_);
            } else {
                Utils.shortToast(this, String.format("%s " + getString(R.string.enter_name), "Please", "Department"));
            }
        });
    }

    private void tryToCreateNewDepartment(String name_) {
        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");
        if (TextUtils.isEmpty(school_id)) {
            Utils.longToast(this, "Sorry, we had a problem with your school, you need to choose a valid School");
            return;
        }
        if (TextUtils.isEmpty(faculty_id)) {
            Utils.longToast(this, "Sorry, we had a problem with your faculty, you need to choose a valid Faculty");
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(this);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.ADD_DEPARTMENT_URL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject department = object.getJSONObject("school");
                                progressDialog.dismiss();
                                Utils.longToast(this, "Department created successfully");

                                SchoolsetupModel item = new SchoolsetupModel(
                                        getString(R.string.its_department),
                                        department.getString("tid"),
                                        department.getString("school"),
                                        department.getString("faculty"),
                                        department.getString("name"),
                                        "",
                                        "",
                                        department.getString("conference_id"),
                                        "0",
                                        department.getString("created_by"),
                                        department.getString("date_added"),
                                        ""
                                );
                                AddItemToRecycler(item);
                                Chosen(item, String.format(context.getString(R.string.confirm_your_choice),
                                        item.getDepartmentName(), context.getString(R.string.department)));
                            } else {
                                Utils.shortToast(this, object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                        }

                    } else {
                        Utils.shortToast(this, this.getString(R.string.an_error));
                        progressDialog.dismiss();
                    }
                }, error -> Utils.shortToast(this, error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("name", name_);
                postMap.put("school", school_id);
                postMap.put("faculty", faculty_id);
                postMap.put("uuid", Utils.getShareprefValue_String(SchoolSetupActivity.this, "uuid"));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(this);
        requestQueue2.add(stringRequest);
    }


    private void createNewClass() {
        selectionNested.setVisibility(View.GONE);
        additionScrollview.setVisibility(View.VISIBLE);
        toolbarText.setText(String.format(getString(R.string.create_new_), "Class"));

        create.setText(String.format(getString(R.string.create_new_), "Class"));
        name.setHint(String.format(getString(R.string.enter_name), "Class"));
        back.setOnClickListener(v -> setUPScreen());

        create.setOnClickListener(v -> {
            String name_ = name.getText().toString();
            name.setText("");
            if (!TextUtils.isEmpty(name_)) {
                tryToCreateNewClass(name_);
            } else {
                Utils.shortToast(this, String.format("%s " + getString(R.string.enter_name), "Please", "Class"));
            }
        });
    }

    private void tryToCreateNewClass(String name_) {
        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");
        String department_id = Utils.getShareprefValue_String(this, "department");
        if (TextUtils.isEmpty(school_id)) {
            Utils.longToast(this, "Sorry, we had a problem with your school, you need to choose a valid School");
            return;
        }
        if (TextUtils.isEmpty(faculty_id)) {
            Utils.longToast(this, "Sorry, we had a problem with your faculty, you need to choose a valid Faculty");
            return;
        }
        if (TextUtils.isEmpty(department_id)) {
            Utils.longToast(this, "Sorry, we had a problem with your department, you need to choose a valid Department");
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(this);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.ADD_CLASS_URL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject class_ = object.getJSONObject("school");
                                progressDialog.dismiss();
                                Utils.longToast(this, "Class created successfully");

                                SchoolsetupModel item = new SchoolsetupModel(
                                        getString(R.string.its_class),
                                        class_.getString("tid"),
                                        class_.getString("school"),
                                        class_.getString("faculty"),
                                        class_.getString("department"),
                                        class_.getString("name"),
                                        "",
                                        class_.getString("conference_id"),
                                        "0",
                                        class_.getString("created_by"),
                                        class_.getString("date_added"),
                                        ""
                                );
                                AddItemToRecycler(item);
                                Chosen(item, String.format(context.getString(R.string.confirm_your_choice),
                                        item.getClassName(), context.getString(R.string.classs)));
                            } else {
                                Utils.shortToast(this, object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                        }

                    } else {
                        Utils.shortToast(this, this.getString(R.string.an_error));
                        progressDialog.dismiss();
                    }
                }, error -> Utils.shortToast(this, error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("name", name_);
                postMap.put("school", school_id);
                postMap.put("faculty", faculty_id);
                postMap.put("department", department_id);
                postMap.put("uuid", Utils.getShareprefValue_String(SchoolSetupActivity.this, "uuid"));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(this);
        requestQueue2.add(stringRequest);
    }



    private void setUPScreen() {
        selectionNested.setVisibility(View.VISIBLE);
        additionScrollview.setVisibility(View.GONE);
        back.setOnClickListener(v1 -> finish());
        toolbarText.setText(pageTitle);
    }

    private void AddItemToRecycler(SchoolsetupModel item){
        modelList.add(0, item);
        adapter.notifyDataSetChanged();
    }


    private void getIDtype() {
        error_container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("id_types");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject idtype = jsonArray.getJSONObject(i);
                                modelList.add(new SchoolsetupModel(
                                        getString(R.string.its_id_type),
                                        idtype.getString("id"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        idtype.getString("name"),
                                        "",
                                        idtype.getString("description"),
                                        "",
                                        idtype.getString("date_added"),
                                        idtype.getString("requirement")

                                ));

                                adapter.notifyDataSetChanged();
                            }

                            progressBar.setVisibility(View.GONE);

                        } else {
                            error_container.setVisibility(View.VISIBLE);
                            ((TextView) error_container.findViewById(R.id.errorTV)).setText(object.getString("error_msg"));
                            progressBar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        error_container.setVisibility(View.VISIBLE);
                        ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
                        progressBar.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            error_container.setVisibility(View.VISIBLE);
            ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
            progressBar.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("id_types", "true");
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getLevels() {
        error_container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("levels");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject level = jsonArray.getJSONObject(i);
                                modelList.add(new SchoolsetupModel(
                                        getString(R.string.its_level),
                                        level.getString("id"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        level.getString("name"),
                                        "",
                                        "",
                                        "",
                                        level.getString("date_added"),
                                        ""
                                ));

                                adapter.notifyDataSetChanged();
                            }

                            progressBar.setVisibility(View.GONE);

                        } else {
                            error_container.setVisibility(View.VISIBLE);
                            ((TextView) error_container.findViewById(R.id.errorTV)).setText(object.getString("error_msg"));
                            progressBar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        error_container.setVisibility(View.VISIBLE);
                        ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
                        progressBar.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            error_container.setVisibility(View.VISIBLE);
            ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
            progressBar.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("levels", "true");
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getClasses() {
        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");
        String department_id = Utils.getShareprefValue_String(this, "department");
        error_container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("classes");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject class_ = jsonArray.getJSONObject(i);
                                modelList.add(new SchoolsetupModel(
                                        getString(R.string.its_class),
                                        class_.getString("id"),
                                        class_.getString("school"),
                                        class_.getString("faculty"),
                                        class_.getString("department"),
                                        class_.getString("name"),
                                        "",
                                        class_.getString("conference_id"),
                                        class_.getString("reports"),
                                        class_.getString("created_by"),
                                        class_.getString("date_added"),
                                        ""
                                ));
                                adapter.notifyDataSetChanged();
                            }

                            progressBar.setVisibility(View.GONE);

                        } else {
                            error_container.setVisibility(View.VISIBLE);
                            ((TextView) error_container.findViewById(R.id.errorTV)).setText(object.getString("error_msg"));
                            progressBar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        error_container.setVisibility(View.VISIBLE);
                        ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
                        progressBar.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            error_container.setVisibility(View.VISIBLE);
            ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
            progressBar.setVisibility(View.GONE);

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
        String school_id = Utils.getShareprefValue_String(this, "school");
        String faculty_id = Utils.getShareprefValue_String(this, "faculty");
        error_container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("departments");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject department = jsonArray.getJSONObject(i);
                                modelList.add(new SchoolsetupModel(
                                        getString(R.string.its_department),
                                        department.getString("id"),
                                        department.getString("school"),
                                        department.getString("faculty"),
                                        department.getString("name"),
                                        "",
                                        "",
                                        department.getString("conference_id"),
                                        department.getString("reports"),
                                        department.getString("created_by"),
                                        department.getString("date_added"),
                                        ""
                                ));
                                adapter.notifyDataSetChanged();
                            }

                            progressBar.setVisibility(View.GONE);

                        } else {
                            error_container.setVisibility(View.VISIBLE);
                            ((TextView) error_container.findViewById(R.id.errorTV)).setText(object.getString("error_msg"));
                            progressBar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        error_container.setVisibility(View.VISIBLE);
                        ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
                        progressBar.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            error_container.setVisibility(View.VISIBLE);
            ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
            progressBar.setVisibility(View.GONE);

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
        String school_id = Utils.getShareprefValue_String(this, "school");
        error_container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("faculties");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject faculty = jsonArray.getJSONObject(i);
                                modelList.add(new SchoolsetupModel(
                                        getString(R.string.its_faculty),
                                        faculty.getString("id"),
                                        faculty.getString("school"),
                                        faculty.getString("name"),
                                        "",
                                        "",
                                        "",
                                        faculty.getString("conference_id"),
                                        faculty.getString("reports"),
                                        faculty.getString("created_by"),
                                        faculty.getString("date_added"),
                                        ""
                                ));
                                adapter.notifyDataSetChanged();
                            }

                            progressBar.setVisibility(View.GONE);

                        } else {
                            error_container.setVisibility(View.VISIBLE);
                            ((TextView) error_container.findViewById(R.id.errorTV)).setText(object.getString("error_msg"));
                            progressBar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        error_container.setVisibility(View.VISIBLE);
                        ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
                        progressBar.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            error_container.setVisibility(View.VISIBLE);
            ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
            progressBar.setVisibility(View.GONE);

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

    private void getSchools() {
        error_container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GET_ALL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("schools");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject school = jsonArray.getJSONObject(i);
                                modelList.add(new SchoolsetupModel(
                                        getString(R.string.its_school),
                                        school.getString("id"),
                                        school.getString("name"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        school.getString("conference_id"),
                                        school.getString("reports"),
                                        school.getString("created_by"),
                                        school.getString("date_added"),
                                        school.getString("location")
                                ));
                                adapter.notifyDataSetChanged();
                            }

                            progressBar.setVisibility(View.GONE);

                        } else {
                            error_container.setVisibility(View.VISIBLE);
                            ((TextView) error_container.findViewById(R.id.errorTV)).setText(object.getString("error_msg"));
                            progressBar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        error_container.setVisibility(View.VISIBLE);
                        ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
                        progressBar.setVisibility(View.GONE);
                    }


                }, error -> {
            Utils.longToast(this, error.getMessage());

            error_container.setVisibility(View.VISIBLE);
            ((TextView) error_container.findViewById(R.id.errorTV)).setText(getString(R.string.an_error));
            progressBar.setVisibility(View.GONE);

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("all_schools", "true");
                return postMap;
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


    @Override
    public void onBackPressed() {
        if (selectionNested.getVisibility() != View.VISIBLE) {
            setUPScreen();
        } else {
            super.onBackPressed();
        }
    }
}
