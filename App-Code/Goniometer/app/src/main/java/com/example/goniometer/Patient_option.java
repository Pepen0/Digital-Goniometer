package com.example.goniometer;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class Patient_option extends DialogFragment {

    private static final String ARG_PATIENT_ID = "patient_id";
    private long patientId;

    public static Patient_option newInstance(long patientId) {
        Patient_option fragment = new Patient_option();
        Bundle args = new Bundle();
        args.putLong(ARG_PATIENT_ID, patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getLong(ARG_PATIENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_option, container, false);

        TextView textViewPatientDetails = view.findViewById(R.id.textViewPatientDetails);
        textViewPatientDetails.setText("Patient ID: " + patientId);

        Button buttonAssessment = view.findViewById(R.id.buttonAssessment);
        Button buttonMeasurements = view.findViewById(R.id.buttonMeasurements);

        buttonAssessment.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AssessmentActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
            dismiss();
        });

        buttonMeasurements.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MeasurementsActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
            dismiss();
        });

        return view;
    }
}
