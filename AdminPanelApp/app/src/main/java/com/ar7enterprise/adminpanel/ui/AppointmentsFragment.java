package com.ar7enterprise.adminpanel.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.model.AppointmentTileAdapter;
import com.ar7enterprise.adminpanel.model.AppointmentTileItem;
import com.ar7enterprise.adminpanel.model.FireVocabulary;
import com.ar7enterprise.adminpanel.model.UserTileAdapter;
import com.ar7enterprise.adminpanel.model.UsersTileItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        firestore.collection("appointment")
                .where(
                        Filter.equalTo("status", FireVocabulary.APPOINTMENT_STATUS_1)
                )
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                        RecyclerView recyclerView = view.findViewById(R.id.apptList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        ArrayList<AppointmentTileItem> tileItems = new ArrayList<>();

                        for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                            tileItems.add(new AppointmentTileItem(documentSnapshot.getId(),
                                    documentSnapshot.get("mobile").toString(),
                                    documentSnapshot.get("department").toString() + "/ " + documentSnapshot.get("sub-department").toString(),
                                    documentSnapshot.get("date").toString() + " " + documentSnapshot.get("time").toString()));
                        }

                        recyclerView.setAdapter(new AppointmentTileAdapter(tileItems, requireActivity()));
                    }
                });


        view.findViewById(R.id.apptAddButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) view.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.frameLayout1, AddAppointmentFragment.class, null)
                        .commit();
            }
        });
        return view;
    }
}