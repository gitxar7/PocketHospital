package com.ar7enterprise.adminpanel.model;

import android.app.AlertDialog;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.ui.PharmacyFragment;
import com.ar7enterprise.adminpanel.ui.ReportsFragment;
import com.ar7enterprise.adminpanel.ui.UserProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PharmacyTileAdapter extends RecyclerView.Adapter<PharmacyTileAdapter.ViewHolder> {

    private ArrayList<PharmacyTileItem> tileItems = new ArrayList<>();
    private Context context;

    public PharmacyTileAdapter(ArrayList<PharmacyTileItem> tileItems, Context context) {
        this.tileItems = tileItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.pharmacy_tile, parent, false);
        return new PharmacyTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PharmacyTileItem item = tileItems.get(position);
        holder.name.setText(item.getName());
        holder.date.setText(item.getDate());
        String id = item.getId();
        boolean isPharmacy = item.isPharmacy();

//        holder.view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        holder.approve.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "", Toast.LENGTH_SHORT).show();
//            }
//        });

        holder.view_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                firestore.collection("pharmacy-request").document(id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()) {
                                        String name = "Pharmacy Slip";
                                        String mobile = documentSnapshot.getString("mobile");
                                        String date = documentSnapshot.getString("date");
                                        String image = documentSnapshot.getString("image");
                                        String priceTxt = documentSnapshot.getString("static-price");
                                        Double static_price = Double.parseDouble(priceTxt)>0?Double.parseDouble(priceTxt):0;
                                        ArrayList<String> items = (ArrayList<String>) documentSnapshot.get("items");

                                        byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                        LayoutInflater inflater = LayoutInflater.from(v.getContext());
                                        View medicineView = inflater.inflate(R.layout.pharmacy_dialog, null, false);

                                        ImageView cancel = medicineView.findViewById(R.id.pdClose);
                                        RecyclerView medList = medicineView.findViewById(R.id.pdList);
                                        TextView mobileField = medicineView.findViewById(R.id.pdMobile);
                                        TextView nameField = medicineView.findViewById(R.id.pdName);
                                        TextView dateField = medicineView.findViewById(R.id.pdDate);
                                        ImageView imageView = medicineView.findViewById(R.id.pdImage);
                                        Button approve = medicineView.findViewById(R.id.pdApprove);
                                        Button reject = medicineView.findViewById(R.id.pdReject);

                                        mobileField.setText(mobile);
                                        nameField.setText(name);
                                        dateField.setText(date);
                                        imageView.setImageBitmap(bitmap);

                                        medList.setLayoutManager(new LinearLayoutManager(v.getContext()));
                                        medList.setAdapter(new ItemAdapter(items));

                                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                        builder.setView(medicineView);
                                        AlertDialog dialog = builder.create();
                                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                        dialog.show();

                                        reject.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Map<String, Object> medMap = new HashMap<>();
                                                medMap.put("status", FireVocabulary.PHARMACY_REQUEST_STATUS_2);
                                                firestore.collection("pharmacy-request").document(id).update(medMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(v.getContext(), "Request Rejected", Toast.LENGTH_SHORT).show();
                                                                dialog.dismiss();

                                                                ((MainActivity) context).getSupportFragmentManager()
                                                                        .beginTransaction()
                                                                        .setReorderingAllowed(true)
                                                                        .replace(R.id.frameLayout1, isPharmacy ? PharmacyFragment.class : UserProfileFragment.class, null)
                                                                        .commit();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(v.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });

                                        approve.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                EditText priceField = medicineView.findViewById(R.id.pdPrice);
                                                String price = priceField.getText().toString();
                                                if (price.isEmpty() || (Integer.parseInt(price)) <= 0) {
                                                    priceField.setError("Invalid Price");
//                                                    Log.i("AdminLog",price);
                                                } else {
                                                    Map<String, Object> medMap = new HashMap<>();
                                                    medMap.put("status", FireVocabulary.PHARMACY_REQUEST_STATUS_3);
                                                    firestore.collection("pharmacy-request").document(id).update(medMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Map<String, Object> orderMap = new HashMap<>();
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                                    String date = sdf.format(new Date());
                                                                    orderMap.put("date", date);
                                                                    orderMap.put("delivery-status", FireVocabulary.PHARMACY_DELIVERY_STATUS_1);
                                                                    orderMap.put("mobile", mobile);
                                                                    orderMap.put("payment-status", FireVocabulary.PHARMACY_PAYMENT_STATUS_1);
                                                                    orderMap.put("price", static_price + Double.parseDouble(price));

                                                                    firestore.collection("pharmacy-order").add(orderMap)
                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentReference documentReference) {
                                                                                    Toast.makeText(v.getContext(), "Request Approved", Toast.LENGTH_SHORT).show();
                                                                                    ((MainActivity) context).getSupportFragmentManager()
                                                                                            .beginTransaction()
                                                                                            .setReorderingAllowed(true)
                                                                                            .replace(R.id.frameLayout1, isPharmacy ? PharmacyFragment.class : UserProfileFragment.class, null)
                                                                                            .commit();
                                                                                    dialog.dismiss();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(v.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
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

                                            }
                                        });

                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }
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
        TextView date;
        //        Button view;
//        Button approve;
        Button view_dialog;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.pharName);
            date = itemView.findViewById(R.id.pharDate);
//            view = itemView.findViewById(R.id.pharView);
//            approve = itemView.findViewById(R.id.pharApprove);
            view_dialog = itemView.findViewById(R.id.pharViewT);
            container = itemView.findViewById(R.id.pharTileContainer);
        }
    }
}

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final ArrayList<String> list;

    ItemAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = list.get(position);
        holder.name.setText(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.liName);
        }
    }
}
