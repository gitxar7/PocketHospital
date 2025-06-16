package com.ar7enterprise.pockethospital;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ar7enterprise.pockethospital.model.City;
import com.ar7enterprise.pockethospital.model.Gender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    List<Gender> genderList;
    List<City> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.servicesMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText nameField = findViewById(R.id.regName);
        EditText dobField = findViewById(R.id.regDOB);
        EditText mobileField = findViewById(R.id.regMobile);
        EditText passwordField = findViewById(R.id.regPassword);
        EditText genderSpinnerField = findViewById(R.id.regGenderCon);
        Spinner genderSpinner = findViewById(R.id.regGender);
        AutoCompleteTextView citySpinner = findViewById(R.id.regCity);
        Button registerButton = findViewById(R.id.buttonRegister);
        Button goSignInButton = findViewById(R.id.buttonToSignIn);

        Calendar calendar = Calendar.getInstance();

        dobField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dobField.setText(sdf.format(calendar.getTime()));
                        if (!isAgeValid(selectedYear, selectedMonth, selectedDay)) {
                            dobField.setError("You must be 16 or older");
                            Toast.makeText(RegisterActivity.this, "You must be 16 or older", Toast.LENGTH_SHORT).show();
                        } else {
                            dobField.setError(null);
                        }
                    }
                }, year, month, day);

                datePickerDialog.show();
            }
        });

        final List<String> cityNames = new ArrayList<>();
        final List<String> genderNames = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Gson gson = new Gson();
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/PocketHospital/GetData")
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String responseTxt = response.body().string();
                    Log.i("PocketHospitalLog", responseTxt);

                    JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);

                    Type genderListType = new TypeToken<List<Gender>>() {
                    }.getType();
                    genderList = gson.fromJson(responseObj.get("GenderList"), genderListType);

                    Type cityListType = new TypeToken<List<City>>() {
                    }.getType();
                    cityList = gson.fromJson(responseObj.get("CityList"), cityListType);

                    genderNames.add("Select");
                    for (Gender gender : genderList) {
                        genderNames.add(gender.getName());
                    }

                    for (City city : cityList) {
                        cityNames.add(city.getName());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(RegisterActivity.this, R.layout.spinner_item, genderNames);
                            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(RegisterActivity.this, R.layout.spinner_item, cityNames);
                            genderAdapter.setDropDownViewResource(R.layout.spinnder_dropdown_item);

                            genderSpinner.setAdapter(genderAdapter);
                            citySpinner.setAdapter(cityAdapter);
                        }
                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameField.getText().toString();
                String dob = dobField.getText().toString();
                String gender = genderSpinner.getSelectedItem().toString();
                String city = citySpinner.getText().toString();
                String mobile = mobileField.getText().toString();
                String password = passwordField.getText().toString();

                if (name.isEmpty()) {
                    nameField.setError("Name can not be empty");
                } else if (dob.isEmpty()) {
                    dobField.setError("Birthday can not be empty");
                } else if (gender.equals("Select") || gender.isEmpty()) {
                    genderSpinnerField.setError("Select your Gender");
                } else if (city.isEmpty()) {
                    genderSpinnerField.setError(null);
                    citySpinner.setError("City can not be empty");
                } else if (!cityNames.contains(city) || city.equals("Select")) {
                    citySpinner.setError("Invalid city");
                } else if (mobile.isEmpty()) {
                    mobileField.setError("Mobile can not be empty");
                } else if (password.isEmpty()) {
                    passwordField.setError("Password can not be empty");
                } else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int cityId = 0;
                            int genderId = 0;

                            for (Gender genderObj : genderList) {
                                if (genderObj.getName().equals(gender)) {
                                    genderId = genderObj.getId();
//                                    Log.i("PocketHospitalLog", String.valueOf(genderId));
                                }
                            }

                            for (City cityObj : cityList) {
                                if (cityObj.getName().equals(city)) {
                                    cityId = cityObj.getId();
                                }
                            }

                            Gson gson = new Gson();
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("name", name);
                            jsonObject.addProperty("birthday", dob);
                            jsonObject.addProperty("mobile", mobile);
                            jsonObject.addProperty("password", password);
                            jsonObject.addProperty("city", cityId);
                            jsonObject.addProperty("gender", genderId);

                            OkHttpClient okHttpClient = new OkHttpClient();
                            RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
                            Request request = new Request.Builder()
                                    .url("http://10.0.2.2:8080/PocketHospital/Register")
                                    .post(requestBody)
                                    .build();

                            try {
                                Response response = okHttpClient.newCall(request).execute();
                                String responseTxt = response.body().string();
                                Log.i("PocketHospitalLog", responseTxt);
                                JsonObject responseObj = gson.fromJson(responseTxt, JsonObject.class);

                                if (responseObj.get("scc").getAsBoolean()) {
                                    Intent intent = new Intent(RegisterActivity.this, SignInActivity.class);
                                    startActivity(intent);
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, responseObj.get("msg").getAsString(), Toast.LENGTH_LONG).show();
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

        goSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, SignInActivity.class));
            }
        });

    }

    private boolean isAgeValid(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age >= 16;
    }
}


//class CustomAdapter extends ArrayAdapter<Object> {
//
//    List<Object> dataList;
//    int layout;
//
//    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<Object> objects) {
//        super(context, resource, objects);
//        dataList = objects;
//        layout = resource;
//    }
//
//    @Override
//    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//        View view = inflater.inflate(layout, parent, false);
//        TextView textView = view.findViewById(R.id.textView);
//        textView.setText(dataList.get(position).hashCode());
//        ImageView imageView = view.findViewById(R.id.imageView2);
//        imageView.setImageResource(position);
//
//        return view;
//    }
//
//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return getDropDownView(position, convertView, parent);
//    }
//}