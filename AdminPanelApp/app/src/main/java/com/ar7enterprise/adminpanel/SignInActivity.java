package com.ar7enterprise.adminpanel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ar7enterprise.adminpanel.model.Admin;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText mobileField = findViewById(R.id.siMobile);
        EditText passwordField = findViewById(R.id.siPassword);
        Button signInButton = findViewById(R.id.buttonSignIn);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mobileField.getText().toString();
                String password = passwordField.getText().toString();

                if (mobile.isEmpty()) {
                    mobileField.setError("Mobile can not be empty");
                } else if (password.isEmpty()) {
                    passwordField.setError("Password can not be empty");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("mobile", mobile);
                            jsonObject.addProperty("password", password);

                            OkHttpClient okHttpClient = new OkHttpClient();
                            RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
                            Request request = new Request.Builder()
                                    .url("http://10.0.2.2:8080/PocketHospital/AdminSignIn")
                                    .post(requestBody)
                                    .build();

                            try {
                                Response response = okHttpClient.newCall(request).execute();
                                String responseTxt = response.body().string();
                                JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);

                                if (responseObj.get("scc").getAsBoolean()) {
                                    Admin admin  = gson.fromJson(responseObj.get("user"), Admin.class);

                                    SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("admin", gson.toJson(admin));
                                    editor.apply();

                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SignInActivity.this, responseObj.get("msg").getAsString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
                }
            }
        });

    }
}