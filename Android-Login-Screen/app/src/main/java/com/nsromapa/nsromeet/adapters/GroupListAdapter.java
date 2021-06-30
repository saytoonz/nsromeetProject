package com.nsromapa.nsromeet.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.activities.GroupInfoActivity;
import com.nsromapa.nsromeet.models.GroupListModel;
import com.nsromapa.nsromeet.models.NewScheduleRecyclerModel;

import java.util.List;

import static com.nsromapa.nsromeet.activities.ScheduleActivity.addToSelectedList;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.removeFromSelectedList;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private Context context;
    private List<GroupListModel> modelList;

    public GroupListAdapter(Context context, List<GroupListModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_school_setup_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupListModel item = modelList.get(position);
        item.setIndex(position);

        holder.main_text.setText(item.getName());
        holder.location.setText(String.format("%s: %s", context.getString(R.string.privacy), item.getPrivacy()));
        holder.itemView.setOnClickListener(v->{
            Intent intent = new Intent(context, GroupInfoActivity.class);
            intent.putExtra("group_item", item);
            context.startActivity(intent);
        });

    }



    @Override
    public int getItemCount() {
        return modelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView main_text, location;
        ImageView checkedIV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            location  =itemView.findViewById(R.id.location);
            main_text = itemView.findViewById(R.id.main_text);
            checkedIV = itemView.findViewById(R.id.delete_or_report);
            checkedIV.setVisibility(View.GONE);
            location.setVisibility(View.VISIBLE);
            location.setGravity(Gravity.END);
            main_text.setSingleLine();
            location.setSingleLine();
            main_text.setTextSize(16f);
            location.setTextSize(10f);
        }
    }
}
