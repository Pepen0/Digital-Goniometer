

package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PatientListActivity extends AppCompatActivity  {

    private ListView listViewPatients;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<Patient> adapter;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_list);

        listViewPatients = findViewById(R.id.listViewPatients);
        dbHelper = new DatabaseHelper(this);

        // Get the action from the intent
        action = getIntent().getStringExtra("ACTION");

        // Call method to display patients
        displayPatients();

        // Set onClickListener for each item in the ListView
        listViewPatients.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = adapter.getItem(position);
            if (selectedPatient != null) {
                if ("DELETE".equals(action)) {
                    deletePatient(selectedPatient.getId());
                } else if ("VIEW".equals(action)) {
                    goToAssessmentPage(selectedPatient.getId());
                } else if ("VIEW_MEASUREMENTS".equals(action)) {
                    viewMeasurements(selectedPatient.getId());
                }
            }
        });

        // Set up the FAB to open the dialog for adding a patient
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Newpatient dialogFragment = new Newpatient();
            dialogFragment.setOnNewPatientListener((firstName, lastName) -> {
                long id = dbHelper.addPatient(firstName, lastName);
                if (id != -1) {
                    Toast.makeText(PatientListActivity.this, "Patient added with ID: " + id, Toast.LENGTH_SHORT).show();
                    displayPatients(); // Refresh the list
                } else {
                    Toast.makeText(PatientListActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
                }
            });
            dialogFragment.show(getSupportFragmentManager(), "NewPatientDialogFragment");
        });
    }


        private void displayPatients () {
        List<Patient> patients = dbHelper.getAllPatients();

        // Create a custom ArrayAdapter to display patient names
        adapter = new ArrayAdapter<Patient>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, patients) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Get the patient at the current position
                Patient patient = getItem(position);

                // Set the text of the item view to display patient information
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(patient.getFirstName() + " " + patient.getLastName()+"\nID:"+patient.getId());

                return view;
            }
        };

        listViewPatients.setAdapter(adapter);
    }

        private void deletePatient ( long patientId){
        dbHelper.deletePatient(patientId);
        adapter.clear();
        adapter.addAll(dbHelper.getAllPatients());
        adapter.notifyDataSetChanged();

        // Display a Toast message indicating successful deletion
        Toast.makeText(this, "Patient " + patientId + " deleted", Toast.LENGTH_SHORT).show();
    }

        private void goToAssessmentPage ( long patientId){
        Intent intent = new Intent(PatientListActivity.this, AssessmentActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        startActivity(intent);
    }

        private void viewMeasurements ( long patientId){
        Intent intent = new Intent(PatientListActivity.this, MeasurementsActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        startActivity(intent);
    }

    }