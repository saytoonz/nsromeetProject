package com.nsromapa.nsromeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class VerifyMobile extends AppCompatActivity {

    private EditText mEt1, mEt2, mEt3, mEt4, mEt5, mEt6, number;
    private Button button;
    private TextView tv_resend, tv_timer, sent_to_, noticeTV, change_number;
    private CountryCodePicker countryCodeHolder;
    private Context mContext;
    private LinearLayout change_phoneLL, verify_codeLL, linearLayout4;
    private ProgressBar progress_circular;
    private int timerTime = 60;
    private TimerTask task;
    private String phoneNumber;


    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String TAG = "VerifyMobile";
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        initialize();
        setupTextWatcher();

        mAuth = FirebaseAuth.getInstance();
    }


    private void initialize() {
        mContext = VerifyMobile.this;
        button = findViewById(R.id.button);
        number = findViewById(R.id.number);
//        tv_timer = findViewById(R.id.tv_timer);
        sent_to_ = findViewById(R.id.sent_to_);
        noticeTV = findViewById(R.id.noticeTV);
        tv_resend = findViewById(R.id.tv_resend);
        mEt1 = findViewById(R.id.otp_edit_text1);
        mEt2 = findViewById(R.id.otp_edit_text2);
        mEt3 = findViewById(R.id.otp_edit_text3);
        mEt4 = findViewById(R.id.otp_edit_text4);
        mEt5 = findViewById(R.id.otp_edit_text5);
        mEt6 = findViewById(R.id.otp_edit_text6);
        progress_circular = findViewById(R.id.progressBar);
        verify_codeLL = findViewById(R.id.verify_codeLL);
        change_number = findViewById(R.id.change_number);
        linearLayout4 = findViewById(R.id.linearLayout4);
        change_phoneLL = findViewById(R.id.change_phoneLL);
        countryCodeHolder = findViewById(R.id.countryCodeHolder);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);


        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {

                if (credential.getSmsCode() != null) {
                    setCodeGrabbed(credential.getSmsCode());
                }
//                    else {
//                        disableCodeArea(credential.getSmsCode());
//                    }

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Invalid phone number.",
                            Snackbar.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }

                progress_circular.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                showVerifyNumberPage();
            }
        };
        // [END phone_auth_callbacks]


        showEnterNumberPage();
        change_number.setOnClickListener(v -> showEnterNumberPage());
        tv_resend.setOnClickListener(v -> {
            if (mResendToken != null) {
                resendVerificationCode(phoneNumber, mResendToken);
            }
        });
    }

    private void showEnterNumberPage() {
        button.setText(getString(R.string.next));
        change_phoneLL.setVisibility(View.VISIBLE);
        verify_codeLL.setVisibility(View.GONE);
        linearLayout4.setVisibility(View.GONE);
        sent_to_.setVisibility(View.GONE);
        change_number.setVisibility(View.GONE);
        noticeTV.setText(getString(R.string.enter_mobile_notification));

        button.setOnClickListener(v1 -> {
            String number_ = number.getText().toString();
            String countryCode = countryCodeHolder.getSelectedCountryCode();
            if (TextUtils.isDigitsOnly(number_) && number_.length() >= 10) {
                phoneNumber = Utils.normalizePhoneNumber(number_, countryCode);
                progress_circular.setVisibility(View.VISIBLE);
                sendVerificationCode(phoneNumber);
            } else {
                Utils.shortToast(this, getString(R.string.invalid_mobile_number));
            }
        });
    }

    private void showVerifyNumberPage() {
        button.setText(getString(R.string.otp_button_text));
        change_phoneLL.setVisibility(View.GONE);
        verify_codeLL.setVisibility(View.VISIBLE);
        linearLayout4.setVisibility(View.VISIBLE);
        sent_to_.setVisibility(View.VISIBLE);
        change_number.setVisibility(View.VISIBLE);
        noticeTV.setText(getString(R.string.otp_title));
        sent_to_.setText(String.format(getString(R.string.sent_to_), phoneNumber));
        progress_circular.setVisibility(View.GONE);
        button.setOnClickListener(v1 -> {
            String code = getCodeEntered();
            if (!TextUtils.isEmpty(code) && code.length() == 6) {
                verifyPhoneNumberWithCode(mVerificationId, code);
            } else {
                Snackbar.make(findViewById(android.R.id.content), String.format("%s %s", getString(R.string.invalid_code), code),
                        Snackbar.LENGTH_SHORT).show();
            }
        });

//        task = new TimerTask() {
//            @Override
//            public void run() {
//                if (timerTime > 0) {
//                    setTimeOnView();
//                    timerTime = timerTime - 1;
//                }
//            }
//        };
//        new Timer().schedule(task, 1000);
    }

    public void setTimeOnView() {
//        if (timerTime > 0) {
//            tv_timer.setVisibility(View.VISIBLE);
//            tv_timer.setText(String.format(getString(R.string.timer_time), String.valueOf(timerTime)));
//        } else {
//            tv_timer.setVisibility(View.GONE);
//        }
    }

    private void sendVerificationCode(String normalizePhoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                normalizePhoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }


    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks, token);
    }
    // [END resend_verification]


    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        userGrabbed(user);

                    } else {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            userGrabbed(user);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_code),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    // [END sign_in_with_phone]


    private void userGrabbed(FirebaseUser user) {
        if (getIntent().hasExtra("its_login")) {
            if (getIntent().getBooleanExtra("its_login", false)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                updateSingleInfo(user);
            }
        } else {
            updateSingleInfo(user);
        }
    }


    private void updateSingleInfo(FirebaseUser firebaseUser) {
        if (progressDialog != null) progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
                requestQueue.cancelAll(this);
        });

        if (progressDialog != null) progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.FUID_PHONE_ACTIVE,
                response -> {
                    Log.e(TAG, "updateSingleInfo: "+response);
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

                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else if (active.equalsIgnoreCase("n_v")) {
                                    Utils.saveInSharepref_String(this, "tid", tid);
                                    progressDialog.dismiss();

                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(this, VerifyMobile.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    FirebaseAuth.getInstance().signOut();
                                    Utils.longToast(this, getString(R.string.account_not_exist));
                                    progressDialog.dismiss();
                                }

                            } else {
                                FirebaseAuth.getInstance().signOut();
                                Utils.shortToast(this, object.getString("error_msg"));
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                        }


                    } else {
                        Utils.shortToast(this, this.getString(R.string.an_error));
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                }, error -> {
            Utils.shortToast(this, error.getMessage());
            progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("phone", firebaseUser.getPhoneNumber());
                postMap.put("fuid", firebaseUser.getUid());
                postMap.put("tid", Utils.getShareprefValue_String(VerifyMobile.this, "tid"));
                return postMap;
            }
        };

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void addTextWatcher(final EditText one) {
        one.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (one.getId()) {
                    case R.id.otp_edit_text1:
                        if (one.length() == 1) {
                            mEt2.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text2:
                        if (one.length() == 1) {
                            mEt3.requestFocus();
                        } else if (one.length() == 0) {
                            mEt1.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text3:
                        if (one.length() == 1) {
                            mEt4.requestFocus();
                        } else if (one.length() == 0) {
                            mEt2.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text4:
                        if (one.length() == 1) {
                            mEt5.requestFocus();
                        } else if (one.length() == 0) {
                            mEt3.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text5:
                        if (one.length() == 1) {
                            mEt6.requestFocus();
                        } else if (one.length() == 0) {
                            mEt4.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text6:
                        if (one.length() == 1) {
                            InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            assert inputManager != null;
                            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(VerifyMobile.this.getCurrentFocus())
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                            String code = getCodeEntered();
                            if (!TextUtils.isEmpty(code) && code.length() == 6) {
                                verifyPhoneNumberWithCode(mVerificationId, code);
                            }


                        } else if (one.length() == 0) {
                            mEt5.requestFocus();
                        }
                        break;
                }
            }
        });
    }

    public void setupTextWatcher() {
        addTextWatcher(mEt1);
        addTextWatcher(mEt2);
        addTextWatcher(mEt3);
        addTextWatcher(mEt4);
        addTextWatcher(mEt5);
        addTextWatcher(mEt6);
    }

    private void setCodeGrabbed(String _6dig_code) {
        mEt1.setText(_6dig_code.substring(0, 1));
        mEt2.setText(_6dig_code.substring(1, 2));
        mEt3.setText(_6dig_code.substring(2, 3));
        mEt4.setText(_6dig_code.substring(3, 4));
        mEt5.setText(_6dig_code.substring(4, 5));
        mEt6.setText(_6dig_code.substring(5, 6));
        disableCodeArea();
    }

    private void disableCodeArea() {
        mEt1.setEnabled(false);
        mEt2.setEnabled(false);
        mEt3.setEnabled(false);
        mEt4.setEnabled(false);
        mEt5.setEnabled(false);
        mEt6.setEnabled(false);
    }

    private String getCodeEntered() {
        String et1 = mEt1.getText().toString();
        String et2 = mEt2.getText().toString();
        String et3 = mEt3.getText().toString();
        String et4 = mEt4.getText().toString();
        String et5 = mEt5.getText().toString();
        String et6 = mEt6.getText().toString();
        if (!TextUtils.isEmpty(et1) && !TextUtils.isEmpty(et2) && !TextUtils.isEmpty(et3) &&
                !TextUtils.isEmpty(et4) && !TextUtils.isEmpty(et5) && !TextUtils.isEmpty(et6)) {
            return String.format("%s%s%s%s%s%s", et1, et2, et3, et4, et5, et6);
        } else {
            Utils.shortToast(this, getString(R.string.wrong_v_code));
            return "";
        }
    }


    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }
}
