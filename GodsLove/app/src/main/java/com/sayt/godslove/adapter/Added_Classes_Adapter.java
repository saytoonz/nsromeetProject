package com.sayt.godslove.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sayt.godslove.R;
import com.sayt.godslove.models.Added_Classes_Model;
import com.sayt.godslove.recording.RecorderActivity;
import com.sayt.godslove.utils.Constants;
import com.sayt.godslove.utils.Utils;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Added_Classes_Adapter  extends RecyclerView.Adapter<Added_Classes_Adapter.ViewHolder> {


    private final Context context;
    private final List<Added_Classes_Model> list;

    public Added_Classes_Adapter(Context context, List<Added_Classes_Model> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_join_buttons, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Added_Classes_Model item = list.get(position);
        item.setItemIndex(position);

        holder.class_info.setText(String.format("Student: %s\nChild AppID: %s\nClass: %s", item.getStudent(),
                item.getTid(), item.get_class()));



        if (TextUtils.isEmpty(item.getConferenceId())){
            holder.joinMeetingBtn.setBackgroundResource(R.drawable.bg_red_round_conner_bg);
            holder.joinMeetingBtn.setTextColor(context.getResources().getColor(R.color.white));
            holder.joinMeetingBtn.setOnClickListener(v -> Utils.longToast(context, "Your account needs to be activated..."));
        }else{
            // Initialize default options for Jitsi Meet conferences.
            URL serverURL;
            try {
                serverURL = new URL(Constants.getServerUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new RuntimeException("Invalid server URL!");
            }

            JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
            userInfo.setDisplayName(item.getStudent());

            JitsiMeetConferenceOptions defaultOptions
                    = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setUserInfo(userInfo)
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);

            holder.joinMeetingBtn.setBackgroundResource(R.drawable.bg_round_conner_bg);
            holder.joinMeetingBtn.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.joinMeetingBtn.setOnClickListener(v -> {
//                JitsiMeetConferenceOptions options
//                        = new JitsiMeetConferenceOptions.Builder()
//                        .setRoom(item.getConferenceId())
//                        .setSubject(item.get_class())
//                        .setWelcomePageEnabled(false)
//                        .build();
//                JitsiMeetActivity.launch(context, options);

                Intent intent = new Intent(context, RecorderActivity.class);
                intent.putExtra("what", "call");
                intent.putExtra("id", item.getConferenceId());
                intent.putExtra("subject", item.get_class());
                context.startActivity(intent);
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView class_info;
        private Button joinMeetingBtn;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            class_info = itemView.findViewById(R.id.class_info);
            joinMeetingBtn = itemView.findViewById(R.id.joinMeetingBtn);
        }
    }
}
