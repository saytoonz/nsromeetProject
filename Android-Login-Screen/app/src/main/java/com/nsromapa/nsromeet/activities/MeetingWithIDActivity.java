package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Objects;
import java.util.Random;

public class MeetingWithIDActivity extends AppCompatActivity {

    private Button btn_go;
    private EditText meeting_id;
    private TextView noticeTV;
    private String randomRoomID;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_with_i_d);

        randomRoomID = String.valueOf(System.currentTimeMillis());

        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getIntent().getStringExtra("title"));


        btn_go = findViewById(R.id.btn_go);
        meeting_id = findViewById(R.id.meeting_id);
        noticeTV = findViewById(R.id.noticeTV);

        if ((Objects.requireNonNull(getIntent().getStringExtra("title"))
                .toLowerCase()).contains("start")) {
            noticeTV.setText(getString(R.string.start_meeting_notice));

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    getNewRandName();
                    handler.postDelayed(this, 10000);
                }
            };
            handler.postDelayed(runnable, 2000);

        } else if ((Objects.requireNonNull(getIntent().getStringExtra("title"))
                .toLowerCase()).contains("join")) {
            noticeTV.setText(getString(R.string.join_meeting_notice));
        }

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


        btn_go.setOnClickListener(v -> {
            String conferenceID = meeting_id.getText().toString();
            if (TextUtils.isEmpty(conferenceID)) {
                if ((Objects.requireNonNull(getIntent().getStringExtra("title"))
                        .toLowerCase()).contains("start"))
                    conferenceID = randomRoomID;
                else {
                    Utils.shortToast(MeetingWithIDActivity.this, getString(R.string.enter_meeting_id));
                    return;
                }
            }

            enterMeeting(conferenceID);
        });

        if (getIntent() != null) {
            if (getIntent().hasExtra("deep_link")) {
                String meetId = getIntent().getStringExtra("deep_link");
                enterMeeting(meetId);
                meeting_id.setText(meetId);
                back.setOnClickListener(v -> gotoMain());
            }
        }


    }

    private void enterMeeting(String meetId) {
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(meetId)
                .setSubject("NsroMeet")
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeetActivity.launch(this, options);
    }

    private void getNewRandName() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        int randLength = random.nextInt(12);
        char tempChar;
        for (int i = 0; i < randLength; i++) {
            tempChar = (char) (random.nextInt(96) + 32);
            stringBuilder.append(tempChar);
            meeting_id.setHint(stringBuilder);
            randomRoomID = String.valueOf(stringBuilder);
        }
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        if (handler != null && runnable != null)
            handler.removeCallbacks(runnable);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (getIntent() != null) {
            if (getIntent().hasExtra("deep_link")) {
                gotoMain();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}


