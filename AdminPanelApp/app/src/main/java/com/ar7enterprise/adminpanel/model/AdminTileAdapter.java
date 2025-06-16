package com.ar7enterprise.adminpanel.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.ui.AppointmentsFragment;
import com.ar7enterprise.adminpanel.ui.DashboardFragment;
import com.ar7enterprise.adminpanel.ui.PharmacyFragment;
import com.ar7enterprise.adminpanel.ui.UsersFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class AdminTileAdapter extends RecyclerView.Adapter<AdminTileAdapter.ViewHolder> {

    private ArrayList<AdminTileItem> items = new ArrayList<>();

    private Context context;
    public static final String TILE_1 = "Total User: ";
    public static final String TILE_2 = "Appointments: ";
    public static final String TILE_3 = "Pharmacy Requests: ";


    public AdminTileAdapter(ArrayList<AdminTileItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public AdminTileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.admin_tile, parent, false);
        return new AdminTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTileAdapter.ViewHolder holder, int position) {
        AdminTileItem tile = items.get(position);
        holder.name.setText(tile.getName());
        holder.description.setText(tile.getDescription());
        holder.icon.setImageResource(tile.getIcon());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                switch (tile.getName()) {
//                    case TILE_1:
//
//                        break;
//                    case TILE_2:
//
//                        break;
//                    case TILE_3:
//
//                        break;
//
//                }
                DrawerLayout drawerLayout = ((MainActivity) context).findViewById(R.id.main);
                NavigationView navigationView = ((MainActivity) context).findViewById(R.id.navView1);
                TextView navTitle = ((MainActivity) context).findViewById(R.id.navMenuTitle);
                ImageView navHome = ((MainActivity) context).findViewById(R.id.navHome);
                int iconNumber = tile.getIcon();

                if (iconNumber == R.drawable.people) {
                    navigationView.setCheckedItem(R.id.nav_users);
                    ((MainActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.frameLayout1, UsersFragment.class, null)
                            .commit();
                    navTitle.setText(R.string.nmn1);
                } else if (iconNumber == R.drawable.tear_off_calendar) {
                    navigationView.setCheckedItem(R.id.nav_appointments);
                    ((MainActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.frameLayout1, AppointmentsFragment.class, null)
                            .commit();
                    navTitle.setText(R.string.nmn2);
                } else if (iconNumber == R.drawable.pharmacy_shop) {
                    navigationView.setCheckedItem(R.id.nav_pharmacy);
                    ((MainActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.frameLayout1, PharmacyFragment.class, null)
                            .commit();
                    navTitle.setText(R.string.nmn3);
                }

                navHome.setVisibility(View.VISIBLE);
                navHome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) context).getSupportFragmentManager()
                                .beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.frameLayout1, DashboardFragment.class, null)
                                .commit();
                        navTitle.setText(R.string.app_name_full);
                        navHome.setVisibility(View.GONE);
                        Menu menu = navigationView.getMenu();
                        for (int i = 0; i < menu.size(); i++) {
                            MenuItem menuItem = menu.getItem(i);
                            menuItem.setChecked(false);
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView description;
        ImageView icon;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.adminTileName);
            description = itemView.findViewById(R.id.adminTileDescription);
            icon = itemView.findViewById(R.id.adminTileIcon);
            container = itemView.findViewById(R.id.adminTileContainer);
        }
    }
}
