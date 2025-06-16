package com.ar7enterprise.pockethospital.model;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.AboutUsActivity;
import com.ar7enterprise.pockethospital.AppointmentsActivity;
import com.ar7enterprise.pockethospital.DepartmentsActivity;
import com.ar7enterprise.pockethospital.EPharmacyActivity;
import com.ar7enterprise.pockethospital.HistoryActivity;
import com.ar7enterprise.pockethospital.MyStepsActivity;
import com.ar7enterprise.pockethospital.R;
import com.ar7enterprise.pockethospital.ReportsActivity;
import com.ar7enterprise.pockethospital.ServicesActivity;
import com.ar7enterprise.pockethospital.SignInActivity;

import java.util.ArrayList;

public class ServiceTileAdapter extends RecyclerView.Adapter<ServiceTileAdapter.ViewHolder> {

    ArrayList<ServiceTileItem> services = new ArrayList<>();

    public static final String TILE_1 = "Make an Appointment";
    public static final String TILE_2 = "Request Ambulance";
    public static final String TILE_3 = "Locate a Department";
    public static final String TILE_4 = "E - Pharmacy";
    public static final String TILE_5 = "Track My Steps ";
    public static final String TILE_6 = "About Us";
    public static final String TILE_7 = "Change your Password";
    public static final String TILE_8 = "Sign In/ Register";


    public ServiceTileAdapter(ArrayList<ServiceTileItem> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceTileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.service_tile, parent, false);
        return new ServiceTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceTileAdapter.ViewHolder holder, int position) {
        ServiceTileItem service = services.get(position);
        holder.name.setText(service.getName());
        holder.description.setText(service.getDescription());
        holder.icon.setImageResource(service.getIcon());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (service.getName()) {
                    case TILE_1:
                        v.getContext().startActivity(new Intent(v.getContext(), AppointmentsActivity.class));
                        break;
                    case TILE_2:
                        if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:+94785068764"));
                            v.getContext().startActivity(callIntent);
                        } else {
                            ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                        }
                        break;
                    case TILE_3:
                        v.getContext().startActivity(new Intent(v.getContext(), DepartmentsActivity.class));
                        break;
                    case TILE_4:
                        v.getContext().startActivity(new Intent(v.getContext(), EPharmacyActivity.class));
                        break;
                    case TILE_5:
                        v.getContext().startActivity(new Intent(v.getContext(), MyStepsActivity.class));
                        break;
                    case TILE_6:
                        v.getContext().startActivity(new Intent(v.getContext(), AboutUsActivity.class));
                        break;
                    case TILE_7:
                        changePasswordDialog(v.getContext());
                        break;
                    case TILE_8:
                        v.getContext().startActivity(new Intent(v.getContext(), SignInActivity.class));
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    private void changePasswordDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View passwordView = inflater.inflate(R.layout.password_dialog, null, false);

        EditText password = passwordView.findViewById(R.id.pdPassword);
        EditText password2 = passwordView.findViewById(R.id.pdPassword2);
        Button save = passwordView.findViewById(R.id.pdSave);
        ImageView cancel = passwordView.findViewById(R.id.pdCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(passwordView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView description;
        ImageView icon;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.serviceTileName);
            description = itemView.findViewById(R.id.serviceTileDescription);
            icon = itemView.findViewById(R.id.serviceTileIcon);
            container = itemView.findViewById(R.id.serviceTileContainer);
        }
    }
}
