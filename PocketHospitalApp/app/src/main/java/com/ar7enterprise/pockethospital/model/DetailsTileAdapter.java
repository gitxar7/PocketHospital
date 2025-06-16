package com.ar7enterprise.pockethospital.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.GuestActivity;
import com.ar7enterprise.pockethospital.MyAppointmentsActivity;
import com.ar7enterprise.pockethospital.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DetailsTileAdapter extends RecyclerView.Adapter<DetailsTileAdapter.ViewHolder> {

    ArrayList<DetailsTileItem> detailTiles = new ArrayList<>();
    boolean hasButton;
    String id;

    public DetailsTileAdapter(ArrayList<DetailsTileItem> detailTiles, boolean hasButton) {
        this.detailTiles = detailTiles;
        this.hasButton = hasButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.details_tile, parent, false);
        return new DetailsTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetailsTileItem service = detailTiles.get(position);
        holder.title.setText(service.getTitle());
        holder.text1.setText(service.getText1());
        holder.text2.setText(service.getText2());
        holder.button.setVisibility(View.GONE);
        id = service.getId();
//        holder.text1.setVisibility(View.GONE);
//        if (service.isHasExtra()){
//            holder.text1.setVisibility(View.VISIBLE);
//            holder.text1.setText(service.getText1());
//        }

        if (hasButton) {
            holder.container.setBackgroundTintList(ContextCompat.getColorStateList(holder.container.getContext(), R.color.light_primary2));
            holder.button.setVisibility(View.VISIBLE);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), "Id: " + id, Toast.LENGTH_SHORT).show();

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
                                                            Log.i("PocketHospitalLog",document.get("mobile").toString());
                                                            String message = document.get("department") + "/ " + document.get("sub-department");
                                                            SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("pocket_hospital", MODE_PRIVATE);
                                                            String user = sharedPreferences.getString("user", null);
                                                            Gson gson = new Gson();
                                                            User userObj = gson.fromJson(user, User.class);

                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                            String date = sdf.format(new Date());

                                                            HashMap<String, Object> history = new HashMap<>();
                                                            history.put("name", FireVocabulary.HISTORY_APP_2);
                                                            history.put("mobile", userObj.getMobile());
                                                            history.put("date", date);
                                                            history.put("message", message);

                                                            firestore.collection("history").add(history)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            v.getContext().startActivity(new Intent(v.getContext(), MyAppointmentsActivity.class));
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
        }
    }

    @Override
    public int getItemCount() {
        return detailTiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView text1;
        TextView text2;
        Button button;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.detailsTileTitle);
            text1 = itemView.findViewById(R.id.detailsTileText1);
            text2 = itemView.findViewById(R.id.detailsTileText2);
            button = itemView.findViewById(R.id.detailsTileButton);
            container = itemView.findViewById(R.id.detailsTileContainer);
        }
    }
}
