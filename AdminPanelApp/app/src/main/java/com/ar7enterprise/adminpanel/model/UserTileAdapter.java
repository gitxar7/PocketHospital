package com.ar7enterprise.adminpanel.model;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.ui.UserProfileFragment;
import com.ar7enterprise.adminpanel.ui.UsersFragment;

import java.util.ArrayList;

public class UserTileAdapter extends RecyclerView.Adapter<UserTileAdapter.ViewHolder> {

    private ArrayList<UsersTileItem> usersTileItems = new ArrayList<>();

    public UserTileAdapter(ArrayList<UsersTileItem> usersTileItems) {
        this.usersTileItems = usersTileItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.users_tile, parent, false);
        return new UserTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsersTileItem item = usersTileItems.get(position);
        holder.name.setText(item.getName());
        holder.mobile.setText(item.getMobile());

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "Deactivated", Toast.LENGTH_SHORT).show();
//                Bundle bundle = new Bundle();
//                bundle.putString("user_id", item.getId());
//                UserProfileFragment userProfileFragment = new UserProfileFragment();
//                userProfileFragment.setArguments(bundle);
//
//                ((MainActivity) v.getContext()).getSupportFragmentManager()
//                        .beginTransaction()
//                        .setReorderingAllowed(true)
//                        .replace(R.id.frameLayout1, userProfileFragment)
//                        .commit();
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("user_id", item.getId());
                UserProfileFragment userProfileFragment = new UserProfileFragment();
                userProfileFragment.setArguments(bundle);

                ((MainActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.frameLayout1, userProfileFragment)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersTileItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView mobile;
        Button button;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mobile = itemView.findViewById(R.id.usersTileMobile);
            name = itemView.findViewById(R.id.usersTileName);
            button = itemView.findViewById(R.id.usersTileAction);
            container = itemView.findViewById(R.id.usersTileContainer);
        }
    }
}
