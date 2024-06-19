package com.example.goniometer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PatientActivity extends AppCompatActivity {

    protected FloatingActionButton floatingAddNewPatient;
    protected EditText editTextPatientName;
    protected TextView textViewPatientList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();

    }

    private void setupUI() {
        editTextPatientName = findViewById(R.id.editTextCurrentName);

        textViewPatientList = findViewById(R.id.textViewPatientList);


        floatingAddNewPatient = findViewById(R.id.floatingAddNewPatient);
        floatingAddNewPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // connect to patient database
                long id = -1;
                String patientName = "First Patient";
                PatientData patient = new PatientData(id, patientName);

                /*
                create database helper here
                databaseHelper = new DatabaseHelper(getApplicationContext());
                databaseHelper.insertCourse(course);
                */
            }
        });
    }
}