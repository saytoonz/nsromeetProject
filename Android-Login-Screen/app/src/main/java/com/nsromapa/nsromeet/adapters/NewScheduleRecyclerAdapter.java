package com.nsromapa.nsromeet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.models.NewScheduleRecyclerModel;

import java.util.List;

import static com.nsromapa.nsromeet.activities.ScheduleActivity.addToSelectedList;
import static com.nsromapa.nsromeet.activities.ScheduleActivity.removeFromSelectedList;

public class NewScheduleRecyclerAdapter extends RecyclerView.Adapter<NewScheduleRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<NewScheduleRecyclerModel> modelList;

    public NewScheduleRecyclerAdapter(Context context, List<NewScheduleRecyclerModel> modelList) {
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
        NewScheduleRecyclerModel item = modelList.get(position);
        item.setIndex(position);

        if (item.isSelected()){
            holder.checkedIV.setImageResource(R.drawable.icon_checked_16px);
            holder.checkedIV.setVisibility(View.VISIBLE);
        }else {
            holder.checkedIV.setVisibility(View.GONE);
        }
        holder.main_text.setText(item.getName());
        holder.itemView.setOnClickListener(v -> {
            if (item.isSelected()){
                deselectItem(item);
            }else{
                selectItem(item);
            }
        });
    }

    private void selectItem(NewScheduleRecyclerModel item) {
        addToSelectedList(item);
        item.setSelected(true);
        notifyDataSetChanged();
    }

    private void deselectItem(NewScheduleRecyclerModel item) {
        removeFromSelectedList(item);
        item.setSelected(false);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView main_text;
        ImageView checkedIV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            main_text = itemView.findViewById(R.id.main_text);
            checkedIV = itemView.findViewById(R.id.delete_or_report);
        }
    }
}
