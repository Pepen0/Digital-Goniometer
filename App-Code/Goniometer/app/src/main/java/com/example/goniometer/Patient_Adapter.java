package com.example.goniometer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        Button buttonOptions = convertView.findViewById(R.id.buttonOptions);

        final Patient patient = getItem(position);
        if (patient != null) {
            textViewPatientName.setText(patient.getFirstName() + " " + patient.getLastName());

            buttonOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch the dialog to choose an action
                    Patient_option dialogFragment = Patient_option.newInstance(patient, position);
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "Patient_option");
                }
            });
        }

        return convertView;
    }

    public void removePatient(int position) {
        patients.remove(position);
        notifyDataSetChanged();
    }
}
