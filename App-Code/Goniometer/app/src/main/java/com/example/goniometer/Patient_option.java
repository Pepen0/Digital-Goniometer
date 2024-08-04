package com.example.goniometer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.io.Serializable;

public class Patient_option extends DialogFragment {

    private static final String ARG_PATIENT = "patient";
    private static final String ARG_POSITION = "position";

    private Patient patient;
    private int position;
    private OnPatientDeletedListener callback;

    public static Patient_option newInstance(Patient patient, int position) {
        Patient_option fragment = new Patient_option();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PATIENT, patient);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnPatientDeletedListener {
        void onPatientDeleted(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPatientDeletedListener) {
            callback = (OnPatientDeletedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPatientDeletedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patient = (Patient) getArguments().getSerializable(ARG_PATIENT);
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_option, container, false);

        TextView textViewPatientDetails = view.findViewById(R.id.textViewPatientDetails);
        textViewPatientDetails.setText("Patient Option                  ");

        Button buttonDelete = view.findViewById(R.id.buttonDelete);
        Button buttonAssessment = view.findViewById(R.id.buttonAssessment);
        Button buttonMeasurements = view.findViewById(R.id.buttonMeasurements);

        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Patient")
                    .setMessage("Are you sure you want to delete this patient?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        dbHelper.deletePatient(patient.getId());
                        callback.onPatientDeleted(position);
                        Toast.makeText(getContext(), "Patient deleted", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        buttonAssessment.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AssessmentActivity.class);
            intent.putExtra("PATIENT_ID", patient.getId()); // Pass patient ID
            startActivity(intent);
            dismiss();
        });

        buttonMeasurements.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MeasurementsActivity.class);
            intent.putExtra("PATIENT_ID", patient.getId()); // Pass patient ID
            startActivity(intent);
            dismiss();
        });

        return view;
    }
}

