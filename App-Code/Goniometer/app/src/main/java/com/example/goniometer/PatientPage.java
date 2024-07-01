package com.example.goniometer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class PatientPage extends AppCompatActivity {
    protected EditText firstName;
    protected EditText lastName;
    protected Button addPatient;
    protected ListView listPatients;
    private DataBaseHelper dataBaseHelper;
    private int patientID;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
setupUI();

    }
    private void setupUI(){
        // Initialize database helper and UI elements
firstName = findViewById(R.id.firstName);
lastName = findViewById(R.id.LastName);
addPatient = findViewById(R.id.addPatient);
listPatients= findViewById(R.id.listPatients);
dataBaseHelper = new DataBaseHelper(this);
patientID =getIntent().getIntExtra()


    }
}
