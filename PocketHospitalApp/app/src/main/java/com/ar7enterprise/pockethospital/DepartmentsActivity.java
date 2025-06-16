package com.ar7enterprise.pockethospital;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class DepartmentsActivity extends AppCompatActivity {

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_departments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.departmentsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CardView profileImage = findViewById(R.id.dpProfileImageContainer);
        Button serviceButton = findViewById(R.id.dpServiceButton);

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            profileImage.setVisibility(View.GONE);
            serviceButton.setVisibility(View.GONE);
        } else {
            profileImage.setVisibility(View.VISIBLE);
            serviceButton.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.dpProfileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DepartmentsActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.dpServiceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DepartmentsActivity.this, ServicesActivity.class));
            }
        });

        SupportMapFragment mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.dpMapFrame, mapFragment).commit();

        Spinner departmentSpinner = findViewById(R.id.dpDepartmentSpinner);
        Button locate = findViewById(R.id.dpLocateButton);

        final Map<String, LatLng> departmentLocations = new HashMap<>();
        departmentLocations.put("Cardiology", new LatLng(6.067946359948525, 80.22802878785227));
        departmentLocations.put("Neurology", new LatLng(6.064553694785751, 80.22880126398147));
        departmentLocations.put("Radiology", new LatLng(6.066160817277721, 80.2256851478758));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(DepartmentsActivity.this,
                R.layout.spinner_item2, departmentLocations.keySet().toArray(new String[0]));

        departmentSpinner.setAdapter(adapter);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap gMap) {
                googleMap = gMap;
            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(DepartmentsActivity.this,R.anim.animation_x);
                locate.startAnimation(animation);
                String selectedDepartment = departmentSpinner.getSelectedItem().toString();
                LatLng location = departmentLocations.get(selectedDepartment);
                if (location != null) {
                    if (googleMap != null) {
                        googleMap.getUiSettings().setCompassEnabled(true);
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        googleMap.getUiSettings().setRotateGesturesEnabled(true);
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(selectedDepartment)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_bookmark2)));

                        googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                        new CameraPosition.Builder()
                                                .target(location)
                                                .zoom(18)
                                                .build()
                                )
                        );

                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                googleMap.setMyLocationEnabled(true);
                            } else {
                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 4);
                            }
                        } else {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 5);
                        }

                    } else {
                        Toast.makeText(DepartmentsActivity.this, "Check your Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DepartmentsActivity.this, "Select a Department", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 5 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i("PocketHospitalLog", "FINE_LOCATION Permission Granted");
        } else {
            Toast.makeText(DepartmentsActivity.this, "Location Permission Required", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(DepartmentsActivity.this, "Location Permission Required", Toast.LENGTH_SHORT).show();
        }
    }
}