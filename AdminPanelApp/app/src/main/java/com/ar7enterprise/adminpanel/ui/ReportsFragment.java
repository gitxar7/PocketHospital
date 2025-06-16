package com.ar7enterprise.adminpanel.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.model.AppointmentTileAdapter;
import com.ar7enterprise.adminpanel.model.AppointmentTileItem;
import com.ar7enterprise.adminpanel.model.FireVocabulary;
import com.ar7enterprise.adminpanel.model.PharmacyTileAdapter;
import com.ar7enterprise.adminpanel.model.PharmacyTileItem;
import com.ar7enterprise.adminpanel.model.ReportTileAdapter;
import com.ar7enterprise.adminpanel.model.UserTileAdapter;
import com.ar7enterprise.adminpanel.model.UsersTileItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

//        RecyclerView recyclerView = view.findViewById(R.id.repList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore.collection("requested-report")
                .where(
                        Filter.equalTo("status", FireVocabulary.REQUESTED_REPORT_STATUS_2)
                )
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                        RecyclerView recyclerView = view.findViewById(R.id.repList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        ArrayList<PharmacyTileItem> tileItems = new ArrayList<>();

                        for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                            tileItems.add(new PharmacyTileItem(documentSnapshot.getId(),
                                    documentSnapshot.get("mobile").toString(),
                                    documentSnapshot.get("date").toString(), false));
                        }

                        recyclerView.setAdapter(new ReportTileAdapter(tileItems,requireActivity()));
                    }
                });

//        AutoCompleteTextView userSpinner = view.findViewById(R.id.repSearchUserx);
//        Spinner reportSpinner = view.findViewById(R.id.repSpinner);
//        EditText result = view.findViewById(R.id.repMessage);
//        Button send = view.findViewById(R.id.repSendButton);
//        Button search = view.findViewById(R.id.repSearchButton);
//        EditText searchField = view.findViewById(R.id.repSearch);

//        String[] items = {"0775021548", "0745147894", "0788451247"};
//        String[] items2 = {"Blood Test", "NIC Copy", "X-ray"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items);
//        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items2);
//
//        userSpinner.setAdapter(adapter);
//        reportSpinner.setAdapter(adapter2);
        loadUsers(view);

//        search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String searchTxt = searchField.getText().toString();
//
//                if (searchTxt.isEmpty()) {
//                    searchField.setError("Search can not be empty");
//                } else {
//
//                }
//            }
//        });

//        send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String report = reportSpinner.getSelectedItem().toString();
//                String user = userSpinner.getText().toString();
//                String resultTxt = result.getText().toString();
//
//                if (user.isEmpty()) {
//                    userSpinner.setError("User can not be empty");
//                } else if (!Arrays.asList(items).contains(user)) {
//                    userSpinner.setError("Invalid user");
//                } else if (report.equals("Select") || report.isEmpty()) {
//                    Toast.makeText(getContext(), "Invalid Report", Toast.LENGTH_SHORT).show();
//                } else if (resultTxt.isEmpty()) {
//                    result.setError("Invalid Report Result");
//                } else {
//
//                }
//            }
//        });

        return view;
    }

    private void loadUsers(View view) {
        Button send = view.findViewById(R.id.repSendButton);
        AutoCompleteTextView userSpinner = view.findViewById(R.id.repSearchUserx);
        Spinner reportSpinner = view.findViewById(R.id.repSpinner);
        EditText result = view.findViewById(R.id.repMessage);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext(), "pocket_hospital.db", null, 1);
                SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM user", null);

                ArrayList<String> userList = new ArrayList<>();

                while (cursor.moveToNext()) {
                    String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                    userList.add(mobile);
                }

                db.close();

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        String[] items = {"0775021548", "0745147894", "0788451247"};
                        String[] items2 = {"Blood Test", "NIC Copy", "X-ray"};
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, userList);
                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, items2);

                        userSpinner.setAdapter(adapter);
                        reportSpinner.setAdapter(adapter2);

                        send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String report = reportSpinner.getSelectedItem().toString();
                                String user = userSpinner.getText().toString();
                                String resultTxt = result.getText().toString();

                                if (user.isEmpty()) {
                                    userSpinner.setError("User can not be empty");
                                } else if (!userList.contains(user)) {
                                    userSpinner.setError("Invalid user");
                                } else if (report.equals("Select") || report.isEmpty()) {
                                    Toast.makeText(getContext(), "Invalid Report", Toast.LENGTH_SHORT).show();
                                } else if (resultTxt.isEmpty()) {
                                    result.setError("Invalid Report Result");
                                } else {
                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    String date = sdf.format(new Date());

                                    Map<String, Object> reportMap = new HashMap<>();
                                    reportMap.put("result", resultTxt);
                                    reportMap.put("name", report);
                                    reportMap.put("mobile", user);
                                    reportMap.put("date", date);

                                    firestore.collection("report").add(reportMap)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(view.getContext(), "Report Added", Toast.LENGTH_SHORT).show();
                                                    requireActivity().getSupportFragmentManager()
                                                            .beginTransaction()
                                                            .setReorderingAllowed(true)
                                                            .replace(R.id.frameLayout1, ReportsFragment.class, null)
                                                            .commit();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(view.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        });

                    }
                });
            }
        }).start();
    }

}