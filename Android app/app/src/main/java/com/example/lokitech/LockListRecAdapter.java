package com.example.lokitech;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LockListRecAdapter extends RecyclerView.Adapter<LockListRecAdapter.Holder> {

    private ArrayList<Lock> locks;
    private Context context;

    public LockListRecAdapter(ArrayList<Lock> locks, Context context) {
        this.locks = locks;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lock_list_item_layout,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Lock lock = locks.get(position);

        String active = "Not active";
        int color = R.color.red;
        if (lock.isActive()) {
            active = "Active";
            color = R.color.green;
        }

        holder.lockName.setText(lock.getName());
        holder.lockActive.setText(active);
        holder.lockActive.setTextColor(ContextCompat.getColor(context ,color));

        holder.lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LockActivity.class);
                intent.putExtra("lock_name", lock.getName());
                intent.putExtra("lock_id", lock.getLockId());
                intent.putExtra("lock_active", lock.isActive());

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return locks.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        TextView lockName, lockActive;
        Button lockButton;

        public Holder(@NonNull View itemView) {
            super(itemView);

            lockName = itemView.findViewById(R.id.lock_list_item_name);
            lockActive = itemView.findViewById(R.id.lock_list_item_active);
            lockButton = itemView.findViewById(R.id.lock_list_item_but);

        }
    }
}
