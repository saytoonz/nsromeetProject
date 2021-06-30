package com.nsromapa.nsromeet.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.models.SchoolsetupModel;
import com.nsromapa.nsromeet.utils.Utils;

import java.util.List;

import static com.nsromapa.nsromeet.activities.SchoolSetupActivity.Chosen;

public class SchoolSetUpAdapter extends RecyclerView.Adapter<SchoolSetUpAdapter.ViewHolder> {
    private Context context;
    private List<SchoolsetupModel> modelList;

    public SchoolSetUpAdapter(Context context, List<SchoolsetupModel> modelList) {
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
        SchoolsetupModel item = modelList.get(position);
        item.setItemIndex(position);

        holder.total.setVisibility(View.GONE);
        holder.location.setVisibility(View.GONE);

        if (Utils.getShareprefValue_String(context, "uuid").equals(item.getCreated_by())) {
            holder.delete_or_report.setImageResource(R.drawable.icon_remove_100px);
            holder.delete_or_report.setOnClickListener(v -> deleteItem(item));
            item.setI_added(true);
        } else {
            holder.delete_or_report.setImageResource(R.drawable.icon_complaint_100px);
            holder.delete_or_report.setOnClickListener(v -> openReportDialogue(item));
            item.setI_added(false);
        }


        if (item.getIts_what().equalsIgnoreCase(context.getString(R.string.its_school))) {
            holder.main_text.setText(item.getSchoolName());
            holder.itemView.setOnClickListener(v -> Chosen(item, String.format(
                    context.getString(R.string.confirm_your_choice), item.getSchoolName(), context.getString(R.string.school))));
            holder.location.setText(item.getLocation());
            holder.location.setVisibility(View.VISIBLE);
        } else {
            if (item.getIts_what().equalsIgnoreCase(context.getString(R.string.its_faculty))) {
                holder.main_text.setText(item.getFacultyName());
                holder.itemView.setOnClickListener(v -> Chosen(item, String.format(
                        context.getString(R.string.confirm_your_choice), item.getFacultyName(), context.getString(R.string.faculty))));
            } else {
                if (item.getIts_what().equalsIgnoreCase(context.getString(R.string.its_department))) {
                    holder.main_text.setText(item.getDepartmentName());
                    holder.itemView.setOnClickListener(v -> Chosen(item, String.format(
                            context.getString(R.string.confirm_your_choice), item.getDepartmentName(), context.getString(R.string.department))));
                } else {
                    if (item.getIts_what().equalsIgnoreCase(context.getString(R.string.its_class))) {
                        holder.main_text.setText(item.getClassName());
                        holder.itemView.setOnClickListener(v -> Chosen(item, String.format(
                                context.getString(R.string.confirm_your_choice), item.getClassName(), context.getString(R.string.classs))));

//                        holder.total.setVisibility(View.VISIBLE);
//                        holder.total.setText("100");

                    } else {
                        if (item.getIts_what().equalsIgnoreCase(context.getString(R.string.its_level))) {
                            holder.main_text.setText(item.getLevelNmae());
                            holder.itemView.setOnClickListener(v -> Chosen(item, String.format(
                                    context.getString(R.string.confirm_your_choice), item.getLevelNmae(), context.getString(R.string.level))));

                        } else {
                            String id = item.getId();
                            String name = item.getLevelNmae();
                            String description = item.getReports();
                            String requirements = item.getLocation();
                            String dateAdded = item.getDate_added();

                            holder.main_text.setText(name);
                            holder.itemView.setOnClickListener(v -> Chosen(item, String.format(
                                    context.getString(R.string.confirm_your_choice), name , context.getString(R.string.idtype))));
                            holder.location.setVisibility(View.VISIBLE);
                            holder.location.setText(String.format("%s: %s", "Requirements",requirements));
                        }
                    }
                }
            }
        }
    }

    private void deleteItem(SchoolsetupModel item) {

    }

    private void openReportDialogue(SchoolsetupModel item) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.alert_report);
        Button want_to_write = dialog.findViewById(R.id.want_to_write);
        Button send_report = dialog.findViewById(R.id.send_report);

        EditText reportET = dialog.findViewById(R.id.reportET);
        TextView appreciationTV = dialog.findViewById(R.id.appreciationTV);

        LinearLayout suggestionsLL = dialog.findViewById(R.id.suggestionsLL);
        TextView not_true = dialog.findViewById(R.id.not_true);
        TextView inappropirate = dialog.findViewById(R.id.inappropirate);
        TextView this_might_be_scam = dialog.findViewById(R.id.this_might_be_scam);


        not_true.setOnClickListener(v -> {
            String report = not_true.getText().toString();
            pushReport(report, item);

            appreciationTV.setVisibility(View.VISIBLE);
            want_to_write.setVisibility(View.GONE);
            suggestionsLL.setVisibility(View.GONE);
            reportET.setVisibility(View.GONE);
            send_report.setVisibility(View.GONE);
        });

        inappropirate.setOnClickListener(v -> {
            String report = inappropirate.getText().toString();
            pushReport(report, item);

            appreciationTV.setVisibility(View.VISIBLE);
            want_to_write.setVisibility(View.GONE);
            suggestionsLL.setVisibility(View.GONE);
            reportET.setVisibility(View.GONE);
            send_report.setVisibility(View.GONE);
        });

        this_might_be_scam.setOnClickListener(v -> {
            String report = this_might_be_scam.getText().toString();
            pushReport(report, item);

            appreciationTV.setVisibility(View.VISIBLE);
            want_to_write.setVisibility(View.GONE);
            suggestionsLL.setVisibility(View.GONE);
            reportET.setVisibility(View.GONE);
            send_report.setVisibility(View.GONE);
        });

        want_to_write.setOnClickListener(v -> {
            want_to_write.setVisibility(View.GONE);
            suggestionsLL.setVisibility(View.GONE);
            reportET.setVisibility(View.VISIBLE);
            send_report.setVisibility(View.VISIBLE);
        });

        send_report.setOnClickListener(v -> {
            String report = reportET.getText().toString();
            pushReport(report, item);

            appreciationTV.setVisibility(View.VISIBLE);
            want_to_write.setVisibility(View.GONE);
            suggestionsLL.setVisibility(View.GONE);
            reportET.setVisibility(View.GONE);
            send_report.setVisibility(View.GONE);
        });


        dialog.show();
    }

    private void pushReport(String report, SchoolsetupModel item) {
        Toast.makeText(context, report + " ===> " + item.getIts_what(), Toast.LENGTH_LONG).show();
    }


    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView main_text, location, total;
        private ImageView delete_or_report;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            main_text = itemView.findViewById(R.id.main_text);
            location = itemView.findViewById(R.id.location);
            total = itemView.findViewById(R.id.total);
            delete_or_report = itemView.findViewById(R.id.delete_or_report);
        }
    }
}
