package com.nsromapa.nsromeet.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.adapters.GroupListAdapter;
import com.nsromapa.nsromeet.databases.DBHelper;
import com.nsromapa.nsromeet.models.GroupListModel;
import com.nsromapa.nsromeet.models.ScheduleListModel;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class GroupActivity extends AppCompatActivity {
    private RelativeLayout listGroupsRL;
    private RecyclerView recycler;
    private TextView noticeTV;
    private ProgressBar progress_circular;
    private List<GroupListModel> listModels;
    private GroupListAdapter adapter;

    private ScrollView newGroupSV;
    private EditText groupNameET, groupDescriptionET;
    private RadioGroup privacy;
    private Button createGroup, cancel_action;

    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getIntent().getStringExtra("title"));


        listGroupsRL = findViewById(R.id.listGroupsRL);
        recycler = findViewById(R.id.recycler);
        noticeTV = findViewById(R.id.noticeTV);
        progress_circular = findViewById(R.id.progress_circular);

        newGroupSV = findViewById(R.id.newGroupSV);
        groupNameET = findViewById(R.id.groupNameET);
        groupDescriptionET = findViewById(R.id.groupDescriptionET);
        privacy = findViewById(R.id.privacy);
        createGroup = findViewById(R.id.createGroup);
        cancel_action = findViewById(R.id.cancel_action);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);

        if (Objects.equals(getIntent().getStringExtra("title"), getString(R.string.groups))) {
            setUpExistingGroups();
        } else {
            setUpNewGroup();
        }
    }

    private void setUpExistingGroups() {
        newGroupSV.setVisibility(View.GONE);
        listGroupsRL.setVisibility(View.VISIBLE);
        progress_circular.setVisibility(View.VISIBLE);

        listModels = getGroups();
        adapter = new GroupListAdapter(this, listModels);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(manager);
        recycler.setHasFixedSize(true);
        adapter.notifyDataSetChanged();


        progress_circular.setVisibility(View.GONE);
        if (listModels.size() < 1) {
            noticeTV.setVisibility(View.VISIBLE);
            noticeTV.setText(getString(R.string.no_result_found_));
        }
    }

    private void setUpNewGroup() {
        newGroupSV.setVisibility(View.VISIBLE);
        listGroupsRL.setVisibility(View.GONE);

        cancel_action.setOnClickListener(v -> finish());
        createGroup.setOnClickListener(v -> {
            String groupName = groupNameET.getText().toString().trim();
            String description = groupDescriptionET.getText().toString().trim();
            groupNameET.setText("");
            groupDescriptionET.setText("");
            String privacy_ = getString(R.string.open);
            if (privacy.getCheckedRadioButtonId() == R.id.closed) {
                privacy_ = getString(R.string.closed);
            }

            if (!TextUtils.isEmpty(groupName)) {
                if (!TextUtils.isEmpty(description)) {
                    createNewGroup(groupName, description, privacy_);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), String.format(getString(R.string.group_name_required), "Description")
                            , Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content), String.format(getString(R.string.group_name_required), "Name")
                        , Snackbar.LENGTH_SHORT).show();
            }

        });

    }


    private List<GroupListModel> getGroups() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"id", "forid", "db_id", "gid", "name", "description",
                "conference_id", "privacy", "link", "created_by", "creator_name",
                "created_idtype", "date_added", "active"};
        String sortOrder = "id DESC";
        Cursor cursor = db.query("groups", projection, null, null, null, null, sortOrder);

        List<GroupListModel> list = new ArrayList<>();
        while (cursor.moveToNext()) {

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

            if (forid.equals(Utils.getShareprefValue_String(this, "uuid"))) {
                list.add(new GroupListModel(String.valueOf(id), db_id, gid, name, description,
                        conference_id, privacy, link, created_by, creator_name, created_idtype,
                        date_added, active));
            }
        }
        cursor.close();
        db.close();
        return list;

    }

    private void createNewGroup(String groupName, String description, String privacy) {

        progressDialog.setOnCancelListener(dialog -> {
            if (requestQueue != null)
                requestQueue.cancelAll(this);
        });
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.CREATE_GROUP,
                response -> {
                    Log.e("TAG", "createNewGroup: " + response);

                    if (response != null) {

                        try {

                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")) {
                                JSONObject group = object.getJSONObject("group");

                                DBHelper dbHelper = new DBHelper(this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                Cursor cursor = db.rawQuery("SELECT * FROM 'groups' WHERE db_id='"
                                        + group.getString("tid") + "'", null);
                                cursor.moveToFirst();
                                if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                                    insertGroup(group);
                                } else {

                                    ContentValues values = new ContentValues();
                                    String tid = group.getString("tid");
                                    String name = group.getString("name");
                                    String _description = group.getString("description");
                                    String conference_id = group.getString("conference_id");
                                    String _privacy = group.getString("privacy");
                                    String link = group.getString("link");
                                    String active = group.getString("active");

                                    String active_ = cursor.getString(cursor.getColumnIndex("active"));
                                    String name_ = cursor.getString(cursor.getColumnIndex("name"));
                                    String description_ = cursor.getString(cursor.getColumnIndex("description"));
                                    String conference_id_ = cursor.getString(cursor.getColumnIndex("conference_id"));
                                    String privacy_ = cursor.getString(cursor.getColumnIndex("privacy"));
                                    String link_ = cursor.getString(cursor.getColumnIndex("link"));

                                    if (!active.equalsIgnoreCase(active_))
                                        values.put("active", active_);
                                    if (!name.equalsIgnoreCase(name_))
                                        values.put("name", name_);
                                    if (!_description.equalsIgnoreCase(description_))
                                        values.put("description", description_);
                                    if (!conference_id.equalsIgnoreCase(conference_id_))
                                        values.put("conference_id", conference_id_);
                                    if (!_privacy.equalsIgnoreCase(privacy_))
                                        values.put("privacy", privacy_);
                                    if (!link.equalsIgnoreCase(link_))
                                        values.put("link", link_);

                                    String selection = "db_id" + " LIKE ?";
                                    String[] selectionArgs = {tid};
                                    if (values.size() > 0)
                                        db.update("groups", values, selection, selectionArgs);
                                }
                                cursor.close();
                                db.close();

                                if (progressDialog != null) progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),
                                        getString(R.string.group_created_successfully), Snackbar.LENGTH_LONG).show();
                                setUpExistingGroups();
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
                Map<String, String> post = new HashMap<>();
                post.put("uuid", Utils.getShareprefValue_String(GroupActivity.this, "uuid"));
                post.put("name", groupName);
                post.put("description", description);
                post.put("privacy", privacy);
                return post;
            }
        };
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }



    private void insertGroup(JSONObject group) {
        try {
            String tid = group.getString("tid");
            String gid = group.getString("gid");
            String name = group.getString("name");
            String description = group.getString("description");
            String conference_id = group.getString("conference_id");
            String privacy = group.getString("privacy");
            String link = group.getString("link");
            String created_by = group.getString("created_by");
            String creator_name = group.getString("creator_name");
            String created_idtype = group.getString("created_idtype");
            String date_added = group.getString("date_added");
            String active = group.getString("active");

            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("forid", Utils.getShareprefValue_String(this, "uuid"));
            values.put("db_id", tid);
            values.put("gid", gid);
            values.put("name", name);
            values.put("description", description);
            values.put("conference_id", conference_id);
            values.put("privacy", privacy);
            values.put("link", link);
            values.put("created_by", created_by);
            values.put("creator_name", creator_name);
            values.put("created_idtype", created_idtype);
            values.put("date_added", date_added);
            values.put("active", active);
            db.insert("groups", null, values);
            db.close();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        if (requestQueue != null)
            requestQueue.cancelAll(this);
        super.onDestroy();
    }
}
