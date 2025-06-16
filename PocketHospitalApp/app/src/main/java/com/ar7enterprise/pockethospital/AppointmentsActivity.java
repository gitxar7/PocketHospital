package com.ar7enterprise.pockethospital;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.ar7enterprise.pockethospital.model.ServiceTileAdapter;
import com.ar7enterprise.pockethospital.model.ServiceTileItem;
import com.ar7enterprise.pockethospital.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appointmentsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            startActivity(new Intent(AppointmentsActivity.this, GuestActivity.class));
            finish();
        } else {
            findViewById(R.id.apProfileImage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AppointmentsActivity.this, MainActivity.class));
                }
            });

            findViewById(R.id.apServiceButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AppointmentsActivity.this, ServicesActivity.class));
                }
            });

            findViewById(R.id.apMyAppointments).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AppointmentsActivity.this, MyAppointmentsActivity.class));
                }
            });

            Spinner departmentSpinner = findViewById(R.id.apDepSpinner);
            Spinner subDepartmentSpinner = findViewById(R.id.apSubSpinner);
            Spinner dateSpinner = findViewById(R.id.apDateSpinner);
            Spinner timeSpinner = findViewById(R.id.apTimeSpinner);
            Button actionButton = findViewById(R.id.apMakeAppointmentButton);

            Gson gson = new Gson();
            User userObj = gson.fromJson(user, User.class);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("available-appointments")
                    .where(
                            Filter.equalTo("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1)
                    ).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                            if (!documentSnapshotList.isEmpty()) {
                                Set<String> departmentSet = new HashSet<>();
                                for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                    departmentSet.add(documentSnapshot.getString("department"));
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(AppointmentsActivity.this,
                                        R.layout.spinner_item2, new ArrayList<>(departmentSet));
//                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                departmentSpinner.setAdapter(adapter);
                            }
                        }
                    });

            departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDepartment = parent.getItemAtPosition(position).toString();

                    firestore.collection("available-appointments")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1),
                                            Filter.equalTo("department", selectedDepartment)
                                    )
                            ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                    if (!documentSnapshotList.isEmpty()) {
                                        Set<String> subDepartmentSet = new HashSet<>();
                                        for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                            subDepartmentSet.add(documentSnapshot.getString("sub_department"));
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AppointmentsActivity.this,
                                                R.layout.spinner_item2, new ArrayList<>(subDepartmentSet));
                                        subDepartmentSpinner.setAdapter(adapter);
                                    }

                                }
                            });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            subDepartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDepartment = departmentSpinner.getSelectedItem().toString();
                    String selectedSubDepartment = parent.getItemAtPosition(position).toString();

                    firestore.collection("available-appointments")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1),
                                            Filter.equalTo("department", selectedDepartment),
                                            Filter.equalTo("sub_department", selectedSubDepartment)
                                    )
                            ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                    if (!documentSnapshotList.isEmpty()) {
                                        Set<String> dateSet = new HashSet<>();
                                        for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                            dateSet.add(documentSnapshot.getString("date"));
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AppointmentsActivity.this,
                                                R.layout.spinner_item2, new ArrayList<>(dateSet));
                                        dateSpinner.setAdapter(adapter);
                                    }
                                }
                            });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDepartment = departmentSpinner.getSelectedItem().toString();
                    String selectedSubDepartment = subDepartmentSpinner.getSelectedItem().toString();
                    String selectedDate = parent.getItemAtPosition(position).toString();

                    firestore.collection("available-appointments")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1),
                                            Filter.equalTo("department", selectedDepartment),
                                            Filter.equalTo("sub_department", selectedSubDepartment),
                                            Filter.equalTo("date", selectedDate)
                                    )
                            ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                    if (!documentSnapshotList.isEmpty()) {
                                        Set<String> timeSet = new HashSet<>();
                                        for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                            timeSet.add(documentSnapshot.getString("time"));
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AppointmentsActivity.this,
                                                R.layout.spinner_item2, new ArrayList<>(timeSet));
                                        timeSpinner.setAdapter(adapter);
                                    }
                                }
                            });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firestore.collection("available-appointments")
                            .where(
                                    Filter.equalTo("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1)
                            ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                    if (!documentSnapshotList.isEmpty()) {
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

                                                        if (documentSnapshotList.size() >= 2) {
                                                            Toast.makeText(AppointmentsActivity.this, "OOPS! You can not make more than 2 Appointments", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            String selectedDepartment = departmentSpinner.getSelectedItem().toString();
                                                            String selectedSubDepartment = subDepartmentSpinner.getSelectedItem().toString();
                                                            String selectedDate = dateSpinner.getSelectedItem().toString();
                                                            String selectedTime = timeSpinner.getSelectedItem().toString();

                                                            firestore.collection("available-appointments")
                                                                    .where(
                                                                            Filter.and(
                                                                                    Filter.equalTo("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1),
                                                                                    Filter.equalTo("department", selectedDepartment),
                                                                                    Filter.equalTo("sub_department", selectedSubDepartment),
                                                                                    Filter.equalTo("date", selectedDate),
                                                                                    Filter.equalTo("time", selectedTime)
                                                                            )
                                                                    ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                                                                            if (!documentSnapshotList.isEmpty()) {
                                                                                String id = documentSnapshotList.get(0).getId();
                                                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                                                String date = sdf.format(new Date());

                                                                                HashMap<String, Object> appointment = new HashMap<>();
                                                                                appointment.put("mobile", userObj.getMobile());
                                                                                appointment.put("date", selectedDate);
                                                                                appointment.put("time", selectedTime);
                                                                                appointment.put("department", selectedDepartment);
                                                                                appointment.put("sub-department", selectedSubDepartment);
                                                                                appointment.put("registered-date", date);
                                                                                appointment.put("status", FireVocabulary.APPOINTMENT_STATUS_1);

                                                                                firestore.collection("appointment").add(appointment)
                                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                            @Override
                                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                                HashMap<String, Object> available_appointment = new HashMap<>();
                                                                                                available_appointment.put("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_2);

                                                                                                firestore.collection("available-appointments").document(id).update(available_appointment)
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void unused) {
                                                                                                                HashMap<String, Object> history = new HashMap<>();
                                                                                                                history.put("name", FireVocabulary.HISTORY_APP);
                                                                                                                history.put("mobile", userObj.getMobile());
                                                                                                                history.put("date", date);
                                                                                                                history.put("message", selectedDepartment + "/ " + selectedSubDepartment);

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
                                                                                                        })
                                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                                            @Override
                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                Log.i("FirebaseLog", "Failure, Document Not Updated");
                                                                                                            }
                                                                                                        });

                                                                                            }
                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(v.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });

                                                                            } else {
                                                                                Toast.makeText(AppointmentsActivity.this, "OOPS! Time Slot Occupied", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                    }
                                                });
                                    } else {
                                        Toast.makeText(AppointmentsActivity.this, "OOPS! Appointments Unavailable at the Moment", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

        }

    }
}