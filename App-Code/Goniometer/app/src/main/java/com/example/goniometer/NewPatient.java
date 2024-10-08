package com.example.goniometer;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewPatient extends DialogFragment {

    private OnNewPatientListener listener;

    public interface OnNewPatientListener {
        void onNewPatient(String firstName, String lastName);
    }

    public void setOnNewPatientListener(OnNewPatientListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.newpatient, null);

        final EditText editTextFirstName = dialogView.findViewById(R.id.DeviceAddressEditText);
        final EditText editTextLastName = dialogView.findViewById(R.id.editTextPatientlastName);

        // Inflate the custom title view
        View customTitleView = inflater.inflate(R.layout.custom_dialog_title, null);

        builder.setView(dialogView)
                .setCustomTitle(customTitleView) // Set custom title view
                .setPositiveButton("Add", null) // Set to null initially
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()); // Dismiss on cancel

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#008B8B")); // Set text color
                positiveButton.setOnClickListener(v -> {
                    String firstName = editTextFirstName.getText().toString().trim();
                    String lastName = editTextLastName.getText().toString().trim();

                    if (!firstName.isEmpty() && !lastName.isEmpty()) {
                        if (listener != null) {
                            listener.onNewPatient(firstName, lastName);
                            dismiss();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please enter both first and last name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.parseColor("#008B8B")); // Set text color
            }
        });

        return dialog;
    }



}

