package com.example.goniometer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PatientActivity extends Base_activity {

    EditText etFirstName, etLastName;
    Button btnAddPatient;
    Button btnPatients;
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
        btnPatients = findViewById(R.id.Patientsbutton);

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

        // Set click listener for Patients button
        btnPatients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientActivity.this, PatientListActivity.class);
                startActivity(intent); // Use startActivity with the Intent object
            }
        });
    }
}
