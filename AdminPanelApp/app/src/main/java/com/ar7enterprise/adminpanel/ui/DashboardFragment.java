package com.ar7enterprise.adminpanel.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.model.AdminTileAdapter;
import com.ar7enterprise.adminpanel.model.AdminTileItem;
import com.ar7enterprise.adminpanel.model.City;
import com.ar7enterprise.adminpanel.model.FireVocabulary;
import com.ar7enterprise.adminpanel.model.Gender;
import com.ar7enterprise.adminpanel.model.Status;
import com.ar7enterprise.adminpanel.model.User;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {

    private int userCount;
    private AdminTileAdapter adapter;
    private ArrayList<AdminTileItem> tileItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.dashList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Gender> genderList;
                List<City> cityList;
                List<Status> statusList;
                OkHttpClient okHttpClient = new OkHttpClient();
                Gson gson = new Gson();

                Request request2 = new Request.Builder()
                        .url("http://10.0.2.2:8080/PocketHospital/GetData")
                        .build();
                try {
                    Response response2 = okHttpClient.newCall(request2).execute();
                    String responseTxt2 = response2.body().string();
                    Log.i("PocketHospitalLog", responseTxt2);

                    SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext(), "pocket_hospital.db", null, 1);
                    SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

                    JsonObject responseObj = gson.fromJson(responseTxt2, JsonObject.class);

                    Type genderListType = new TypeToken<List<Gender>>() {
                    }.getType();
                    genderList = gson.fromJson(responseObj.get("GenderList"), genderListType);

                    Type cityListType = new TypeToken<List<City>>() {
                    }.getType();
                    cityList = gson.fromJson(responseObj.get("CityList"), cityListType);

                    Type statusListType = new TypeToken<List<Status>>() {
                    }.getType();
                    statusList = gson.fromJson(responseObj.get("StatusList"), statusListType);

                    for (Gender gender : genderList) {
                        ContentValues values = new ContentValues();
                        values.put("id", gender.getId());
                        values.put("name", gender.getName());
                        db.insertWithOnConflict("gender", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }

                    for (City city : cityList) {
                        ContentValues values = new ContentValues();
                        values.put("id", city.getId());
                        values.put("name", city.getName());
                        db.insertWithOnConflict("city", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }

                    for (Status status : statusList) {
                        ContentValues values = new ContentValues();
                        values.put("id", status.getId());
                        values.put("name", status.getName());
                        db.insertWithOnConflict("status", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }

                } catch (SocketTimeoutException e) {
                    Log.e("PocketHospitalLog", "Server timeout, using offline data", e);
                } catch (IOException e) {
                    Log.e("PocketHospitalLog", "Network error, using offline data", e);
                } catch (Exception e) {
                    Log.e("PocketHospitalLog", "Unexpected error", e);
                }

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/PocketHospital/GetUsers")
                        .build();

                List<User> userList;
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();
                    String responseTxt = response.body().string();

                    JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);

                    Type userListType = new TypeToken<List<User>>() {
                    }.getType();
                    userList = gson.fromJson(responseObj.get("UserList"), userListType);
                    userCount = userList.size();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext(), "pocket_hospital.db", null, 1);
                    SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
                    for (User user : userList) {
                        ContentValues values = new ContentValues();

                        values.put("id", user.getId());
                        values.put("name", user.getName());
                        values.put("mobile", user.getMobile());
                        values.put("password", user.getPassword());
                        values.put("birthday", dateFormat.format(user.getBirthday()));
                        values.put("registered_date", dateFormat.format(user.getRegistered_date()));
                        values.put("gender_id", user.getGender().getId());
                        values.put("city_id", user.getCity().getId());
                        values.put("status_id", user.getStatus().getId());

//                        long result = db.insert("user", null, values);
                        db.insertWithOnConflict("user", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tileItems.set(0, new AdminTileItem(AdminTileAdapter.TILE_1 + String.valueOf(userCount), "Click here for more details", R.drawable.people));
                            adapter.notifyDataSetChanged();
                        }
                    });

                } catch (SocketTimeoutException e) {
                    Log.e("PocketHospitalLog", "Server timeout, using offline data", e);
                } catch (IOException e) {
                    Log.e("PocketHospitalLog", "Network error, using offline data", e);
                } catch (Exception e) {
                    Log.e("PocketHospitalLog", "Unexpected error", e);
                }

            }
        }).start();

        firestore.collection("appointment")
                .where(
                        Filter.equalTo("status", FireVocabulary.APPOINTMENT_STATUS_1)
                )
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int count;
                        if (task.isSuccessful() && task.getResult() != null) {
                            count = task.getResult().size();
                        } else {
                            count = 0;
                        }

                        tileItems.set(1, new AdminTileItem(AdminTileAdapter.TILE_2 + count, "Click here for more details", R.drawable.tear_off_calendar));
                        adapter.notifyDataSetChanged();
                    }
                });

        firestore.collection("pharmacy-request")
                .where(
                        Filter.equalTo("status", FireVocabulary.PHARMACY_REQUEST_STATUS_1)
                )
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int count;
                        if (task.isSuccessful() && task.getResult() != null) {
                            count = task.getResult().size();
                        } else {
                            count = 0;
                        }

                        tileItems.set(2, new AdminTileItem(AdminTileAdapter.TILE_3 + count, "Click here for more details", R.drawable.pharmacy_shop));
                        adapter.notifyDataSetChanged();
                    }
                });


        tileItems.add(new AdminTileItem(AdminTileAdapter.TILE_1 + "Loading...", "Click here for more details", R.drawable.people));
        tileItems.add(new AdminTileItem(AdminTileAdapter.TILE_2 + "Loading...", "Click here for more details", R.drawable.tear_off_calendar));
        tileItems.add(new AdminTileItem(AdminTileAdapter.TILE_3 + "Loading...", "Click here for more details", R.drawable.pharmacy_shop));

        adapter = new AdminTileAdapter(tileItems, requireActivity());
        recyclerView.setAdapter(adapter);

        HashMap<String, Integer> departmentAppointments = new HashMap<>();

        firestore.collection("department").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                    if (documentSnapshotList.isEmpty()) {
                        Log.i("AdminLog", "No departments found.");
                        return;
                    }

                    AtomicInteger pendingQueries = new AtomicInteger(documentSnapshotList.size());

                    for (DocumentSnapshot departmentDoc : documentSnapshotList) {
                        String department = departmentDoc.getString("name");

                        firestore.collection("appointment")
                                .where(
                                        Filter.equalTo("department", department)
                                )
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> appointmentTask) {
                                        if (appointmentTask.isSuccessful() && appointmentTask.getResult() != null) {
                                            int count = appointmentTask.getResult().size();
                                            departmentAppointments.put(department, count);
                                        } else {
                                            departmentAppointments.put(department, 0);
                                        }
                                        if (pendingQueries.decrementAndGet() == 0) {
//                                            Log.i("AdminLog", departmentAppointments.toString());

                                            PieChart pieChart = view.findViewById(R.id.dashPieChart);
                                            pieChart.setUsePercentValues(true);
                                            pieChart.getDescription().setEnabled(false);
                                            pieChart.setExtraOffsets(5, 10, 5, 5);
                                            pieChart.setDrawHoleEnabled(true);
                                            pieChart.setHoleColor(Color.WHITE);
                                            pieChart.setTransparentCircleRadius(50);

                                            loadPieChartData(departmentAppointments, pieChart);
                                        }
                                    }
                                });
                    }
                } else {
                    Log.i("AdminLog", "Failed to fetch departments");
                }

            }
        });

        return view;
    }

    private void loadPieChartData(HashMap<String, Integer> data, PieChart pieChart) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Appointments");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}

class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");

        db.execSQL("CREATE TABLE IF NOT EXISTS gender (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS city (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS status (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "mobile TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "birthday DATE NOT NULL, " +
                "registered_date DATE NOT NULL, " +
                "gender_id INTEGER NOT NULL, " +
                "city_id INTEGER NOT NULL, " +
                "status_id INTEGER NOT NULL, " +
                "FOREIGN KEY (gender_id) REFERENCES gender(id) ON DELETE NO ACTION ON UPDATE NO ACTION, " +
                "FOREIGN KEY (city_id) REFERENCES city(id) ON DELETE NO ACTION ON UPDATE NO ACTION, " +
                "FOREIGN KEY (status_id) REFERENCES status(id) ON DELETE NO ACTION ON UPDATE NO ACTION);");

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Gender> genderList;
//                List<City> cityList;
//                List<Status> statusList;
//                OkHttpClient okHttpClient = new OkHttpClient();
//                Gson gson = new Gson();
//
//                Request request = new Request.Builder()
//                        .url("http://10.0.2.2:8080/PocketHospital/GetData")
//                        .build();
//                try {
//                    Response response = okHttpClient.newCall(request).execute();
//                    String responseTxt = response.body().string();
//                    Log.i("PocketHospitalLog", responseTxt);
//
//                    JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);
//
//                    Type genderListType = new TypeToken<List<Gender>>() {
//                    }.getType();
//                    genderList = gson.fromJson(responseObj.get("GenderList"), genderListType);
//
//                    Type cityListType = new TypeToken<List<City>>() {
//                    }.getType();
//                    cityList = gson.fromJson(responseObj.get("CityList"), cityListType);
//
//                    Type statusListType = new TypeToken<List<Status>>() {
//                    }.getType();
//                    statusList = gson.fromJson(responseObj.get("StatusList"), statusListType);
//
//                    for (Gender gender : genderList) {
//                        Log.i("gender",gender.getName());
//                        db.execSQL("INSERT OR REPLACE INTO gender (id, name) VALUES (" + gender.getId() + ", '" + gender.getName() + "');");
//                    }
//
//                    for (City city : cityList) {
//                        db.execSQL("INSERT OR REPLACE INTO city (id, name) VALUES (" + city.getId() + ", '" + city.getName() + "');");
//                    }
//
//                    for (Status status : statusList) {
//                        db.execSQL("INSERT OR REPLACE INTO status (id, name) VALUES (" + status.getId() + ", '" + status.getName() + "');");
//                    }
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}