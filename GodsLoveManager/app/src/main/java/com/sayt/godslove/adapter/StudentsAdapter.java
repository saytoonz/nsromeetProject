package com.sayt.godslove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sayt.godslove.R;
import com.sayt.godslove.models.StudentsModel;

import java.util.List;

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.ViewHolder> {
    private Context context;
    private List<StudentsModel> studentsModels;

    public StudentsAdapter(Context context, List<StudentsModel> studentsModels) {
        this.context = context;
        this.studentsModels = studentsModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_students, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentsModel item = studentsModels.get(position);
        item.setIndex(position);

        holder.studentTV.setText(String.format("%s - %s", item.getStudent(), item.getId()));
        holder.secret_keyTV.setText(item.getSecret_key());
        holder.classTV.setText(item.get_class());
        holder.last_updateTV.setText(item.getLast_update());
        holder.monthTV.setText(item.getMonth());
    }

    @Override
    public int getItemCount() {
        return studentsModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView studentTV, secret_keyTV, classTV, monthTV, last_updateTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentTV = itemView.findViewById(R.id.studentTV);
            secret_keyTV = itemView.findViewById(R.id.secret_keyTV);
            classTV = itemView.findViewById(R.id.classTV);
            monthTV = itemView.findViewById(R.id.monthTV);
            last_updateTV = itemView.findViewById(R.id.last_updateTV);
        }
    }
}
