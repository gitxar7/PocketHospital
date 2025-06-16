package com.ar7enterprise.pockethospital;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MyStepsActivity extends AppCompatActivity implements SensorEventListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_steps);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myStepsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences1 = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences1.getString("user", null);
        if (user == null) {
            startActivity(new Intent(MyStepsActivity.this, GuestActivity.class));
            finish();
        }

        findViewById(R.id.msProfileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyStepsActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.msServiceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyStepsActivity.this, ServicesActivity.class));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 2);
            }
        }

        TextView action = findViewById(R.id.msActionButton);
        steps = findViewById(R.id.msSteps);
        calToday = findViewById(R.id.msCalToday);
        time = findViewById(R.id.msTime);
        calories = findViewById(R.id.msCalories);
        distance = findViewById(R.id.msDistance);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sharedPreferences = getSharedPreferences("FitnessData", Context.MODE_PRIVATE);
        resetDailyCalories();
        calToday.setText(Math.round(sharedPreferences.getFloat("dailyCalories", 0)) + " kcal");

        if (sensor == null) {
            action.setText("Unavailable");
            steps.setText("Incompatible Device");
            action.setClickable(false);
        } else {
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                        if (!isTracking) {
                            startTracking();
                            action.setText("Stop");
                            action.setBackgroundResource(R.color.error);
                        } else {
                            stopTracking();
                            action.setText("Start");
                            action.setBackgroundResource(R.drawable.bg_gradient);
                        }
                    }
                }
            });
        }


        barChart = findViewById(R.id.msChart);
        setupBarChart(barChart);

//        ArrayList<BarEntry> barEntries = new ArrayList<>();
//        barEntries.add(new BarEntry(10, 600));
//        barEntries.add(new BarEntry(20, 100));
//        barEntries.add(new BarEntry(30, 400));
//        barEntries.add(new BarEntry(40, 410));
//        barEntries.add(new BarEntry(50, 240));
//        barEntries.add(new BarEntry(60, 560));
//        barEntries.add(new BarEntry(70, 130));
//
//        BarDataSet barDataSet = new BarDataSet(barEntries, "Calories Burned");
//        ArrayList<Integer> colors = new ArrayList<>();
//        colors.add(getColor(R.color.gradient_2));
//        colors.add(getColor(R.color.gradient_2));
//        colors.add(getColor(R.color.gradient_2));
//        colors.add(getColor(R.color.gradient_2));
//        colors.add(getColor(R.color.gradient_2));
//        colors.add(getColor(R.color.gradient_2));
//        colors.add(getColor(R.color.gradient_2));
//        barDataSet.setColors(colors);
//
//        BarData barData = new BarData();
//        barData.addDataSet(barDataSet);
//
//        // bar customization
//        barData.setBarWidth(7);
//
//        // chart customization
//        barChart.setPinchZoom(false);
//        barChart.setScaleEnabled(false);
//        barChart.animateY(2000, Easing.EaseInCubic);
//        barChart.setDescription(null);
//        barChart.setFitBars(true);
//
//        barChart.setData(barData);
//        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
//        legendEntries.add(new LegendEntry("Mon", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        legendEntries.add(new LegendEntry("Tue", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        legendEntries.add(new LegendEntry("Wed", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        legendEntries.add(new LegendEntry("Thu", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        legendEntries.add(new LegendEntry("Fri", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        legendEntries.add(new LegendEntry("Sat", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        legendEntries.add(new LegendEntry("Sun", Legend.LegendForm.CIRCLE, Float.NaN, Float.NaN, null, getColor(R.color.gradient_2)));
//        barChart.getLegend().setCustom(legendEntries);
//        barChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//        barChart.getLegend().setXEntrySpace(20);
//
//        barChart.invalidate();
    }

    private void saveCaloriesForTheDay(float calories) {
        int dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (dayIndex < 0) dayIndex = 6;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("calories_" + dayIndex, calories);
        editor.apply();
    }

    private void setupBarChart(BarChart barChart) {
//        float[] caloriesBurned = {450, 380, 500, 550, 620, 580, 400};
        final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            entries.add(new BarEntry(i, sharedPreferences.getFloat("calories_" + i, 0)));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Calories Burned");
        barDataSet.setColor(getColor(R.color.light_primary2));
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setTouchEnabled(false);
        barChart.invalidate();


        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return days[(int) value];
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isTracking && event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++;
            steps.setText(stepCount + " Steps");
//            Log.i("Steps", " " + stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startTracking() {
        if (sensor != null) {
            isTracking = true;
            stepCount = 0;
            startTime = SystemClock.elapsedRealtime();
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void stopTracking() {
        if (isTracking) {
            isTracking = false;
            endTime = SystemClock.elapsedRealtime();
            sensorManager.unregisterListener(this);

            float distanceTravelled = stepCount * stepLength / 1000;
            float caloriesBurned = distanceTravelled * caloriesPerKM;
            long timeSpent = (endTime - startTime) / 1000;
            long minutes = timeSpent / 60;
            long sec = timeSpent % 60;
            String formTime = String.format(Locale.US, "%02d:%02d", minutes, sec);

            distance.setText(String.format(Locale.US, "%.2f km", distanceTravelled));
            calories.setText(String.format(Locale.US, "%.2f kcal", caloriesBurned));
            time.setText(formTime);

            float newDailyCalories = sharedPreferences.getFloat("dailyCalories", 0) + caloriesBurned;
            sharedPreferences.edit().putFloat("dailyCalories", newDailyCalories).apply();
            calToday.setText(String.format(Locale.US, "%.2f kcal", newDailyCalories));

            saveCaloriesForTheDay(caloriesBurned);
            setupBarChart(barChart);
        }
    }

    private void resetDailyCalories() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String lastDate = sharedPreferences.getString("lastDate", "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());

        if (!lastDate.equals(todayDate)) {
            editor.putString("lastDate", todayDate);
            editor.putFloat("dailyCalories", 0);
            editor.apply();
            calToday.setText("0 kcal");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTracking) {
            startTracking();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                steps.setText("Permission Required");
            }
        }
    }

    //    variables
    private SensorManager sensorManager;
    private Sensor sensor;
    private boolean isTracking = false;
    private int stepCount = 0;
    private long startTime;
    private long endTime;
    final private float stepLength = 0.75f;
    final private float caloriesPerKM = 50f;
    private SharedPreferences sharedPreferences;
    private TextView steps;
    private TextView calToday;
    private TextView time;
    private TextView calories;
    private TextView distance;
    BarChart barChart;
//    final private String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    //    variables
}