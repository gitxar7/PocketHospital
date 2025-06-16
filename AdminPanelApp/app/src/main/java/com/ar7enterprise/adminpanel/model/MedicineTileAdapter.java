package com.ar7enterprise.adminpanel.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.ar7enterprise.adminpanel.ui.AppointmentsFragment;
import com.ar7enterprise.adminpanel.ui.MedicineStocksFragment;
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

public class MedicineTileAdapter extends RecyclerView.Adapter<MedicineTileAdapter.ViewHolder> {

    private ArrayList<MedicineTileItem> tileItems = new ArrayList<>();
    private Context context;
    String id;
    String status;

    public MedicineTileAdapter(ArrayList<MedicineTileItem> tileItems, Context context) {
        this.tileItems = tileItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.medicine_tile, parent, false);
        return new MedicineTileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineTileItem item = tileItems.get(position);
        holder.name.setText(item.getName());
        holder.text1.setText(item.getText1());
        holder.text2.setText(item.getText2());
        id = item.getId();
        status = item.getStatus();
        String updStatus;

        if (status.equals(FireVocabulary.PHARMACY_STOCK_STATUS_1)) {
            holder.button.setTextColor(context.getColor(R.color.error));
            holder.button.setText("Deactivate");
            updStatus = FireVocabulary.PHARMACY_STOCK_STATUS_2;
        } else {
            holder.button.setTextColor(context.getColor(R.color.primary));
            holder.button.setText("Activate");
            updStatus = FireVocabulary.PHARMACY_STOCK_STATUS_1;
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                HashMap<String, Object> stock = new HashMap<>();
                stock.put("status", updStatus);

                firestore.collection("pharmacy-stock").document(id).update(stock)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                ((MainActivity) context).getSupportFragmentManager()
                                        .beginTransaction()
                                        .setReorderingAllowed(true)
                                        .replace(R.id.frameLayout1, MedicineStocksFragment.class, null)
                                        .commit();
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

            name = itemView.findViewById(R.id.medTileName);
            text1 = itemView.findViewById(R.id.medTileText1);
            text2 = itemView.findViewById(R.id.medTileText2);
            button = itemView.findViewById(R.id.medTileAction);
            container = itemView.findViewById(R.id.medTileContainer);
        }
    }
}
