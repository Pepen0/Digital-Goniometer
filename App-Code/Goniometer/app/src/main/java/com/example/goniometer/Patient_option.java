package com.example.goniometer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class Patient_option extends DialogFragment {

    private long patientId;

    public static Patient_option newInstance(long patientId) {
        Patient_option fragment = new Patient_option();
        Bundle args = new Bundle();
        args.putLong("PATIENT_ID", patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        patientId = getArguments().getLong("PATIENT_ID");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Patient Options")
                .setItems(new String[]{"View Measurements", "Start Assessment"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Handle View Measurements
                            viewMeasurements();
                        } else if (which == 1) {
                            // Handle Start Assessment
                            startAssessment();
                        }
                    }
                });

        return builder.create();
    }

    private void viewMeasurements() {
        Intent intent = new Intent(getActivity(), MeasurementsActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        startActivity(intent);
    }

    private void startAssessment() {
        Intent intent = new Intent(getActivity(), AssessmentActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        startActivity(intent);
    }
}
