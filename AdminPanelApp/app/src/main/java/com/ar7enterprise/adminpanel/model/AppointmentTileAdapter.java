package com.ar7enterprise.adminpanel.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.ui.AppointmentsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.Inflater;

public class AppointmentTileAdapter extends RecyclerView.Adapter<AppointmentTileAdapter.ViewHolder> {

    private ArrayList<AppointmentTileItem> tileItems = new ArrayList<>();
    String id;
    private Context context;

    public AppointmentTileAdapter(ArrayList<AppointmentTileItem> tileItems, Context context) {
        this.tileItems = tileItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.appointments_tile, parent, false);
        return new AppointmentTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentTileItem item = tileItems.get(position);
        holder.name.setText(item.getName());
        holder.text1.setText(item.getText1());
        holder.text2.setText(item.getText2());
        id = item.getId();

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "", Toast.LENGTH_SHORT).show();
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                HashMap<String, Object> appt = new HashMap<>();
                appt.put("status", FireVocabulary.APPOINTMENT_STATUS_2);

                firestore.collection("appointment").document(id).update(appt)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(v.getContext(), "Appointment Cancelled", Toast.LENGTH_SHORT).show();

                                firestore.collection("appointment").document(id).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Log.i("PocketHospitalLog", document.get("mobile").toString());
                                                        String message = document.get("department") + "/ " + document.get("sub-department");
                                                        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("pocket_hospital", MODE_PRIVATE);
                                                        String user = sharedPreferences.getString("user", null);
                                                        Gson gson = new Gson();
                                                        User userObj = gson.fromJson(user, User.class);

                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                        String date = sdf.format(new Date());

                                                        HashMap<String, Object> history = new HashMap<>();
                                                        history.put("name", FireVocabulary.HISTORY_APP_2);
                                                        history.put("mobile", item.getName());
                                                        history.put("date", date);
                                                        history.put("message", message);

                                                        firestore.collection("history").add(history)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        ((MainActivity) context).getSupportFragmentManager()
                                                                                .beginTransaction()
                                                                                .setReorderingAllowed(true)
                                                                                .replace(R.id.frameLayout1, AppointmentsFragment.class, null)
                                                                                .commit();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(v.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("FirebaseLog", "Failure, Document Not Updated");
                            }
                        });
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tileItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView text1;
        TextView text2;
        Button button;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.apptTileName);
            text1 = itemView.findViewById(R.id.apptTileText1);
            text2 = itemView.findViewById(R.id.apptTileText2);
            button = itemView.findViewById(R.id.apptTileAction);
            container = itemView.findViewById(R.id.apptTileContainer);
        }
    }
}
