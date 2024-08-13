package com.example.goniometer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.List;

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

    private boolean hasMeasurements(long patientId) {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        List<Measurement> measurements = dbHelper.getMeasurementsForPatient(patientId);
        return measurements != null && !measurements.isEmpty();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_option, container, false);

        TextView textViewPatientDetails = view.findViewById(R.id.textViewPatientDetails);
        textViewPatientDetails.setText("  Patient " + patient.getId() + " Option                  ");

        Button buttonDelete = view.findViewById(R.id.buttonDelete);
        Button buttonAssessment = view.findViewById(R.id.buttonAssessment);
        Button buttonMeasurements = view.findViewById(R.id.buttonMeasurements);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Patient");
        builder.setMessage("Are you sure you want to delete this patient?");

        buttonDelete.setOnClickListener(v -> {
            FunctionsController.askForConfirmation(getContext(),
                    "Delete Patient",
                    "Are you sure you want to delete this patient?",
                    "yes", ()-> {
                        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                        dbHelper.deletePatient(patient.getId());
                        callback.onPatientDeleted(position);
                        Toast.makeText(getContext(), "Patient deleted", Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    });

        });

        buttonAssessment.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AssessmentActivity.class);
            intent.putExtra("PATIENT_ID", patient.getId()); // Pass patient ID
            startActivity(intent);
            dismiss();
        });

        buttonMeasurements.setOnClickListener(v -> {
            long patientId = patient.getId(); // Get the patient ID
            if (hasMeasurements(patientId)) {
                Intent intent = new Intent(getActivity(), MeasurementsActivity.class);
                intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Patient has no records", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        return view;
    }
}
