package com.ar7enterprise.pockethospital;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ComponentCaller;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ar7enterprise.pockethospital.model.FireVocabulary;
import com.ar7enterprise.pockethospital.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EPharmacyActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_epharmacy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ePharmacyMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            startActivity(new Intent(EPharmacyActivity.this, GuestActivity.class));
            finish();
        } else {
            Gson gson = new Gson();
            userObj = gson.fromJson(user, User.class);
            actionButton = findViewById(R.id.epActionButton);
            medicineSpinner = findViewById(R.id.epMedicineSpinner);
            Button view = findViewById(R.id.epViewListButton);
            Button upload = findViewById(R.id.epSelectSlipButton);
            Button add = findViewById(R.id.epAddButton);

            loadMedicines();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("pharmacy-order")
                    .where(
                            Filter.and(
                                    Filter.equalTo("mobile", userObj.getMobile()),
                                    Filter.equalTo("payment-status", FireVocabulary.PHARMACY_PAYMENT_STATUS_1)
                            )
                    ).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                            DocumentSnapshot documentSnapshot = documentSnapshotList.get(0);

                            actionState = "Proceed";
                            actionButton.setText("Proceed");
//                            String amountTxt = documentSnapshot.getString("price");
                            Double price  = documentSnapshot.getDouble("price");
//                            if (amountTxt == null) {
//                                amount = 00.00;
//                            } else {
                                amount = price;
//                            }
                        }
                    });

            findViewById(R.id.epProfileImage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(EPharmacyActivity.this, MainActivity.class));
                }
            });

            findViewById(R.id.epServiceButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(EPharmacyActivity.this, ServicesActivity.class));
                }
            });

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedMedicine = medicineSpinner.getSelectedItem().toString();
                    if (selectedMedicines.size() < 5 && !selectedMedicines.contains(selectedMedicine)) {
                        selectedMedicines.add(selectedMedicine);
                    } else {
                        Toast.makeText(EPharmacyActivity.this, "Max 5 medicines, 1 quantity allowed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String medicineList = selectedMedicines.isEmpty() ? "No medicines added" : TextUtils.join(", ", selectedMedicines);
                    new AlertDialog.Builder(EPharmacyActivity.this)
                            .setTitle("Selected Medicines")
                            .setMessage(medicineList)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });

            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (actionState) {
                        case "Next":
                            if (imageUri != null) {
                                try {
                                    Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                    saveRequestToFirestore(selectedBitmap);
                                    actionState = "Cancel";
                                    actionButton.setText("Cancel");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
//                                actionState = "Proceed";
//                                actionButton.setText("Proceed");
                                Toast.makeText(EPharmacyActivity.this, "Invalid medical slip", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "Cancel":
                            cancelRequest();
                            actionState = "Next";
                            actionButton.setText("Next");
                            break;

                        case "Proceed":
                            payment(amount);
                            break;
                    }
                }
            });
        }

    }

    public void payment(Double amount) {
        String id = String.valueOf(Math.ceil(Math.random() * 10));
        InitRequest req = new InitRequest();
        req.setMerchantId("1221775");
        req.setCurrency("LKR");
        req.setAmount(amount);
        req.setOrderId(id);
        req.setItemsDescription("E-Pharmacy");
        req.setCustom1("Medical Items");
        req.setCustom2("");
        req.getCustomer().setFirstName(userObj.getName());
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail("");
        req.getCustomer().setPhone(userObj.getMobile());
        req.getCustomer().getAddress().setAddress("");
        req.getCustomer().getAddress().setCity(userObj.getCity().getName());
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        req.getItems().add(new Item(id, "E-Pharmacy", 1, amount));

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null) {
                    if (response.isSuccess()) {
                        msg = "Activity result:" + response.getData().toString();
                        //history
                    } else {
                        msg = "Result:" + response.toString();
                    }
                } else {
                    msg = "Result: no response";
                    Log.i("PocketHospitalLog", msg);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
//                if (response != null) {
//                    Toast.makeText(this, "User canceled the request", Toast.LENGTH_SHORT).show();
//                } else {
                Toast.makeText(this, "User canceled the request", Toast.LENGTH_SHORT).show();
//                }
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (!selectedMedicines.contains("Medical Slip")) {
                selectedMedicines.add("Medical Slip");
            }

            Toast.makeText(this, "Slip Uploaded", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedicines() {
//        Log.i("PocketHospitalLog", "loadMedicines");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pharmacy-stock")
                .where(Filter.equalTo("status", FireVocabulary.PHARMACY_STOCK_STATUS_1))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> medicineList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                medicineList.add(document.getString("name"));
//                                Log.i("PocketHospitalLog", document.getString("name"));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(EPharmacyActivity.this, android.R.layout.simple_spinner_item, medicineList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            medicineSpinner.setAdapter(adapter);
                        }
                    }
                });
    }

    private void saveRequestToFirestore(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        String base64String = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> request = new HashMap<>();
        request.put("mobile", userObj.getMobile());
        request.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        request.put("status", FireVocabulary.PHARMACY_REQUEST_STATUS_1);
        request.put("items", selectedMedicines);
        request.put("static-price", "00.00");
        request.put("image", base64String);

        firestore.collection("pharmacy-request")
                .add(request)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        listenForApproval(String.valueOf(documentReference.getId()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EPharmacyActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelRequest() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("pharmacy-request")
                .where(Filter.and(
                        Filter.equalTo("mobile", userObj.getMobile()),
                        Filter.equalTo("status", FireVocabulary.PHARMACY_REQUEST_STATUS_1)
                ))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                firestore.collection("pharmacy-request").document(document.getId()).update("status", FireVocabulary.PHARMACY_REQUEST_STATUS_2);
                            }
                        }
                    }
                });
    }

    private void cancelOrder() {
    }

    private void listenForApproval(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pharmacy-request").document(id)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null && value != null) {
                            String status = value.getString("status");
                            if (FireVocabulary.PHARMACY_REQUEST_STATUS_3.equals(status)) {
                                actionState = "Proceed";
                                actionButton.setText("Proceed");
//                                String amountTxt = value.getString("static-price");
//                                if (amountTxt == null) {
//                                    amount = 00.00;
//                                } else {
//                                    amount = Double.parseDouble(amountTxt);
//                                }

                                amount = 1200.00;

                            }
                        }
                    }
                });
    }

    private static final int PICK_IMAGE_REQUEST = 112;
    private Uri imageUri;
    private Spinner medicineSpinner;
    private List<String> selectedMedicines = new ArrayList<>();
    private String actionState = "Next";
    private User userObj;
    private Button actionButton;
    private final int PAYHERE_REQUEST = 1564;

    private Double amount;
}



