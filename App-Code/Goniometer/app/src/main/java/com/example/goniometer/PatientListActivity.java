package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class PatientListActivity extends AppCompatActivity implements Patient_option.OnPatientDeletedListener {
    private DatabaseHelper dbHelper;
    private Patient_Adapter adapter;
    private ListView listViewPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_list);

        dbHelper = DatabaseHelper.getInstance(this);
        listViewPatients = findViewById(R.id.listViewPatients);

        List<Patient> patients = dbHelper.getAllPatients();
        adapter = new Patient_Adapter(this, patients, dbHelper);
        listViewPatients.setAdapter(adapter);
    }

    @Override
    public void onPatientDeleted(int position) {
        dbHelper.deletePatient(adapter.getItem(position).getId());
        adapter.removePatient(position);
        Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show();
    }

    private void showPatientOptionDialog(Patient patient, int position) {
        Patient_option patientOptionFragment = Patient_option.newInstance(patient, position);
        patientOptionFragment.show(getSupportFragmentManager(), "PatientOptionFragment");
    }
}
