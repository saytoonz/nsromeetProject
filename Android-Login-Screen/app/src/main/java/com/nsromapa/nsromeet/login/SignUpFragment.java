package com.nsromapa.nsromeet.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nsromapa.nsromeet.activities.MainActivity;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.activities.VerifyMobile;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpFragment extends Fragment implements OnSignUpListener {
    private static final String TAG = "SignUpFragment";
    private RequestQueue requestQueue;
    private EditText _username, _fullname;
    private EditText _password;
    private ProgressDialog progressDialog;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        _username = view.findViewById(R.id.username);
        _fullname = view.findViewById(R.id.fullname);
        _password = view.findViewById(R.id.password);
        return view;
    }

    @Override
    public void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }

    @Override
    public void signUp() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> requestQueue.cancelAll(Objects.requireNonNull(getActivity())));


        String username_ = _username.getText().toString();
        String name_ = _fullname.getText().toString();
        String password_ = _password.getText().toString();
        if (!TextUtils.isEmpty(username_)) {
            if (!TextUtils.isEmpty(name_)) {
                    if (!TextUtils.isEmpty(password_)) {

                            progressDialog.show();

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SIGNUP_URL,
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
                                                        Utils.saveInSharepref_String(getContext(), "uuid", uid);
                                                        Utils.saveInSharepref_String(getContext(), "fuid", fuid);
                                                        Utils.saveInSharepref_String(getContext(), "uname", uname);
                                                        Utils.saveInSharepref_String(getContext(), "idtype", id_type);
                                                        Utils.saveInSharepref_String(getContext(), "level", level);
                                                        Utils.saveInSharepref_String(getContext(), "name", name);
                                                        Utils.saveInSharepref_String(getContext(), "phone", phone);
                                                        Utils.saveInSharepref_String(getContext(), "image", image);
                                                        Utils.saveInSharepref_String(getContext(), "active", active);

                                                        if (!TextUtils.isEmpty(school))
                                                            Utils.SharedPrefSchool(getContext(), object.getJSONObject("school"));

                                                        if (!TextUtils.isEmpty(faculty))
                                                            Utils.SharedPrefFaculty(getContext(), object.getJSONObject("faculty"));

                                                        if (!TextUtils.isEmpty(department))
                                                            Utils.SharedPrefDepartment(getContext(), object.getJSONObject("department"));

                                                        if (!TextUtils.isEmpty(class_))
                                                            Utils.SharedPrefClass(getContext(), object.getJSONObject("class"));


                                                        progressDialog.dismiss();

                                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        Objects.requireNonNull(getActivity()).finish();

                                                    } else if (active.equalsIgnoreCase("n_v")) {
                                                        Utils.saveInSharepref_String(getContext(), "tid", tid);
                                                        progressDialog.dismiss();

                                                        Intent intent = new Intent(getContext(), VerifyMobile.class);
                                                        startActivity(intent);
                                                        Objects.requireNonNull(getActivity()).finish();
                                                    } else {
                                                        Utils.longToast(getContext(), Objects.requireNonNull(getActivity()).getString(R.string.account_not_exist));
                                                        progressDialog.dismiss();
                                                    }

                                                } else {
                                                    Utils.shortToast(getContext(), object.getString("error_msg"));
                                                    progressDialog.dismiss();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                progressDialog.dismiss();

                                            }


                                        } else {
                                            Utils.shortToast(getContext(), this.getString(R.string.an_error));
                                            progressDialog.dismiss();
                                        }
                                    }, error -> {
                                Utils.shortToast(getContext(), error.getMessage());
                                progressDialog.dismiss();
                            }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> postMap = new HashMap<>();
                                    postMap.put("name", name_);
                                    postMap.put("username", username_);
                                    postMap.put("mobile", "");
                                    postMap.put("password", password_);
                                    return postMap;
                                }
                            };

                            if (requestQueue == null)
                                requestQueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
                            requestQueue.add(stringRequest);



                    } else {
                        Utils.shortToast(getContext(), this.getString(R.string.enter_pass));
                    }

            } else {
                Utils.shortToast(getContext(), this.getString(R.string.enter_full_name));
            }
        } else {
            Utils.shortToast(getContext(), this.getString(R.string.enter_username));
        }
    }
}

