package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.models.GroupListModel;
import com.nsromapa.nsromeet.models.NewScheduleRecyclerModel;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GroupInfoActivity extends AppCompatActivity {
    private GroupListModel item;

    private RecyclerView recycler;
    private TextView date_created;
    private TextView created_by;
    private TextView group_link;
    private TextView conference_id;
    private TextView privacyTV;
    private Switch privacySwitch;
    private EditText description;
    private EditText name;
    private Button revoke;

    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getString(R.string.group_info));


        if (getIntent().hasExtra("group_item")) {
            Bundle b = getIntent().getExtras();
            if (b != null) {
                item = (GroupListModel) b.getSerializable("group_item");
            } else {
                finish();
            }
        } else {
            finish();
        }


        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        privacyTV = findViewById(R.id.privacyTV);
        privacySwitch = findViewById(R.id.privacySwitch);
        conference_id = findViewById(R.id.conference_id);
        group_link = findViewById(R.id.group_link);
        revoke = findViewById(R.id.revoke);
        created_by = findViewById(R.id.created_by);
        date_created = findViewById(R.id.date_created);

        recycler = findViewById(R.id.recycler);


        name.setText(item.getName());
        description.setText(item.getDescription());
        privacyTV.setText(item.getPrivacy());
        if (item.getPrivacy().equals("open"))
            privacySwitch.setChecked(false);
        else privacySwitch.setChecked(true);
        conference_id.setText(item.getConference_id());
        conference_id.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("Choose your Option")
                    .setPositiveButton("Goto Meeting Room", (dialog, which) -> {
                        Intent intent = new Intent(this, MeetingWithIDActivity.class);
                        intent.putExtra("deep_link", item.getConference_id());
                        intent.putExtra("title", getString(R.string.join_meet));
                        startActivity(intent);
                    })

                    .setNegativeButton("Copy", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("NsroMeet", item.getConference_id());
                        assert clipboard != null;
                        clipboard.setPrimaryClip(clip);
                        Utils.shortToast(this, String.format("%s %s", "Conference ID", getString(R.string.copied)));
                    })
                    .show();
        });

        group_link.setText(String.format(getString(R.string.group_url), item.getLink()));
        group_link.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("NsroMeet GroupLink",
                    String.format(getString(R.string.group_url), item.getLink()));
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            Utils.shortToast(this, String.format("%s %s", "Group Link", getString(R.string.copied)));
        });
        revoke.setOnClickListener(v -> revokeLink(item.getTid()));
        created_by.setText(String.format("%s (%s)", item.getCreator_name(), item.getCreated_idtype()));
        date_created.setText(item.getDate_added());


        if (item.getCreated_by().equals(Utils.getShareprefValue_String(this, "uuid"))) {
            privacySwitch.setVisibility(View.GONE);
            revoke.setVisibility(View.VISIBLE);
        } else {
            if (item.getPrivacy().equals("open"))
                revoke.setVisibility(View.VISIBLE);
            else revoke.setVisibility(View.GONE);
            privacySwitch.setVisibility(View.GONE);
        }
    }

    private void revokeLink(String tid) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
            requestQueue.cancelAll(this);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.GROUP_REVOKE,
                response -> {
                    if (response != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                group_link.setText(String.format(getString(R.string.group_url), object.getString("error_msg")));
                                ShowSnackBar(getString(R.string.link_revoked));
                                progressDialog.dismiss();
                            } else {
                                ShowSnackBar(object.getString("error_msg"));
                                progressDialog.dismiss();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                            ShowSnackBar(getString(R.string.an_error));
                            progressDialog.dismiss();                        }


                    } else {

                        ShowSnackBar(getString(R.string.an_error));
                        progressDialog.dismiss();                    }
                }, error -> {
            Utils.shortToast(this, error.getMessage());
            progressDialog.dismiss();

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(GroupInfoActivity.this, "uuid"));
                post.put("tid", tid);
                return post;
            }
        };


        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void ShowSnackBar(String message){
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }
}
