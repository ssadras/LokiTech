package com.example.lokitech;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LogListRecAdapter extends RecyclerView.Adapter<LogListRecAdapter.Holder> {

    private ArrayList<Log> logs;
    private Context context;

    public LogListRecAdapter(ArrayList<Log> logs, Context context) {
        this.logs = logs;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.log_list_item_layout, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Log log = logs.get(position);

        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateFormat dateFormat = new SimpleDateFormat(pattern);

        int color = R.color.white;
        if (log.getStatus().equals("Unsuccessful attempt") || log.getStatus().equals("Error")) {
            color = R.color.red;
        } else if (log.getStatus().equals("First config") || log.getStatus().equals("Owner change") || log.getStatus().equals("Wi-Fi change")) {
            color = R.color.yellow;
        }

        holder.logStatus.setText(log.getStatus());
        holder.logStatus.setTextColor(ContextCompat.getColor(context ,color));
        holder.logDate.setText(dateFormat.format(log.getCreatedDate()));

    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView logStatus, logDate;

        public Holder(@NonNull View itemView) {
            super(itemView);

            logStatus = itemView.findViewById(R.id.log_list_item_status);
            logDate = itemView.findViewById(R.id.log_list_item_date);


        }
    }
}
