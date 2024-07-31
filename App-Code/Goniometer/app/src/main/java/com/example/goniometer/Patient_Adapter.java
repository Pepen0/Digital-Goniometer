package com.example.goniometer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Patient_Adapter extends ArrayAdapter<Patient> {
    private Context context;
    private List<Patient> patients;
    private DatabaseHelper dbHelper;

    public Patient_Adapter(Context context, List<Patient> patients, DatabaseHelper dbHelper) {
        super(context, 0, patients);
        this.context = context;
        this.patients = patients;
        this.dbHelper = dbHelper;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_patient, parent, false);
        }

        TextView textViewPatientName = convertView.findViewById(R.id.textViewPatientName);
        Button buttonDelete = convertView.findViewById(R.id.buttonDelete);

        final Patient patient = getItem(position);
        textViewPatientName.setText(patient.getFirstName() + " " + patient.getLastName());

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the patient from the database and the list
                dbHelper.deletePatient(patient.getId());
                patients.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Patient deleted", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}