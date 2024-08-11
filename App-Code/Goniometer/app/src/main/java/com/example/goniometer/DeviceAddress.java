package com.example.goniometer;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DeviceAddress extends DialogFragment {
    private String PhysicalAddress;
    private DeviceAddress.SetDeviceAddress listener;

    //Callback interface to handle  address changes
    public interface SetDeviceAddress {
        void OnAddressChange(String PhysicalAddress);
    }

    public void SetDeviceAddress(DeviceAddress.SetDeviceAddress listener) {
        this.listener = listener;
    }

    @NonNull
    @Override

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_device_address, null);

        final EditText DeviceAddressEditText = dialogView.findViewById(R.id.DeviceAddressEditText);
        DeviceAddressEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        // Inflate the custom title view
        View customTitleView = inflater.inflate(R.layout.custom_dialog_title2, null);
        builder.setView(dialogView)
                .setCustomTitle(customTitleView) // Set custom title view
                .setPositiveButton("Save", null) // Set to null initially
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()); // Dismiss on cancel

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#008B8B")); // Set text color
                positiveButton.setOnClickListener(v -> {
                    PhysicalAddress = DeviceAddressEditText.getText().toString().trim();

                    if (validInput(PhysicalAddress)) {
                        MainActivity.BluetoothButton.setText("Connect to Device");
                            listener.OnAddressChange(PhysicalAddress); //Notify listener of the new address
                            dismiss(); //close the dialog
                        }
                     else {
                        Toast.makeText(getActivity(), "Invalid address format", Toast.LENGTH_SHORT).show();
                    }

                });
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.parseColor("#008B8B")); // Set text color
            }
        });

        return dialog;
    }

    //Check the physical address input if matches the provided pattern
    public static boolean validInput(String address){
        return address != null && address.matches("^[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}$");
    }



}