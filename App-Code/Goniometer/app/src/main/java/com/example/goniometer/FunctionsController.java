package com.example.goniometer;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;

import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FunctionsController {

    public static void askForConfirmation(Context context, String title, String Dialog, String yesButton, Runnable onPositive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(Dialog);
        //implementation of Yes, No choices and what action it does
        builder.setPositiveButton(yesButton, (dialog, which) -> {
            if(onPositive != null){
                onPositive.run();
            }
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    public static void saveMeasurement(Context context,
                                 DatabaseHelper dbHelper,
                                 long patientId,
                                 String measurementType,
                                 double leftAngle,
                                 double rightAngle , Button SaveButton) {

        // Format the current timestamp to include date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd @ HH:mm", Locale.getDefault()); // ISO 8601 format
        String timestamp = sdf.format(new Date());

        // Add measurement to the database
        long id = dbHelper.addMeasurement(patientId, measurementType, leftAngle, rightAngle, timestamp);

        // Check if the insertion was successful
        if (id != -1) {
            Toast.makeText(context, "Measurement saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to save measurement", Toast.LENGTH_SHORT).show();
        }
        SaveButton.setVisibility(View.GONE);
    }
}
