package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PatientActivity extends AppCompatActivity {

    EditText etFirstName, etLastName;
    Button btnAddPatient;
    Button btnDeletePatients;
    Button btnSelectPatient;
    Button btnviewRecords;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize EditTexts and Buttons
        etFirstName = findViewById(R.id.FirstNameText);
        etLastName = findViewById(R.id.LastNameText);
        btnAddPatient = findViewById(R.id.AddButton);
        btnDeletePatients = findViewById(R.id.DeletePatientbutton);
        btnSelectPatient = findViewById(R.id.SelectPatientsbutton);
        btnviewRecords = findViewById(R.id.ViewRecordsbutton);

        // Set click listener for Add Patient button
        btnAddPatient.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();

            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                long id = dbHelper.addPatient(firstName, lastName); // Use dbHelper to add patient
                if (id != -1) {
                    Toast.makeText(PatientActivity.this, "Patient added with ID: " + id, Toast.LENGTH_SHORT).show();
                    etFirstName.setText("");
                    etLastName.setText("");
                } else {
                    Toast.makeText(PatientActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PatientActivity.this, "Please enter both first name and last name", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for Delete Patients button
        btnDeletePatients.setOnClickListener(v -> {
            Intent intent = new Intent(PatientActivity.this, PatientListActivity.class);
            intent.putExtra("ACTION", "DELETE");
            startActivity(intent);
        });

        // Set click listener for Select Patient button
        btnSelectPatient.setOnClickListener(v -> {
            Intent intent = new Intent(PatientActivity.this, PatientListActivity.class);
            intent.putExtra("ACTION", "VIEW");
            startActivity(intent);
        });

        // Set click listener for View Records button
        btnviewRecords.setOnClickListener(v -> {
            // Ensure that a patient is selected before showing records
            Intent intent = new Intent(PatientActivity.this, PatientListActivity.class);
            intent.putExtra("ACTION", "VIEW_MEASUREMENTS");
            startActivity(intent);
        });
    }
}
