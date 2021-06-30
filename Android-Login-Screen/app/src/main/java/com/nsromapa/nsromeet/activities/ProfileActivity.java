package com.nsromapa.nsromeet.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.snackbar.Snackbar;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.models.SchoolsetupModel;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER = 111;
    private ImageView profile_image, change_profile_img;
    private EditText name_;
    private TextView username_, phone_;
    private Button update_basic_;

    private EditText old_pass, new_pass, confirm_new_pass;
    private Button change_pass;


    private RequestQueue requestQueue;
    private RequestQueue requestQueue2;
    private ProgressDialog progressDialog;


    private ImageButton idTypeBtn, schoolBtn;
    private ImageButton facultyBtn, departmentBtn;
    private ImageButton classBtn, levelBtn;

    private TextView idTypeTV, schoolTV;
    private TextView facultyTV, departmentTV;
    private TextView classTV, levelTV;
    private JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getString(R.string.profile));

        //Basic profile set up
        profile_image = findViewById(R.id.profile_image);
        change_profile_img = findViewById(R.id.change_profile_img);
        name_ = findViewById(R.id.name);
        username_ = findViewById(R.id.username);
        phone_ = findViewById(R.id.phone);
        update_basic_ = findViewById(R.id.update_basic);

        Glide.with(this)
                .load(Constants.BASEURL + Utils.getShareprefValue_String(this, "image"))
                .apply(new RequestOptions().placeholder(R.drawable.user))
                .into(profile_image);


        name_.setText(Utils.getShareprefValue_String(this, "name"));
        username_.setText(Utils.getShareprefValue_String(this, "uname"));
        phone_.setText(Utils.getShareprefValue_String(this, "phone"));
        update_basic_.setOnClickListener(v -> {
            String name = name_.getText().toString();
            String name_old = Utils.getShareprefValue_String(this, "name");
            ;
            if (!TextUtils.isEmpty(name)) {
                if (!name.equals(name_old)) {
                    updateSingleInfo(name, "name", "Name updated successfully!",
                            "name", null, name_);
                }
            } else
                Utils.shortToast(ProfileActivity.this, getString(R.string.name_cannot_be_empty));
        });


        //Change password setup

        old_pass = findViewById(R.id.old_pass);
        new_pass = findViewById(R.id.new_pass);
        confirm_new_pass = findViewById(R.id.confirm_new_pass);
        change_pass = findViewById(R.id.change_pass);
        change_pass.setOnClickListener(v -> {
            String OPass = old_pass.getText().toString();
            String NPass = new_pass.getText().toString();
            String CPass = confirm_new_pass.getText().toString();
            if (!TextUtils.isEmpty(OPass) && !TextUtils.isEmpty(NPass) && !TextUtils.isEmpty(CPass)) {
                if (NPass.equals(CPass)) {
                    updatePassword(OPass, NPass);
                } else {
                    Utils.longToast(ProfileActivity.this, getString(R.string.new_passwords_do_not_much));
                }
            } else {
                Utils.longToast(ProfileActivity.this, getString(R.string.all_password_fields_require));
            }
        });


        //School profile set up
        idTypeTV = findViewById(R.id.idTypeTV);
        schoolTV = findViewById(R.id.schoolTV);
        facultyTV = findViewById(R.id.facultyTV);
        departmentTV = findViewById(R.id.departmentTV);
        classTV = findViewById(R.id.classTV);
        levelTV = findViewById(R.id.levelTV);

        idTypeTV.setText(Utils.getShareprefValue_String(this, "idtype"));

        if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "school")) &&
                !TextUtils.isEmpty(Utils.getShareprefValue_String(this, "schoolName")))
            schoolTV.setText(Utils.getShareprefValue_String(this, "schoolName"));


        if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "faculty")) &&
                !TextUtils.isEmpty(Utils.getShareprefValue_String(this, "facultyName")))
            facultyTV.setText(Utils.getShareprefValue_String(this, "facultyName"));


        if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "department")) &&
                !TextUtils.isEmpty(Utils.getShareprefValue_String(this, "departmentName")))
            departmentTV.setText(Utils.getShareprefValue_String(this, "departmentName"));


        if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "class")) &&
                !TextUtils.isEmpty(Utils.getShareprefValue_String(this, "className")))
            classTV.setText(Utils.getShareprefValue_String(this, "className"));


        levelTV.setText(Utils.getShareprefValue_String(this, "level"));


        idTypeBtn = findViewById(R.id.idTypeBtn);
        schoolBtn = findViewById(R.id.schoolBtn);
        facultyBtn = findViewById(R.id.facultyBtn);
        departmentBtn = findViewById(R.id.departmentBtn);
        classBtn = findViewById(R.id.classBtn);
        levelBtn = findViewById(R.id.levelBtn);

        idTypeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SchoolSetupActivity.class);
            intent.putExtra("awt", "idType");
            intent.putExtra("title", "SetUp ID");
            startActivityForResult(intent, 1010);
        });

        schoolBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SchoolSetupActivity.class);
            intent.putExtra("awt", "school");
            intent.putExtra("title", "SetUp School");
            startActivityForResult(intent, 1011);
        });

        facultyBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "school"))) {
                Intent intent = new Intent(this, SchoolSetupActivity.class);
                intent.putExtra("awt", "faculty");
                intent.putExtra("title", "SetUp Faculty");
                startActivityForResult(intent, 1012);
            } else {
                Utils.shortToast(this, getString(R.string.setup_school_first));
            }
        });

        departmentBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "faculty"))) {
                Intent intent = new Intent(this, SchoolSetupActivity.class);
                intent.putExtra("awt", "department");
                intent.putExtra("title", "SetUp Department");
                startActivityForResult(intent, 1013);
            } else {
                Utils.shortToast(this, getString(R.string.setup_faculty_first));
            }
        });

        classBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "department"))) {
                Intent intent = new Intent(this, SchoolSetupActivity.class);
                intent.putExtra("awt", "class");
                intent.putExtra("title", "SetUp Class");
                startActivityForResult(intent, 1014);
            } else {
                Utils.shortToast(this, getString(R.string.setup_department_first));
            }
        });

        levelBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "department"))) {
                Intent intent = new Intent(this, SchoolSetupActivity.class);
                intent.putExtra("awt", "level");
                intent.putExtra("title", "SetUp Level");
                startActivityForResult(intent, 1015);
            } else {
                Utils.shortToast(this, getString(R.string.setup_class_first));
            }
        });


        if (getIntent().hasExtra("setup")) {
            if (getIntent().getBooleanExtra("setup", true))
                schoolTV.requestFocus();
            else
                profile_image.requestFocus();
        } else {
            username_.requestFocus();
        }


        progressDialog = progressDialog == null ? new ProgressDialog(this) : progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);


        change_profile_img.setOnClickListener(v -> updateProfilePhoto());

    }

    private void updatePassword(String oPass, String nPass) {
        if (progressDialog != null) progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
                requestQueue.cancelAll(this);
        });

        if (progressDialog != null) progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.CHANGE_PASS_URL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                Utils.shortToast(this, object.getString("error_msg"));
                                if (progressDialog != null) progressDialog.dismiss();
                                old_pass.setText("");
                                new_pass.setText("");
                                confirm_new_pass.setText("");
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
                }, error -> Utils.shortToast(this, error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("OPass", oPass);
                postMap.put("NPass", nPass);
                postMap.put("uuid", Utils.getShareprefValue_String(ProfileActivity.this, "uuid"));
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void updateSingleInfo(String value, String where, String success_msg,
                                  String sharePrename, TextView updateTV, EditText updateET) {
        if (progressDialog != null) progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue2 != null)
                requestQueue2.cancelAll(this);
        });

        if (progressDialog != null) progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.CHANGE_NAME_URL,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {

                                if (progressDialog != null) progressDialog.dismiss();
                                Utils.saveInSharepref_String(ProfileActivity.this, sharePrename, value);
                                if (updateET != null)
                                    updateET.setText(value);
                                if (updateTV != null)
                                    updateTV.setText(value);
                                Utils.shortToast(this, object.getString("error_msg"));
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
                }, error -> {
            Utils.shortToast(this, error.getMessage());
            if (progressDialog != null) progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("success_msg", success_msg);
                postMap.put("where", where);
                postMap.put("value", value);
                postMap.put("uuid", Utils.getShareprefValue_String(ProfileActivity.this, "uuid"));
                return postMap;
            }
        };

        if (requestQueue2 == null)
            requestQueue2 = Volley.newRequestQueue(this);
        requestQueue2.add(stringRequest);
    }


    private void updateProfilePhoto() {
        ImagePicker.Companion.with(this)
                .crop(1f, 1f)                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .galleryMimeTypes(new String[]
                        {"image/png",
                                "image/jpg",
                                "image/jpeg"})
                .start(IMAGE_PICKER);
    }

    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        try {
            jsonObject = new JSONObject();
            jsonObject.put("name", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("image", encodedImage);
            jsonObject.put("uuid", Utils.getShareprefValue_String(this, "uuid"));
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.PROFILE_IMAGE, jsonObject,
                jsonObject -> { requestQueue.getCache().clear();
                    try {
                        if (!jsonObject.getBoolean("error")){
                            Utils.saveInSharepref_String(getApplicationContext(), "image", jsonObject.getString("image"));
                            Snackbar.make(findViewById(android.R.id.content), "Image changed successfully.",
                                    Snackbar.LENGTH_LONG).show();
                        }else{
                            Utils.shortToast(this,jsonObject.getString("error_msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, volleyError -> {
            Log.e("uploadImage", volleyError.toString());
            Utils.shortToast(this, volleyError.getMessage());
        });

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICKER) {
                if (data != null) {
                    File image = new File(data.getData().getPath());
                    if (image != null){
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                        profile_image.setImageBitmap(bitmap);
                        uploadImage(bitmap);
                    }else{
                        Utils.shortToast(this, getString(R.string.no_image_found));
                    }
                } else {
                    Utils.shortToast(this, getString(R.string.no_image_found));
                }

            } else if (requestCode == 1010) {
                if (data != null) {
                    Bundle b = data.getExtras();
                    assert b != null;
                    SchoolsetupModel model = (SchoolsetupModel) b.getSerializable("item");
                    assert model != null;

                    if (!Utils.getShareprefValue_String(this, "idtype").equals(model.getLevelNmae())) {
                        if (model.getLevelNmae().equalsIgnoreCase("student")) {
                            idTypeTV.setText(model.getLevelNmae());
                            updateSingleInfo(model.getLevelNmae(), "id_type", String.format(getString(R.string.school_success), "id"),
                                    "idtype", null, null);
                            Utils.saveInSharepref_String(this, "idtype_id", model.getId());
                        } else {
                            Intent intent = new Intent(this, IDTypeSetupActivity.class);
                            intent.putExtra("requested_id_type", model.getLevelNmae());
                            startActivityForResult(intent, 2020);
                        }
                    }
                } else {
                    Utils.shortToast(this, getString(R.string.some_error_occurred));
                }

            } else if (requestCode == 1011) {
                if (data != null) {
                    Bundle b = data.getExtras();
                    assert b != null;
                    SchoolsetupModel model = (SchoolsetupModel) b.getSerializable("item");
                    assert model != null;
                    schoolTV.setText(model.getSchoolName());
                    updateSingleInfo(model.getId(), "school", String.format(getString(R.string.school_success), "School"),
                            "school", null, null);

                    Utils.saveInSharepref_String(this, "schoolName", model.getSchoolName());
                    Utils.saveInSharepref_String(this, "schoolLocation", model.getLocation());
                    Utils.saveInSharepref_String(this, "schoolConference", model.getConference());
                    Utils.saveInSharepref_String(this, "schoolCreated_by", model.getCreated_by());
                    Utils.saveInSharepref_String(this, "schoolDate_added", model.getDate_added());

                    departmentTV.setText("");
                    classTV.setText("");
                    facultyTV.setText("");
                    updateSingleInfo("", "faculty", String.format(getString(R.string.school_success), "Faculty"),
                            "faculty", null, null);
                    updateSingleInfo("", "department", String.format(getString(R.string.school_success), "Department"),
                            "department", null, null);
                    updateSingleInfo("", "class", String.format(getString(R.string.school_success), "class"),
                            "class", null, null);
                }

            } else if (requestCode == 1012) {
                if (data != null) {
                    Bundle b = data.getExtras();
                    assert b != null;
                    SchoolsetupModel model = (SchoolsetupModel) b.getSerializable("item");
                    assert model != null;
                    facultyTV.setText(model.getFacultyName());
                    updateSingleInfo(model.getId(), "faculty", String.format(getString(R.string.school_success), "Faculty"),
                            "faculty", null, null);

                    Utils.saveInSharepref_String(this, "facultyName", model.getFacultyName());
                    Utils.saveInSharepref_String(this, "facultySchool", model.getSchoolName());
                    Utils.saveInSharepref_String(this, "facultyConference", model.getConference());
                    Utils.saveInSharepref_String(this, "facultyCreated_by", model.getCreated_by());
                    Utils.saveInSharepref_String(this, "facultyDate_added", model.getDate_added());

                    departmentTV.setText("");
                    classTV.setText("");
                    updateSingleInfo("", "department", String.format(getString(R.string.school_success), "Department"),
                            "department", null, null);
                    updateSingleInfo("", "class", String.format(getString(R.string.school_success), "class"),
                            "class", null, null);
                }

            } else if (requestCode == 1013) {
                if (data != null) {
                    Bundle b = data.getExtras();
                    assert b != null;
                    SchoolsetupModel model = (SchoolsetupModel) b.getSerializable("item");
                    assert model != null;
                    departmentTV.setText(model.getDepartmentName());
                    updateSingleInfo(model.getId(), "department", String.format(getString(R.string.school_success), "Department"),
                            "department", null, null);
                    Utils.saveInSharepref_String(this, "departmentName", model.getDepartmentName());
                    Utils.saveInSharepref_String(this, "departmentFaculty", model.getFacultyName());
                    Utils.saveInSharepref_String(this, "departmentSchool", model.getSchoolName());
                    Utils.saveInSharepref_String(this, "departmentConference", model.getConference());
                    Utils.saveInSharepref_String(this, "departmentCreated_by", model.getCreated_by());
                    Utils.saveInSharepref_String(this, "departmentDate_added", model.getDate_added());

                    classTV.setText("");
                    updateSingleInfo("", "class", String.format(getString(R.string.school_success), "class"),
                            "class", null, null);
                }

            } else if (requestCode == 1014) {
                if (data != null) {
                    Bundle b = data.getExtras();
                    assert b != null;
                    SchoolsetupModel model = (SchoolsetupModel) b.getSerializable("item");
                    assert model != null;
                    classTV.setText(model.getClassName());
                    updateSingleInfo(model.getId(), "class", String.format(getString(R.string.school_success), "class"),
                            "class", null, null);
                    Utils.saveInSharepref_String(this, "className", model.getClassName());
                    Utils.saveInSharepref_String(this, "classDepartment", model.getDepartmentName());
                    Utils.saveInSharepref_String(this, "classFaculty", model.getFacultyName());
                    Utils.saveInSharepref_String(this, "classSchool", model.getSchoolName());
                    Utils.saveInSharepref_String(this, "classConference", model.getConference());
                    Utils.saveInSharepref_String(this, "classCreated_by", model.getCreated_by());
                    Utils.saveInSharepref_String(this, "classDate_added", model.getDate_added());
                }

            } else if (requestCode == 1015) {
                if (data != null) {
                    Bundle b = data.getExtras();
                    assert b != null;
                    SchoolsetupModel model = (SchoolsetupModel) b.getSerializable("item");
                    assert model != null;
                    levelTV.setText(model.getLevelNmae());
                    updateSingleInfo(model.getLevelNmae(), "level", String.format(getString(R.string.school_success), "level"),
                            "level", null, null);
                    Utils.saveInSharepref_String(this, "level_id", model.getId());
                } else {
                    Utils.shortToast(this, getString(R.string.some_error_occurred));
                }

            }
        }

    }

    @Override
    public void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);

        if (requestQueue2 != null)
            requestQueue2.cancelAll(this);
        super.onDestroy();
    }
}
