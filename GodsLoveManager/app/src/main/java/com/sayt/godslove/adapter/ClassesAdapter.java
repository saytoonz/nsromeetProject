package com.sayt.godslove.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sayt.godslove.R;
import com.sayt.godslove.models.ClassesModel;
import com.sayt.godslove.models.StudentsModel;
import com.sayt.godslove.recording.RecorderActivity;
import com.sayt.godslove.utils.Constants;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ViewHolder> {
    private Context context;
    private List<ClassesModel> classesModels;

    public ClassesAdapter(Context context, List<ClassesModel> classesModels) {
        this.context = context;
        this.classesModels = classesModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_classes, parent,  false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassesModel item = classesModels.get(position);
        item.setIndex(position);


        holder.class_name.setText(item.getName());
        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            serverURL = new URL(Constants.getServerUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }

        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName("Administrator");

        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setUserInfo(userInfo)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        holder.joinIV.setOnClickListener(v -> {
//            JitsiMeetConferenceOptions options
//                    = new JitsiMeetConferenceOptions.Builder()
//                    .setRoom(item.getConference_id())
//                    .setSubject(item.getName())
//                    .setWelcomePageEnabled(false)
//                    .build();
//            JitsiMeetActivity.launch(context, options);

            Intent intent = new Intent(context, RecorderActivity.class);
            intent.putExtra("what", "call");
            intent.putExtra("id", item.getConference_id());
            intent.putExtra("subject", item.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return classesModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView joinIV;
        TextView class_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            joinIV = itemView.findViewById(R.id.joinIV);
            class_name = itemView.findViewById(R.id.class_name);
        }
    }
}
