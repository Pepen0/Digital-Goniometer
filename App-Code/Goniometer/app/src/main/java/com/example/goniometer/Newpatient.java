package com.example.goniometer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Newpatient extends DialogFragment {

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

        final EditText editTextFirstName = dialogView.findViewById(R.id.editTextPatientName);
        final EditText editTextLastName = dialogView.findViewById(R.id.editTextPatientlastName);

        builder.setView(dialogView)
                .setTitle("Add New Patient")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = editTextFirstName.getText().toString().trim();
                        String lastName = editTextLastName.getText().toString().trim();

                        if (!firstName.isEmpty() && !lastName.isEmpty()) {
                            if (listener != null) {
                                listener.onNewPatient(firstName, lastName);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Please enter both first name and last name", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}

