package com.nsromapa.nsromeet.adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.activities.ScheduleActivity;
import com.nsromapa.nsromeet.databases.DBHelper;
import com.nsromapa.nsromeet.models.ScheduleListModel;
import com.nsromapa.nsromeet.utils.SettingsUtils;
import com.nsromapa.nsromeet.utils.Utils;

import java.util.List;

import static com.nsromapa.nsromeet.activities.ScheduleActivity.deleteCreatedSchedule;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.endMeeting;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.hideNoSchedule;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.openMeetingNow;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.showNoSchedule;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.startMeetingNow;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private Context context;
    private List<ScheduleListModel> modelList;

    public ScheduleAdapter(Context context, List<ScheduleListModel> modelList) {
        this.context = context;
        this.modelList = modelList;
        checkList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleListModel item = modelList.get(position);
        item.setIndex(position);

        holder.title_.setText(item.getTitle());
        holder.start_time.setText(String.format(context.getString(R.string.start_time), item.getDate_string()));
        holder.conference_id.setText(String.format(context.getString(R.string.conference_id), item.getConference_id()));
        holder.duration_.setText(String.format(context.getString(R.string.duration_time),
                String.format("%s %s", item.getDuration_hours(), item.getDuration_minutes())));
        holder.init_by.setText(String.format(context.getString(R.string.meeting_init_by),
                String.format("%s (%s)", item.getCreator_name(), item.getCreator_idtype())));
        holder.status.setText(String.format(context.getString(R.string.meeting_status), item.getStatus()));
        holder.host_type.setText(String.format(context.getString(R.string.host_type), item.getHost_type()));

        Long current = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(0, 10));
        Long carrying = Long.parseLong(String.valueOf(item.getTime_stamp()).substring(0, 10));
        if (current < carrying) {
            holder.start_time.setTextColor(context.getResources().getColor(R.color.white));
            holder.start_time.setTextSize(16);
        } else {
            holder.start_time.setTextColor(context.getResources().getColor(R.color.meet_orange));
            holder.start_time.setTextSize(20);
        }

        if (!item.getCreated_by().equals(Utils.getShareprefValue_String(context, "uuid"))) {
            if (item.getStatus().equals(context.getString(R.string.ongoing))) {
                holder.start.setVisibility(View.VISIBLE);
                holder.start.setText(context.getString(R.string.join_meet));
                holder.start.setOnClickListener(v -> openMeetingNow(item));
            } else {
                holder.start.setVisibility(View.GONE);
            }
            holder.delete.setText(context.getString(R.string.remove));
            holder.delete.setOnClickListener(v -> removeSchedule(item));
        } else {

            holder.delete.setVisibility(View.VISIBLE);
            holder.start.setVisibility(View.VISIBLE);
            if (item.getStatus().equals(context.getString(R.string.ongoing))) {
                holder.start.setText(context.getString(R.string.join_meet));
                holder.start.setOnClickListener(v -> openMeetingNow(item));
                holder.delete.setText(context.getString(R.string.end_meeting));
                holder.delete.setOnClickListener(v -> {
                    Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setContentView(R.layout.alert_ok_alert);
                    TextView alertText = dialog.findViewById(R.id.alertText);
                    Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                    Button okBtn = dialog.findViewById(R.id.okBtn);

                    alertText.setText(String.format(context.getString(R.string.do_you_want_to_delete_this), "End", "\n"));
                    okBtn.setText(context.getString(R.string.yes));
                    okBtn.setOnClickListener(v1 -> {
                        dialog.dismiss();

                        endMeetingNow(item);
                    });
                    cancelBtn.setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                });
            } else {
                holder.start.setText(context.getString(R.string.start));
                holder.delete.setText(context.getString(R.string.delete));
                holder.delete.setOnClickListener(v -> {
                    Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setContentView(R.layout.alert_ok_alert);
                    TextView alertText = dialog.findViewById(R.id.alertText);
                    Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                    Button okBtn = dialog.findViewById(R.id.okBtn);

                    alertText.setText(String.format(context.getString(R.string.do_you_want_to_delete_this), "delete", "\n"));
                    okBtn.setText(context.getString(R.string.yes));
                    okBtn.setOnClickListener(v1 -> {
                        dialog.dismiss();
                        deleteSchedule(item);
                    });
                    cancelBtn.setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                });
                holder.start.setOnClickListener(v -> {
                    Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setContentView(R.layout.alert_ok_alert);
                    TextView alertText = dialog.findViewById(R.id.alertText);
                    Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                    Button okBtn = dialog.findViewById(R.id.okBtn);

                    alertText.setText(String.format(context.getString(R.string.do_you_want_to_start_meeting_now), "\n"));
                    okBtn.setText(context.getString(R.string.yes));
                    okBtn.setOnClickListener(v1 -> {
                        dialog.dismiss();
                        startMeetingNow(item);
                    });
                    cancelBtn.setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();

                });
            }
        }


        if (item.getStatus().equals(context.getString(R.string.done)) || !item.getActive().equals("yes")) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.start.setVisibility(View.GONE);
            holder.edit.setVisibility(View.GONE);
            holder.copy_invitation.setVisibility(View.GONE);
            holder.delete.setText(context.getString(R.string.remove));
            holder.delete.setOnClickListener(v -> removeSchedule(item));
        }

        holder.copy_invitation.setOnClickListener(v -> {
            String inviteText = Utils.getShareprefValue_String(context, "name") + " is inviting you to a scheduled NsroMeet meeting.\n" +
                    "\n" +
                    "Topic: " + item.getTitle() + "\n" +
                    "Time: " + item.getDate_string() + "\n" +
                    "Duration: " + item.getDuration_hours() + " " + item.getDuration_minutes() + "\n" +
                    "\n" +
                    "Join NsroMeet Meeting\n" +
                    context.getString(R.string.schedule_url) + item.getTid() + "/" + item.getConference_id() + "?pwd=null\n" +
                    "\n" +
                    "Meeting ID: " + item.getConference_id() + "\n" +
                    "Password: null\n";

            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("NsroMeet Schedule", inviteText);
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            Utils.shortToast(context, context.getString(R.string.copied));
        });




        //Setup the card flipped back
        setCardBackText(item, holder.noticeTV);

        if (SettingsUtils.getShareprefValue_Bool(context, "mute_" + item.getId())) {
            holder.mute_this_schedule.setOnClickListener(v -> unMute(item, holder.mute_this_schedule));
            holder.mute_this_schedule.setText(context.getString(R.string.unmute));
            holder.mute_this_schedule.setBackgroundResource(R.drawable.bg_new_meet_bg);
            holder.mute_this_schedule.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.mute_this_schedule.setText(context.getString(R.string.mute));
            holder.mute_this_schedule.setBackgroundResource(R.drawable.bg_sharp_curve_coners_bg);
            holder.mute_this_schedule.setOnClickListener(v -> mute(item, holder.mute_this_schedule));
            holder.mute_this_schedule.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }
    }

    private void setCardBackText(ScheduleListModel item, TextView noticeTV) {
        switch (item.getSched_type()) {
            case "FAC":
                noticeTV.setText(String.format("%s: %s", "Faculty", item.getSched_type_name()));
                break;
            case "DEP":
                noticeTV.setText(String.format("%s: %s", "Department", item.getSched_type_name()));
                break;
            case "CLA":
                noticeTV.setText(String.format("%s: %s", "Class", item.getSched_type_name()));
                break;
            case "GRO":
                noticeTV.setText(String.format("%s: %s", "Group", item.getSched_type_name()));
                break;
            case "ADDED":
                noticeTV.setText(String.format("%s: %s", "You Added", item.getTitle()));
                break;
            default:
                noticeTV.setText(String.format("%s: %s", "Unknown", item.getSched_type_name()));
                break;
        }
    }

    private void mute(ScheduleListModel item, Button button) {
        SettingsUtils.saveInSharepref_Bool(context, "mute_" + item.getId(), true);
        button.setText(context.getString(R.string.unmute));
        button.setBackgroundResource(R.drawable.bg_new_meet_bg);
        button.setTextColor(context.getResources().getColor(R.color.white));
        button.setOnClickListener(v -> unMute(item, button));
    }

    private void unMute(ScheduleListModel item, Button button) {
        SettingsUtils.saveInSharepref_Bool(context, "mute_" + item.getId(), false);
        button.setText(context.getString(R.string.mute));
        button.setTextColor(context.getResources().getColor(R.color.colorAccent));
        button.setBackgroundResource(R.drawable.bg_sharp_curve_coners_bg);
        button.setOnClickListener(v -> mute(item, button));
    }

    private void endMeetingNow(ScheduleListModel item) {
        modelList.remove(item.getIndex());
        if (modelList.size() > 1)
            notifyItemRemoved(item.getIndex());
        else
            notifyDataSetChanged();
        checkList();
        endMeeting(item);
    }

    private void removeSchedule(ScheduleListModel item) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.alert_ok_alert);
        TextView alertText = dialog.findViewById(R.id.alertText);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
        Button okBtn = dialog.findViewById(R.id.okBtn);

        alertText.setText(String.format(context.getString(R.string.do_you_want_to_delete_this), context.getString(R.string.remove), "\n"));
        okBtn.setText(context.getString(R.string.yes));
        okBtn.setOnClickListener(v1 -> {
            dialog.dismiss();
            modelList.remove(item.getIndex());
            if (modelList.size() > 1)
                notifyItemRemoved(item.getIndex());
            else
                notifyDataSetChanged();
            checkList();

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("deleted", "yes");
            String selection = "id" + " LIKE ?";
            String[] selectionArgs = {item.getId()};
            db.update("schedules", values, selection, selectionArgs);
            db.close();
        });
        cancelBtn.setOnClickListener(v1 -> dialog.dismiss());
        dialog.show();
    }


    private void deleteSchedule(ScheduleListModel item) {
        modelList.remove(item.getIndex());
        if (modelList.size() > 1)
            notifyItemRemoved(item.getIndex());
        else
            notifyDataSetChanged();
        checkList();
        deleteCreatedSchedule(item);
    }

    private void checkList() {
        if (modelList.size() < 1) {
            showNoSchedule();
        } else {
            hideNoSchedule();
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title_;
        private TextView host_type;
        private TextView init_by;
        private TextView start_time;
        private TextView duration_;
        private TextView conference_id;
        private TextView status;
        private Button start;
        private Button copy_invitation;
        private Button edit;
        private Button delete;

        private Button mute_this_schedule;
        private TextView noticeTV;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            title_ = itemView.findViewById(R.id.title_);
            init_by = itemView.findViewById(R.id.init_by);
            start_time = itemView.findViewById(R.id.start_time);
            duration_ = itemView.findViewById(R.id.duration_);
            conference_id = itemView.findViewById(R.id.conference_id);
            status = itemView.findViewById(R.id.status);
            host_type = itemView.findViewById(R.id.host_type);
            start = itemView.findViewById(R.id.start);
            copy_invitation = itemView.findViewById(R.id.copy_invitation);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            edit.setVisibility(View.GONE);


            mute_this_schedule = itemView.findViewById(R.id.mute_this_schedule);
            noticeTV = itemView.findViewById(R.id.noticeTV);
        }
    }


}
