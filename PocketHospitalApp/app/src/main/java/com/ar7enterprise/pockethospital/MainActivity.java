package com.ar7enterprise.pockethospital;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.model.FireVocabulary;
import com.ar7enterprise.pockethospital.model.HospitalNotifiacations;
import com.ar7enterprise.pockethospital.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private User userObj;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> docPickerLauncher;
    private ImageView profilePicture;
    private Uri selectedImageUri = null;
    private ImageView dialogPreviewImage = null;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.servicesMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            startActivity(new Intent(MainActivity.this, GuestActivity.class));
            finish();
        } else {
//            HospitalNotifiacations.makeNotfications(this, DepartmentsActivity.class,"Hello","Testing");
            //profile
            TextView nameField = findViewById(R.id.dashProfileName);
            TextView mobileField = findViewById(R.id.dashProfileMobile);
            TextView genderField = findViewById(R.id.dashProfileGender);
            TextView cityField = findViewById(R.id.dashProfileCity);
            TextView ageField = findViewById(R.id.dashProfileAge);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Gson gson = new Gson();
            userObj = gson.fromJson(user, User.class);
            String birthday = dateFormat.format(userObj.getBirthday());


            nameField.setText(userObj.getName());
            mobileField.setText(userObj.getMobile());
            genderField.setText(userObj.getGender().getName());
            cityField.setText(userObj.getCity().getName());
            ageField.setText(birthday + "(" + getAge(userObj.getBirthday()) + ")");
            //profile

            //logout
            findViewById(R.id.dashProfileSignOut).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            //logout


            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }

            firestore.collection("notifications").document("E9RgTf7JRpW1Xs4YsH7o")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value != null && value.exists()) {
                                HospitalNotifiacations.makeNotfications(MainActivity.this,
                                        MainActivity.class,
                                        value.getString("title"),
                                        "message");
                            }
                        }
                    });


            //list
            firestore.collection("requested-report")
                    .where(
                            Filter.and(
                                    Filter.equalTo("mobile", userObj.getMobile()),
                                    Filter.equalTo("status", FireVocabulary.REQUESTED_REPORT_STATUS_1)
                            )
                    )
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                            RecyclerView recyclerView = findViewById(R.id.dashDocList);
                            ConstraintLayout dashPillDoc = findViewById(R.id.dash_pill2);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            ArrayList<String> requests = new ArrayList<>();
                            Spinner dashDocSpinner = findViewById(R.id.dashDocSpinner);
                            Button dashDocButton = findViewById(R.id.dashDocButton);
                            TextView dashDocCount = findViewById(R.id.dashDocCount);

                            if (!documentSnapshotList.isEmpty()) {
                                for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                    requests.add(documentSnapshot.get("name").toString());
                                }

                                DocAdapter adapter = new DocAdapter(requests);
                                recyclerView.setAdapter(adapter);

                                dashDocCount.setText(requests.size() + " Documents");

                                String[] items = requests.toArray(new String[0]);
                                ArrayAdapter<String> dashDocSpinnerAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item2, items);
                                dashDocSpinner.setAdapter(dashDocSpinnerAdapter);
                            }

                            if (requests.isEmpty()) {
                                dashDocSpinner.setVisibility(View.GONE);
                                dashDocButton.setVisibility(View.GONE);
                                dashPillDoc.setMaxHeight(110);
                            }
                        }
                    });

            firestore.collection("appointment")
                    .where(
                            Filter.and(
                                    Filter.equalTo("mobile", userObj.getMobile()),
                                    Filter.equalTo("status", FireVocabulary.APPOINTMENT_STATUS_1)
                            )
                    )
                    .limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                            TextView nextAppt = findViewById(R.id.dashNextAppointment);
                            if (!documentSnapshotList.isEmpty()) {
                                nextAppt.setText(documentSnapshotList.get(0).get("date").toString() + " " +
                                        documentSnapshotList.get(0).get("time").toString());
                            } else {
                                nextAppt.setText("none");
                            }
                        }
                    });


            RecyclerView navTileRecyclerView = findViewById(R.id.dashNavTiles);
            navTileRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

            ArrayList<Item> tiles = new ArrayList<>();
            tiles.add(new Item(R.drawable.service, NavTileAdapter.TILE_1));
            tiles.add(new Item(R.drawable.document, NavTileAdapter.TILE_2));
            tiles.add(new Item(R.drawable.timesheet, NavTileAdapter.TILE_3));
            tiles.add(new Item(R.drawable.time_machine, NavTileAdapter.TILE_4));

            NavTileAdapter navTileAdapter = new NavTileAdapter(tiles);
            navTileRecyclerView.setAdapter(navTileAdapter);

            //list

            //doc
            findViewById(R.id.dashDocButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    docPickerLauncher.launch(intent);
                }
            });

            docPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                Uri imageUri = result.getData().getData();
                                try {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                    uploadImage(selectedBitmap);
                                } catch (IOException e) {
                                    Log.i("PocketHospitalLog", "Image selection failed");
                                }
                            }
                        }
                    });
            //doc

            //image
            imagePickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                Uri imageUri = result.getData().getData();
                                selectedImageUri = imageUri;
                                if (dialogPreviewImage != null) {
                                    dialogPreviewImage.setImageURI(imageUri);
                                } else {
                                    profilePicture.setImageURI(imageUri);
                                    saveImageToInternalStorage(imageUri);
                                }
                            }
                        }
                    }
            );


            profilePicture = findViewById(R.id.dashProfilePicture);
            loadImageFromInternalStorage();

            profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadImageDialog();
                }
            });

            //image
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted! Try again.", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage(Bitmap bitmap) {
        Context context = MainActivity.this;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        String base64String = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
        Spinner dashDocSpinner = findViewById(R.id.dashDocSpinner);
        String docName = dashDocSpinner.getSelectedItem().toString();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("requested-report")
                .where(
                        Filter.and(
                                Filter.equalTo("mobile", userObj.getMobile()),
                                Filter.equalTo("status", FireVocabulary.REQUESTED_REPORT_STATUS_1),
                                Filter.equalTo("name", docName)
                        )
                ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                        DocumentSnapshot documentSnapshot = documentSnapshotList.get(0);

                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put("image", base64String);
                        imageMap.put("name", documentSnapshot.getString("name"));
                        imageMap.put("mobile", documentSnapshot.getString("mobile"));
                        imageMap.put("date", documentSnapshot.getString("date"));
                        imageMap.put("status", FireVocabulary.REQUESTED_REPORT_STATUS_2);

                        firestore.collection("requested-report").document(documentSnapshot.getId()).set(imageMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Image upload Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }


    private void uploadImageDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.image_dialog, null, false);

        dialogPreviewImage = dialogView.findViewById(R.id.idPicture);
        Button select = dialogView.findViewById(R.id.idSelelct);
        Button save = dialogView.findViewById(R.id.idSave);
        Button cancel = dialogView.findViewById(R.id.idCancel);

        selectedImageUri = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageUri != null) {
                    saveImageToInternalStorage(selectedImageUri);
                    profilePicture.setImageURI(selectedImageUri);
                    Toast.makeText(MainActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
                dialogPreviewImage = null;
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPreviewImage = null;
                dialog.dismiss();
            }
        });
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            String fileName = userObj.getMobile() + ".jpg";
            File file = new File(getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e("PocketHospitalLog", "Failed to save image: " + e.getMessage());
        }
    }

    private void loadImageFromInternalStorage() {
        String fileName = userObj.getMobile() + ".jpg";
        File file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profilePicture.setImageBitmap(bitmap);
        } else {
            profilePicture.setImageResource(R.drawable.user2);
        }
    }

    private int getAge(Date birthday) {
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(birthday);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

}

class DocAdapter extends RecyclerView.Adapter<DocAdapter.ViewHolder> {

    private final ArrayList<String> requests;

    public DocAdapter(ArrayList<String> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.rdocument_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String request = requests.get(position);
        holder.request.setText(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView request;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            request = itemView.findViewById(R.id.rDocumentText);
        }
    }
}

class NavTileAdapter extends RecyclerView.Adapter<NavTileAdapter.ViewHolder> {

    private final ArrayList<Item> items;
    static final String TILE_1 = "Services";
    static final String TILE_2 = "Reports";
    static final String TILE_3 = "Appointments";
    static final String TILE_4 = "History";

    public NavTileAdapter(ArrayList<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.nav_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.name.setText(item.getName());
        holder.icon.setImageResource(item.getIcon());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (item.getName()) {
                    case TILE_1:
                        v.getContext().startActivity(new Intent(v.getContext(), ServicesActivity.class));
                        break;
                    case TILE_2:
                        v.getContext().startActivity(new Intent(v.getContext(), ReportsActivity.class));
                        break;
                    case TILE_3:
                        v.getContext().startActivity(new Intent(v.getContext(), MyAppointmentsActivity.class));
                        break;
                    case TILE_4:
                        v.getContext().startActivity(new Intent(v.getContext(), HistoryActivity.class));
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView icon;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.navTileName);
            icon = itemView.findViewById(R.id.navTileIcon);
            container = itemView.findViewById(R.id.navTileContainer);
        }
    }

}

class Item {
    private int icon;
    private String name;

    public Item(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
