package com.ar7enterprise.adminpanel.model;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.ui.AppointmentsFragment;
import com.ar7enterprise.adminpanel.ui.ReportsFragment;
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
import java.util.Map;

public class ReportTileAdapter extends RecyclerView.Adapter<ReportTileAdapter.ViewHolder> {

    private ArrayList<PharmacyTileItem> tileItems = new ArrayList<>();
    private Context context;

    public ReportTileAdapter(ArrayList<PharmacyTileItem> tileItems, Context context) {
        this.tileItems = tileItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.reports_tile, parent, false);
        return new ReportTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PharmacyTileItem item = tileItems.get(position);
        holder.name.setText(item.getName());
        holder.date.setText(item.getDate());
        String id = item.getId();

//        holder.view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "", Toast.LENGTH_SHORT).show();
//            }
//        });

        holder.add_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                firestore.collection("requested-report").document(id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String name = document.getString("name");
                                        String mobile = document.getString("mobile");
                                        String date = document.getString("date");
                                        String image = document.getString("image");

                                        byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                                        LayoutInflater inflater = LayoutInflater.from(v.getContext());
                                        View reportView = inflater.inflate(R.layout.report_dialog, null, false);

                                        ImageView cancel = reportView.findViewById(R.id.rdClose);
                                        Button send = reportView.findViewById(R.id.rdSend);
                                        EditText result = reportView.findViewById(R.id.rdResult);
                                        TextView mobileField = reportView.findViewById(R.id.rdMobile);
                                        TextView nameField = reportView.findViewById(R.id.rdName);
                                        TextView dateField = reportView.findViewById(R.id.rdDate);
                                        ImageView imageView = reportView.findViewById(R.id.rdImage);

                                        mobileField.setText(mobile);
                                        nameField.setText(name);
                                        dateField.setText(date);
                                        imageView.setImageBitmap(bitmap);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                        builder.setView(reportView);
                                        AlertDialog dialog = builder.create();
                                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                        dialog.show();

                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });

                                        send.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String resultTxt = result.getText().toString();
                                                setResult(resultTxt, id, v);
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            }
                        });

            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult("Rejected", id, v);
            }
        });

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

    private void setResult(String result, String id, View v) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("status", result);
        reportMap.put("image", "null");

        firestore.collection("requested-report").document(id).update(reportMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firestore.collection("requested-report").document(id).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String name = document.getString("name");
                                                String mobile = document.getString("mobile");
                                                Map<String, Object> reportMap = new HashMap<>();
                                                reportMap.put("result", result);
                                                reportMap.put("name", name);
                                                reportMap.put("mobile", mobile);
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                String date = sdf.format(new Date());
                                                reportMap.put("date", date);

                                                firestore.collection("report").add(reportMap)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Toast.makeText(v.getContext(), "Report Result Added", Toast.LENGTH_SHORT).show();
                                                                ((MainActivity) context).getSupportFragmentManager()
                                                                        .beginTransaction()
                                                                        .setReorderingAllowed(true)
                                                                        .replace(R.id.frameLayout1, ReportsFragment.class, null)
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
                        Toast.makeText(v.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView date;
        Button view;
        Button add_result;
        Button reject;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.repName);
            date = itemView.findViewById(R.id.repDate);
            view = itemView.findViewById(R.id.repView);
            add_result = itemView.findViewById(R.id.repApprove);
            reject = itemView.findViewById(R.id.repCancel);
            container = itemView.findViewById(R.id.repTileContainer);
        }
    }
}
