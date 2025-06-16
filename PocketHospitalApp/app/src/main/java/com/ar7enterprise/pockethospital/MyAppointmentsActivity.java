package com.ar7enterprise.pockethospital;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.model.DetailsTileAdapter;
import com.ar7enterprise.pockethospital.model.DetailsTileItem;
import com.ar7enterprise.pockethospital.model.FireVocabulary;
import com.ar7enterprise.pockethospital.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_appointments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myAppointmentsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            startActivity(new Intent(MyAppointmentsActivity.this, GuestActivity.class));
            finish();
        } else {

            Gson gson = new Gson();
            User userObj = gson.fromJson(user, User.class);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            findViewById(R.id.maProfileImage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MyAppointmentsActivity.this, MainActivity.class));
                }
            });

            findViewById(R.id.maNewAppointment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MyAppointmentsActivity.this, AppointmentsActivity.class));
                }
            });

            firestore.collection("appointment")
                    .where(
                            Filter.and(
                                    Filter.equalTo("mobile", userObj.getMobile()),
                                    Filter.equalTo("status", FireVocabulary.APPOINTMENT_STATUS_1)
                            )

                    )
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                            RecyclerView recyclerView = findViewById(R.id.maAppointmentsList);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MyAppointmentsActivity.this));

                            ArrayList<DetailsTileItem> detailsTileItems = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                detailsTileItems.add(new DetailsTileItem("#" + documentSnapshot.getId().toString(),
                                        documentSnapshot.get("department").toString() + "/ " + documentSnapshot.get("sub-department").toString(),
                                        documentSnapshot.get("date").toString(), documentSnapshot.getId()));
                            }

                            recyclerView.setAdapter(new DetailsTileAdapter(detailsTileItems, true));
                        }
                    });


//            detailsTileItems.add(new DetailsTileItem("#125xsz2", "Department/ Sub-Department", "September 02, 2010 5:20PM", true));

            firestore.collection("history")
                    .where(
                            Filter.and(
                                    Filter.equalTo("mobile", userObj.getMobile()),
                                    Filter.or(
                                            Filter.equalTo("name", FireVocabulary.HISTORY_APP),
                                            Filter.equalTo("name", FireVocabulary.HISTORY_APP_2)
                                    )
                            )

                    )
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                            RecyclerView recyclerView2 = findViewById(R.id.maAppointmentsHistoryList);
                            recyclerView2.setLayoutManager(new LinearLayoutManager(MyAppointmentsActivity.this));

                            ArrayList<DetailsTileItem> historyTileItems = new ArrayList<>();

                            for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                historyTileItems.add(new DetailsTileItem(documentSnapshot.get("name").toString(),
                                        documentSnapshot.get("message").toString(),
                                        documentSnapshot.get("date").toString(), null));
                            }

                            recyclerView2.setAdapter(new DetailsTileAdapter(historyTileItems, false));
                        }
                    });


//            historyTileItems.add(new DetailsTileItem("#125xsz2 : Attended", "Department/ Sub-Department", "September 02, 2010 5:20PM", true));
        }

    }
}