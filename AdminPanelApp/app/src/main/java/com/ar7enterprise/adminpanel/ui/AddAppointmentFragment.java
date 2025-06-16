package com.ar7enterprise.adminpanel.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.model.FireVocabulary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddAppointmentFragment extends Fragment {
    private ArrayAdapter<String> departmentAdapter, subDepartmentAdapter, timeAdapter;
    private List<String> departmentList = new ArrayList<>();
    private Map<String, List<String>> subDepartmentMap = new HashMap<>();
    private String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_appointment, container, false);
        view.findViewById(R.id.adpManagement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) view.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.frameLayout1, AppointmentsFragment.class, null)
                        .commit();
            }
        });

        Spinner departmentSpinner = view.findViewById(R.id.adpDepSpinner);
        Spinner subDepartmentSpinner = view.findViewById(R.id.adpSubDepSpinner);
        EditText dateSpinner = view.findViewById(R.id.adpDateSpinner);
        Spinner timeSpinner = view.findViewById(R.id.adpTimeSpinner);
        Button action = view.findViewById(R.id.adpAddButton);
//        List<String> departmentList = new ArrayList<>();
//        Map<String, List<String>> subDepartmentMap = new HashMap<>();
//        ArrayAdapter<String> departmentAdapter = departmentAdapter = new ArrayAdapter<>(view.getContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                departmentList);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("department").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String departmentName = document.getString("name");
                                List<String> subDepartments = (List<String>) document.get("sub-departments");

                                if (departmentName != null && subDepartments != null) {
                                    departmentList.add(departmentName);
                                    subDepartmentMap.put(departmentName, subDepartments);
                                }
                            }

                            departmentAdapter.notifyDataSetChanged();
                        }
                    }
                });

        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDepartment = departmentList.get(position);
                List<String> subDepartments = subDepartmentMap.get(selectedDepartment);

                if (subDepartments != null) {
                    subDepartmentAdapter.clear();
                    subDepartmentAdapter.addAll(subDepartments);
                    subDepartmentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateSpinner.setFocusable(false);
        dateSpinner.setFocusableInTouchMode(false);
        dateSpinner.setLongClickable(false);
        dateSpinner.setKeyListener(null);

        dateSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay);
                        dateSpinner.setText(date);
                        List<String> timeSlots = Arrays.asList("Morning", "Evening");
                        timeAdapter.clear();
                        timeAdapter.addAll(timeSlots);
                        timeAdapter.notifyDataSetChanged();
                    }
                }, year, month, day);

                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        departmentAdapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, departmentList);
        departmentSpinner.setAdapter(departmentAdapter);

        subDepartmentAdapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, new ArrayList<>());
        subDepartmentSpinner.setAdapter(subDepartmentAdapter);

        timeAdapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, new ArrayList<>());
        timeSpinner.setAdapter(timeAdapter);

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (date == null || date.isEmpty()) {
                    dateSpinner.setError("Select a Date");
                    Toast.makeText(view.getContext(), "Fields can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedDepartment = departmentSpinner.getSelectedItem().toString();
                    String selectedSubDepartment = subDepartmentSpinner.getSelectedItem().toString();
                    String selectedTime = timeSpinner.getSelectedItem().toString();

                    HashMap<String, Object> appointment = new HashMap<>();
                    appointment.put("date", date);
                    appointment.put("time", selectedTime);
                    appointment.put("department", selectedDepartment);
                    appointment.put("sub-department", selectedSubDepartment);
                    appointment.put("status", FireVocabulary.APPOINTMENT_SLOT_STATUS_1);

                    firestore.collection("available-appointments").add(appointment)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(v.getContext(), "Appointment Added", Toast.LENGTH_SHORT).show();
                                    ((MainActivity) view.getContext()).getSupportFragmentManager()
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
        });

        return view;
    }
}