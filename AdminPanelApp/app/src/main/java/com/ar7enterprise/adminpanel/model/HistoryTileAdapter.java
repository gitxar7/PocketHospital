package com.ar7enterprise.adminpanel.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.adminpanel.R;

import java.util.ArrayList;

public class HistoryTileAdapter extends RecyclerView.Adapter<HistoryTileAdapter.ViewHolder> {

    private ArrayList<HistoryTileItem> tileItems = new ArrayList<>();

    public HistoryTileAdapter(ArrayList<HistoryTileItem> tileItems) {
        this.tileItems = tileItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history, parent, false);
        return new HistoryTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryTileItem item = tileItems.get(position);
        holder.title.setText(item.getTitle());
        holder.text1.setText(item.getContent());

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return tileItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView text1;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.historyTileTitle);
            text1 = itemView.findViewById(R.id.historyTileText1);
            container = itemView.findViewById(R.id.historyTileContainer);
        }
    }
}
