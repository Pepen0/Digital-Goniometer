package com.example.goniometer;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PatientListActivity extends BaseActivity implements Patient_option.OnPatientDeletedListener {

    private ListView listViewPatients;
    private DatabaseHelper dbHelper;
    private Patient_Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_list);

        listViewPatients = findViewById(R.id.listViewPatients);
        dbHelper = new DatabaseHelper(this);

        displayPatients();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Newpatient dialogFragment = new Newpatient();
            dialogFragment.setOnNewPatientListener((firstName, lastName) -> {
                long id = dbHelper.addPatient(firstName, lastName);
                if (id != -1) {
                    Toast.makeText(PatientListActivity.this, "Patient added with ID: " + id, Toast.LENGTH_SHORT).show();
                    displayPatients();
                } else {
                    Toast.makeText(PatientListActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
                }
            });
            dialogFragment.show(getSupportFragmentManager(), "NewPatientDialogFragment");
        });

        listViewPatients.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = (Patient) parent.getItemAtPosition(position);
            Patient_option dialogFragment = Patient_option.newInstance(selectedPatient, position);
            dialogFragment.show(getSupportFragmentManager(), "Patient_option");
        });
        setupToolbar();
    }

    private void displayPatients() {
        List<Patient> patients = dbHelper.getAllPatients();
        adapter = new Patient_Adapter(this, patients, dbHelper);
        listViewPatients.setAdapter(adapter);
    }

    @Override
    public void onPatientDeleted(int position) {
        adapter.removePatient(position);
    }
}
