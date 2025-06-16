package com.ar7enterprise.adminpanel;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ar7enterprise.adminpanel.model.Admin;
import com.ar7enterprise.adminpanel.model.Status;
import com.ar7enterprise.adminpanel.model.User;
import com.ar7enterprise.adminpanel.ui.AppointmentsFragment;
import com.ar7enterprise.adminpanel.ui.DashboardFragment;
import com.ar7enterprise.adminpanel.ui.PharmacyFragment;
import com.ar7enterprise.adminpanel.ui.ReportsFragment;
import com.ar7enterprise.adminpanel.ui.UsersFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String admin = sharedPreferences.getString("admin", null);
        if (admin == null) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        } else {
            NavigationView navigationView = findViewById(R.id.navView1);
            FrameLayout frameLayout = findViewById(R.id.frameLayout1);
            ConstraintLayout toolbar = findViewById(R.id.toolbar);
            DrawerLayout drawerLayout = findViewById(R.id.main);
            View headerView = navigationView.getHeaderView(0);
            TextView navTitle = findViewById(R.id.navMenuTitle);
            ImageView navHome = findViewById(R.id.navHome);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.frameLayout1, DashboardFragment.class, null)
                    .commit();
            navTitle.setText(R.string.app_name_full);
            navHome.setVisibility(View.GONE);


            findViewById(R.id.navMenuButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.open();
                }
            });

            headerView.findViewById(R.id.navCloseButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.close();
                }
            });

            headerView.findViewById(R.id.navChangePAssword).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View passwordView = inflater.inflate(R.layout.password_dialog, null, false);

                    EditText passwordField = passwordView.findViewById(R.id.pdPassword);
                    EditText passwordField2 = passwordView.findViewById(R.id.pdPassword2);
                    Button save = passwordView.findViewById(R.id.pdSave);
                    ImageView cancel = passwordView.findViewById(R.id.pdCancel);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(passwordView);
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.show();
                    drawerLayout.close();

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Gson gson = new Gson();
                            Admin adminObj = gson.fromJson(admin, Admin.class);
                            String mobile = adminObj.getMobile();
                            String password = passwordField.getText().toString();
                            String password2 = passwordField2.getText().toString();

                            if (mobile.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Invalid User", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                finish();
                            } else if (password.isEmpty()) {
                                passwordField.setError("Password can not be empty");
                            } else if (password2.isEmpty()) {
                                passwordField2.setError("Password 2 can not be empty");
                            } else if (!password2.equals(password)) {
                                passwordField2.setError("Password mismatch");
                            } else {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("mobile", mobile);
                                        jsonObject.addProperty("password", password);

                                        OkHttpClient okHttpClient = new OkHttpClient();
                                        RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
                                        Request request = new Request.Builder()
                                                .url("http://10.0.2.2:8080/PocketHospital/AdminChangePassword")
                                                .post(requestBody)
                                                .build();

                                        dialog.dismiss();

                                        try {
                                            Response response = okHttpClient.newCall(request).execute();
                                            String responseTxt = response.body().string();
                                            JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, responseObj.get("msg").getAsString(), Toast.LENGTH_LONG).show();
                                                }
                                            });

                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }).start();
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
            });

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.nav_users) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.frameLayout1, UsersFragment.class, null)
                                .commit();
                        navTitle.setText(R.string.nmn1);
                    } else if (item.getItemId() == R.id.nav_appointments) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.frameLayout1, AppointmentsFragment.class, null)
                                .commit();
                        navTitle.setText(R.string.nmn2);
                    } else if (item.getItemId() == R.id.nav_pharmacy) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.frameLayout1, PharmacyFragment.class, null)
                                .commit();
                        navTitle.setText(R.string.nmn3);
                    } else if (item.getItemId() == R.id.nav_reports) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.frameLayout1, ReportsFragment.class, null)
                                .commit();
                        navTitle.setText(R.string.nmn4);
                    }

                    if (item.getItemId() == R.id.nav_logout) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    navHome.setVisibility(View.VISIBLE);
                    navHome.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.frameLayout1, DashboardFragment.class, null)
                                    .commit();
                            navTitle.setText(R.string.app_name_full);
                            navHome.setVisibility(View.GONE);
                            item.setChecked(false);
                        }
                    });
                    drawerLayout.close();
                    return true;
                }
            });
        }
    }
}