package com.example.goniometer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeasurementsActivity extends BaseActivity {

    private static final String TAG = "MeasurementsActivity"; // Tag for logging
    private ListView listViewMeasurements;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<Measurement> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);

        listViewMeasurements = findViewById(R.id.listViewMeasurements);
        TextView textViewPatientId = findViewById(R.id.textViewPatientId); // Find the TextView for patient info
        dbHelper = DatabaseHelper.getInstance(this); // Use the singleton instance

        // Retrieve patient ID from intent
        long patientId = getIntent().getLongExtra("PATIENT_ID", -1);

        Log.d(TAG, "Patient ID: " + patientId);

        if (patientId != -1) {
            // Get the patient name from the database
            String patientName = dbHelper.getPatientNameById(patientId);

            if (patientName != null) {
                // Construct and set the patient details text
                String patientInfo = "Name: " + patientName + "\n" + "ID: " + patientId;
                textViewPatientId.setText(patientInfo);
            } else {
                textViewPatientId.setText("Patient not found");
                Log.e(TAG, "Patient not found for ID: " + patientId);
            }

            displayMeasurements(patientId);
        } else {
            Log.e(TAG, "Invalid patient ID: " + patientId);
            textViewPatientId.setText("Patient not found");
        }
        setupToolbar();
    }



    private void displayMeasurements(long patientId) {
        try {
            Log.d(TAG, "Fetching measurements for patient ID: " + patientId);
            // Retrieve measurements from the database
            List<Measurement> measurements = dbHelper.getMeasurementsForPatient(patientId);

            // Check if the measurements list is null and initialize it if necessary
            if (measurements == null) {
                Log.d(TAG, "Measurements list is null, initializing to empty list.");
                measurements = new ArrayList<>();
            }

            // Reverse the list to show the newest items first
            Collections.reverse(measurements);

            // Create a custom ArrayAdapter to display measurements
            adapter = new ArrayAdapter<Measurement>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, measurements) {

                @SuppressLint("SetTextI18n")
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                    }

                    // Get the measurement at the current position
                    Measurement measurement = getItem(position);
                    Log.d(TAG, "Displaying measurement at position: " + position);

                    // Set the text of the item view to display measurement details
                    TextView textView = convertView.findViewById(android.R.id.text1);
                    if (measurement != null) {
                        if (measurement.getMeasurementType().equals("Head Rotation") ||
                                measurement.getMeasurementType().equals("Right Elbow Rotation") ||
                                measurement.getMeasurementType().equals("Left Elbow Rotation")) {
                            if(!measurement.getMeasurementType().equals("Head Rotation") ){
                                textView.setText("Type: " + measurement.getMeasurementType() + "\n" +
                                        "Pronation : " + measurement.getLeftAngle() + "°" + "\n" +
                                        "Supination : " + measurement.getRightAngle()+ "°" + "\n" +
                                        measurement.getTimestamp());
                            } else {
                                textView.setText("Type: " + measurement.getMeasurementType() + "\n" +
                                        "LeftAngle: " + measurement.getLeftAngle() + "°" + "\n" +
                                        "RightAngle: " + measurement.getRightAngle() + "°" + "\n" +
                                        measurement.getTimestamp());
                            }
                        } else {
                            textView.setText("Type: " + measurement.getMeasurementType() + "\n" +
                                    "AbductionAngle: " + measurement.getLeftAngle() +"°" + "\n" +
                                    measurement.getTimestamp());
                        }
                    } else {
                        textView.setText("No data available");
                        Log.d(TAG, "Measurement at position " + position + " is null.");
                    }

                    return convertView;
                }
            };

            // Set the adapter for the ListView
            listViewMeasurements.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying measurements", e);
        }
    }

}
