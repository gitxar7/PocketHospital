package com.ar7enterprise.pockethospital;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.model.ServiceTileAdapter;
import com.ar7enterprise.pockethospital.model.ServiceTileItem;

import java.util.ArrayList;

public class GuestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.guestMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.gsServicesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<ServiceTileItem> tileItems = new ArrayList<>();
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_3,"Click here for more details",R.drawable.hospital_w));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_2,"Click here to make a Call",R.drawable.hospital_w));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_6,"Click here for more details",R.drawable.hospital_w));
        tileItems.add(new ServiceTileItem(ServiceTileAdapter.TILE_8,"Sign In for more features",R.drawable.user));

        recyclerView.setAdapter(new ServiceTileAdapter(tileItems));
    }

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