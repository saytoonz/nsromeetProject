package com.nsromapa.nsromeet.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.snackbar.Snackbar;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class IDTypeSetupActivity extends AppCompatActivity {
    private ImageView staff_idIV;
    private ImageView national_id;
    private ImageView additional_img;
    private EditText phone;
    private EditText name;
    private EditText optionNoteET;
    private Button sendRequest, cancel_action;

    private Bitmap staff_idBtmp = null;
    private Bitmap national_idBtmp = null;
    private Bitmap additional_imgBtmp = null;

    private int STAFF_RCODE = 110, NATIONAL_RCODE = 111, ADDITIONAL_RCODE = 112;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_d_type_setup);

        if (!getIntent().hasExtra("requested_id_type"))
            finish();

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getString(R.string.id_request));


        staff_idIV = findViewById(R.id.staff_idIV);
        national_id = findViewById(R.id.national_id);
        additional_img = findViewById(R.id.additional_img);
        phone = findViewById(R.id.phone);
        name = findViewById(R.id.name);
        optionNoteET = findViewById(R.id.optionNoteET);
        sendRequest = findViewById(R.id.sendRequest);
        cancel_action = findViewById(R.id.cancel_action);
        cancel_action.setOnClickListener(v -> finish());

        staff_idIV.setOnClickListener(v ->
                ImagePicker.Companion.with(this).crop()
                        .compress(1024).maxResultSize(1080, 1080)
                        .galleryMimeTypes(new String[]{"image/png", "image/jpg", "image/jpeg"})
                        .start(STAFF_RCODE));

        national_id.setOnClickListener(v ->
                ImagePicker.Companion.with(this).crop()
                        .compress(1024).maxResultSize(1080, 1080)
                        .galleryMimeTypes(new String[]{"image/png", "image/jpg", "image/jpeg"})
                        .start(NATIONAL_RCODE));

        additional_img.setOnClickListener(v ->
                ImagePicker.Companion.with(this).crop()
                        .compress(1024).maxResultSize(1080, 1080)
                        .galleryMimeTypes(new String[]{"image/png", "image/jpg", "image/jpeg"})
                        .start(ADDITIONAL_RCODE));

        sendRequest.setOnClickListener(v -> trySendingRequest());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);

    }

    private void trySendingRequest() {
        String phone_ = phone.getText().toString();
        String name_ = name.getText().toString();
        String optionNoteET_ = optionNoteET.getText().toString();
        JSONObject jsonObject = null;
        if (!TextUtils.isEmpty(phone_) && !TextUtils.isEmpty(name_)
                && staff_idBtmp != null && national_idBtmp != null) {

            if (phone_.length() != 10) {
                showSnackbar(getString(R.string.invalid_mobile_number));
                return;
            }
            progressDialog.show();

            ByteArrayOutputStream byteStaff = new ByteArrayOutputStream();
            staff_idBtmp.compress(Bitmap.CompressFormat.JPEG, 100, byteStaff);
            String encodedStaff = Base64.encodeToString(byteStaff.toByteArray(), Base64.DEFAULT);

            ByteArrayOutputStream byteNational = new ByteArrayOutputStream();
            national_idBtmp.compress(Bitmap.CompressFormat.JPEG, 100, byteNational);
            String encodedNational = Base64.encodeToString(byteNational.toByteArray(), Base64.DEFAULT);

            String encodedAdditional = "empty";
            if (additional_imgBtmp != null) {
                ByteArrayOutputStream byteAdditional = new ByteArrayOutputStream();
                additional_imgBtmp.compress(Bitmap.CompressFormat.JPEG, 100, byteAdditional);
                encodedAdditional = Base64.encodeToString(byteAdditional.toByteArray(), Base64.DEFAULT);
            }


            try {
                jsonObject = new JSONObject();
                jsonObject.put("name", name_);
                jsonObject.put("phone", phone_);
                jsonObject.put("optional_text", optionNoteET_);
                jsonObject.put("encodedStaff", encodedStaff);
                jsonObject.put("encodedNational", encodedNational);
                jsonObject.put("encodedAdditional", encodedAdditional);
                jsonObject.put("requested_id_type", getIntent().getStringExtra("requested_id_type"));
                jsonObject.put("uuid", Utils.getShareprefValue_String(this, "uuid"));
            } catch (JSONException e) {
                progressDialog.dismiss();
                Log.e("JSONObject Here", e.toString());
            }


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.ID_TYPE, jsonObject,
                    jObject -> {
                        requestQueue.getCache().clear();
                        try {
                            if (!jObject.getBoolean("error")) {
                                progressDialog.dismiss();
                                Toast.makeText(this,
                                        "We have received your request, we will attend to you as soon as possible, thank you.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                progressDialog.dismiss();
                                showSnackbar(jObject.getString("error_msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }, volleyError -> {
                progressDialog.dismiss();
                Log.e("uploadImage", volleyError.toString());
                Utils.shortToast(this, volleyError.getMessage());
            });


            if (requestQueue == null)
                requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonObjectRequest);


        } else {
            showSnackbar("Staff ID photo, National ID photo , Phone Number and Full name are the basic requirements");
        }
    }


    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == STAFF_RCODE) {
                if (data != null) {
                    File image = new File(data.getData().getPath());
                    if (image != null) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                        staff_idIV.setImageBitmap(bitmap);
                        staff_idBtmp = bitmap;
                    } else {
                        Utils.shortToast(this, getString(R.string.no_image_found));
                    }
                } else {
                    Utils.shortToast(this, getString(R.string.no_image_found));
                }

            } else if (requestCode == NATIONAL_RCODE) {
                if (data != null) {
                    File image = new File(data.getData().getPath());
                    if (image != null) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                        national_id.setImageBitmap(bitmap);
                        national_idBtmp = bitmap;
                    } else {
                        Utils.shortToast(this, getString(R.string.no_image_found));
                    }
                } else {
                    Utils.shortToast(this, getString(R.string.no_image_found));
                }

            } else if (requestCode == ADDITIONAL_RCODE) {
                if (data != null) {
                    File image = new File(data.getData().getPath());
                    if (image != null) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                        additional_img.setImageBitmap(bitmap);
                        additional_imgBtmp = bitmap;
                    } else {
                        Utils.shortToast(this, getString(R.string.no_image_found));
                    }
                } else {
                    Utils.shortToast(this, getString(R.string.no_image_found));
                }

            }
        }
    }
}
