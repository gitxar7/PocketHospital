package com.ar7enterprise.adminpanel.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ar7enterprise.adminpanel.MainActivity;
import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.SignInActivity;
import com.ar7enterprise.adminpanel.model.Admin;
import com.ar7enterprise.adminpanel.model.FireVocabulary;
import com.ar7enterprise.adminpanel.model.HistoryTileAdapter;
import com.ar7enterprise.adminpanel.model.HistoryTileItem;
import com.ar7enterprise.adminpanel.model.PharmacyTileAdapter;
import com.ar7enterprise.adminpanel.model.PharmacyTileItem;
import com.ar7enterprise.adminpanel.model.User;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserProfileFragment extends Fragment {

    private String id;
    private String name = "Loading...";
    private String mobile = "Loading...";
    private String birthdayStr = "Loading...";
    private String registeredDateStr = "Loading...";
    private int genderId = 0;
    private int cityId = 0;
    private int statusId = 0;
    private String gender = "Loading...";
    private String city = "Loading...";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        if (getArguments() != null) {
            String userId = getArguments().getString("user_id");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext(), "pocket_hospital.db", null, 1);
                    SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
                    Cursor cursor = db.rawQuery("SELECT * FROM user WHERE id = '" + userId + "'", null);

                    while (cursor.moveToNext()) {
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                        birthdayStr = cursor.getString(cursor.getColumnIndexOrThrow("birthday"));
                        registeredDateStr = cursor.getString(cursor.getColumnIndexOrThrow("registered_date"));
                        genderId = cursor.getInt(cursor.getColumnIndexOrThrow("gender_id"));
                        cityId = cursor.getInt(cursor.getColumnIndexOrThrow("city_id"));
                        statusId = cursor.getInt(cursor.getColumnIndexOrThrow("status_id"));
                    }

                    cursor.close();

//                    Log.d("City", String.valueOf(cityId));
//                    Log.d("Gender", String.valueOf(genderId));

                    if (genderId > 0) {
                        Cursor cursorGender = db.rawQuery("SELECT * FROM gender WHERE id = '" + genderId + "'", null);
                        while (cursorGender.moveToNext()) {
                            gender = cursorGender.getString(cursorGender.getColumnIndexOrThrow("name"));
                        }
                    }
                    if (cityId > 0) {
                        Cursor cursorCity = db.rawQuery("SELECT * FROM city WHERE id = '" + cityId + "'", null);
                        while (cursorCity.moveToNext()) {
                            city = cursorCity.getString(cursorCity.getColumnIndexOrThrow("name"));
                        }
                    }

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView nameField = view.findViewById(R.id.upName);
                            TextView mobileField = view.findViewById(R.id.upMobile);
                            TextView dobField = view.findViewById(R.id.upDOB);
                            TextView registerField = view.findViewById(R.id.upDate);
                            TextView action = view.findViewById(R.id.upAction);
                            Button request = view.findViewById(R.id.upDocButton);
                            Spinner reportSpinner = view.findViewById(R.id.upSpinner);
                            String spinnerItem = reportSpinner.getSelectedItem().toString();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String date = sdf.format(new Date());

                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("history")
                                    .where(
                                            Filter.equalTo("mobile", mobile)

                                    )
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                            RecyclerView recyclerView2 = view.findViewById(R.id.upHistoryList);
                                            recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
                                            ArrayList<HistoryTileItem> tileItems2 = new ArrayList<>();

                                            for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                                tileItems2.add(new HistoryTileItem(documentSnapshot.get("name").toString(),
                                                        documentSnapshot.get("message").toString()));
                                            }
                                            recyclerView2.setAdapter(new HistoryTileAdapter(tileItems2));
                                        }
                                    });

                            firestore.collection("pharmacy-request")
                                    .where(
                                            Filter.and(
                                                    Filter.equalTo("status", FireVocabulary.PHARMACY_REQUEST_STATUS_1),
                                                    Filter.equalTo("mobile", mobile)
                                            )
                                    )
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                            RecyclerView recyclerView = view.findViewById(R.id.upMedicineList);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                            ArrayList<PharmacyTileItem> tileItems = new ArrayList<>();

                                            for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                                tileItems.add(new PharmacyTileItem(documentSnapshot.getId(),
                                                        documentSnapshot.get("mobile").toString(),
                                                        documentSnapshot.get("date").toString(), false));
                                            }

                                            recyclerView.setAdapter(new PharmacyTileAdapter(tileItems,requireActivity()));
                                        }
                                    });

                            request.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Map<String, Object> requestMap = new HashMap<>();
                                    requestMap.put("image", "null");
                                    requestMap.put("name", spinnerItem);
                                    requestMap.put("mobile", mobile);
                                    requestMap.put("date", date);
                                    requestMap.put("status", FireVocabulary.REQUESTED_REPORT_STATUS_1);

                                    firestore.collection("requested-report").add(requestMap)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(view.getContext(), "Document Request Added", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(view.getContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });

                            nameField.setText(name + "(" + gender + ")");
                            mobileField.setText(mobile + "(" + city + ")");
                            dobField.setText(birthdayStr);
                            registerField.setText(registeredDateStr);

                            action.setTextColor(getResources().getColor(statusId == 1 ? R.color.error : R.color.primary));
                            action.setText(statusId == 1 ? R.string.mt4 : R.string.mt4x);

                            action.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Gson gson = new Gson();
                                            JsonObject jsonObject = new JsonObject();
                                            jsonObject.addProperty("mobile", mobile);
                                            jsonObject.addProperty("id", statusId == 1 ? 2 : 1);

                                            OkHttpClient okHttpClient = new OkHttpClient();
                                            RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
                                            Request request = new Request.Builder()
                                                    .url("http://10.0.2.2:8080/PocketHospital/DeactivateUser")
                                                    .post(requestBody)
                                                    .build();

                                            try {
                                                Response response = okHttpClient.newCall(request).execute();
                                                String responseTxt = response.body().string();
                                                JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);

                                                requireActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(), responseObj.get("msg").getAsString(), Toast.LENGTH_LONG).show();
                                                        if (responseObj.get("scc").getAsBoolean()) {
                                                            int id = responseObj.get("id").getAsInt();
                                                            action.setTextColor(getResources().getColor(id == 1 ? R.color.error : R.color.primary));
                                                            action.setText(id == 1 ? R.string.mt4 : R.string.mt4x);
                                                            statusId = id;
                                                        }
                                                    }
                                                });

                                            } catch (SocketTimeoutException e) {
                                                Toast.makeText(getContext(), "Couldn't connect to the server", Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                Log.e("PocketHospitalLog", "Network error, using offline data", e);
                                            } catch (Exception e) {
                                                Log.e("PocketHospitalLog", "Unexpected error", e);
                                            }
                                        }
                                    }).start();
                                }
                            });


                        }
                    });
                }
            }).start();
        } else {
            ((MainActivity) view.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.frameLayout1, UsersFragment.class, null)
                    .commit();
        }

        Spinner reportSpinner = view.findViewById(R.id.upSpinner);
        String[] items = {"Blood Test", "NIC Copy", "X-ray"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, items);

        reportSpinner.setAdapter(adapter);


//        RecyclerView recyclerView = view.findViewById(R.id.upMedicineList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        ArrayList<PharmacyTileItem> tileItems = new ArrayList<>();
//        tileItems.add(new PharmacyTileItem("id", "Phyllis Pointer", "June 26, 2001", true));
//
//        recyclerView.setAdapter(new PharmacyTileAdapter(tileItems, requireActivity()));

        view.findViewById(R.id.upUserManagementButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) view.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.frameLayout1, UsersFragment.class, null)
                        .commit();
            }
        });

        return view;
    }
}