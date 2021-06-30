package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.utils.Constants;
import com.nsromapa.nsromeet.utils.Utils;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class SchoolCallTrendActivity extends AppCompatActivity {
    private ImageView close, back;
    private TextView toolbarText;
    private TextView joinVideoTV, noticeTV;
    private ImageView connerVideo;
    private ImageView connerStar;
    private ImageView bottomFlower;
    private CardView enterMeeting;
    private ImageButton nextLevel;
    private Button level_go;
    private String roomid;
    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_call_trend);

        close = findViewById(R.id.close);
        back = findViewById(R.id.toolbar_backIv);
        toolbarText = findViewById(R.id.toolbar_text);

        enterMeeting = findViewById(R.id.enterMeeting);
        connerVideo = findViewById(R.id.connerVideo);
        connerStar = findViewById(R.id.connerStar);
        bottomFlower = findViewById(R.id.bottomFlower);
        joinVideoTV = findViewById(R.id.joinVideoTV);
        noticeTV = findViewById(R.id.noticeTV);
        nextLevel = findViewById(R.id.nextLevel);
        level_go = findViewById(R.id.level_go);
        level = Utils.getShareprefValue_String(this, "level");

        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            serverURL = new URL(Constants.SERVER_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }

        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName(Utils.getShareprefValue_String(this, "uname"));
        try {
            userInfo.setAvatar(new URL(Constants.BASEURL+Utils.getShareprefValue_String(this, "image")));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setUserInfo(userInfo)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);




        if (!TextUtils.isEmpty(Utils.getShareprefValue_String(this, "school")) &&
                !TextUtils.isEmpty(Utils.getShareprefValue_String(this, "schoolConference")))
            setUPSchoolLevel();
        else {
            Utils.longToast(this, "Sorry, we have problem identifying your school, re-login!");
            finish();
        }

        if (TextUtils.isEmpty(level)) {
            level_go.setVisibility(View.GONE);
        } else {
            level_go.setVisibility(View.VISIBLE);
            level_go.setOnClickListener(v -> {
                JitsiMeetConferenceOptions options
                        = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(String.format("%s_%s", roomid, level))
                        .setSubject("NsroMeet")
                        .setWelcomePageEnabled(false)
                        .build();
                JitsiMeetActivity.launch(this, options);
            });
        }


    }


    private void setUPSchoolLevel() {
        roomid = Utils.getShareprefValue_String(this, "schoolConference");
        toolbarText.setText(getString(R.string.school_level_));
        back.setOnClickListener(v -> finish());
        close.setVisibility(View.GONE);

        if (TextUtils.isEmpty(Utils.getShareprefValue_String(this, "faculty")) ||
                TextUtils.isEmpty(Utils.getShareprefValue_String(this, "facultyConference"))) {
            nextLevel.setVisibility(View.GONE);
            noticeTV.setText(String.format(getString(R.string.setup_for_access_note), "the Faculty", "school", "faculty"));
        } else {
            nextLevel.setVisibility(View.VISIBLE);
            noticeTV.setText(String.format("%s %s", getString(R.string.continue_to_the_), getString(R.string.faculty_level)));
        }
        nextLevel.setOnClickListener(v -> setUPFacultyLevel());
        level_go.setText(String.format(getString(R.string.level_version_of_meet), level, getString(R.string.school_level_)));
        bottomFlower.setVisibility(View.GONE);
        noticeTV.setVisibility(View.VISIBLE);
        joinVideoTV.setText(String.format(getString(R.string.you_can_join_school_level_call), "School"));

        enterMeeting.setOnClickListener(v -> {

            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(roomid)
                    .setSubject("NsroMeet")
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeetActivity.launch(this, options);
        });
    }


    private void setUPFacultyLevel() {
        roomid = Utils.getShareprefValue_String(this, "facultyConference");
        toolbarText.setText(getString(R.string.faculty_level_));
        back.setOnClickListener(v -> setUPSchoolLevel());
        close.setVisibility(View.VISIBLE);
        close.setOnClickListener(v -> finish());


        if (TextUtils.isEmpty(Utils.getShareprefValue_String(this, "department")) ||
                TextUtils.isEmpty(Utils.getShareprefValue_String(this, "departmentConference"))) {
            nextLevel.setVisibility(View.GONE);
            noticeTV.setText(String.format(getString(R.string.setup_for_access_note), "the Department", "faculty", "departmental"));
        } else {
            nextLevel.setVisibility(View.VISIBLE);
            noticeTV.setText(String.format("%s %s", getString(R.string.continue_to_the_), getString(R.string.departmental_level_)));
        }
        nextLevel.setOnClickListener(v -> setUPDepartmentLevel());
        level_go.setText(String.format(getString(R.string.level_version_of_meet), level, getString(R.string.faculty_level_)));

        noticeTV.setVisibility(View.VISIBLE);
        bottomFlower.setVisibility(View.VISIBLE);
        connerStar.setVisibility(View.GONE);
        joinVideoTV.setText(String.format(getString(R.string.you_can_join_school_level_call), "Faculty"));

        enterMeeting.setOnClickListener(v -> {

            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(roomid)
                    .setSubject("NsroMeet")
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeetActivity.launch(this, options);
        });
    }


    private void setUPDepartmentLevel() {
        roomid = Utils.getShareprefValue_String(this, "departmentConference");
        toolbarText.setText(getString(R.string.departmental_level_));
        back.setOnClickListener(v -> setUPFacultyLevel());
        close.setVisibility(View.VISIBLE);
        close.setOnClickListener(v -> finish());


        if (TextUtils.isEmpty(Utils.getShareprefValue_String(this, "class")) ||
                TextUtils.isEmpty(Utils.getShareprefValue_String(this, "classConference"))) {
            nextLevel.setVisibility(View.GONE);
            noticeTV.setText(String.format(getString(R.string.setup_for_access_note), "the Class", "department", "class"));
        } else {
            nextLevel.setVisibility(View.VISIBLE);
            noticeTV.setText(String.format("%s %s", getString(R.string.continue_to_the_), getString(R.string.class_level_)));
        }
        nextLevel.setOnClickListener(v -> setUPClassLevel());
        level_go.setText(String.format(getString(R.string.level_version_of_meet), level, getString(R.string.departmental_level_)));

        noticeTV.setVisibility(View.VISIBLE);
        bottomFlower.setVisibility(View.GONE);
        connerStar.setVisibility(View.VISIBLE);
        joinVideoTV.setText(String.format(getString(R.string.you_can_join_school_level_call), "Departmental"));

        enterMeeting.setOnClickListener(v -> {

            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(roomid)
                    .setSubject("NsroMeet")
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeetActivity.launch(this, options);
        });
    }


    private void setUPClassLevel() {
        roomid = Utils.getShareprefValue_String(this, "classConference");
        toolbarText.setText(getString(R.string.class_level_));
        back.setOnClickListener(v -> setUPDepartmentLevel());
        close.setVisibility(View.VISIBLE);
        close.setOnClickListener(v -> finish());
        noticeTV.setVisibility(View.GONE);
        nextLevel.setVisibility(View.GONE);
        bottomFlower.setVisibility(View.VISIBLE);
        connerStar.setVisibility(View.VISIBLE);
        connerVideo.setRotation(126f);

        level_go.setText(String.format(getString(R.string.level_version_of_meet), level, getString(R.string.class_level_)));
        enterMeeting.setOnClickListener(v -> {

            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(roomid)
                    .setSubject("NsroMeet")
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeetActivity.launch(this, options);
        });
    }


}
