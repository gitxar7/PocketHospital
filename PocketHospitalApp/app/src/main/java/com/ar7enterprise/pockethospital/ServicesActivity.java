package com.ar7enterprise.pockethospital;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.model.ServiceTileAdapter;
import com.ar7enterprise.pockethospital.model.ServiceTileItem;

import java.util.ArrayList;

public class ServicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.servicesMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            startActivity(new Intent(ServicesActivity.this, GuestActivity.class));
            finish();
        }

        findViewById(R.id.svProfileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServicesActivity.this, MainActivity.class));
            }
        });

        RecyclerView recyclerView = findViewById(R.id.serviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<ServiceTileItem> tileItems = new ArrayList<>();
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_1,"Click here for more details",R.drawable.settings));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_2,"Click here to make a Call",R.drawable.settings));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_3,"Click here for more details",R.drawable.settings));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_4,"Click here for more details",R.drawable.settings));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_5,"Click here for more details",R.drawable.settings));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_7,"Click here for more details",R.drawable.settings));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_6,"Click here for more details",R.drawable.settings));

        recyclerView.setAdapter(new ServiceTileAdapter(tileItems));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted! Try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

