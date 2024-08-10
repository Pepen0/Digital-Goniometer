package com.example.goniometer;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;

import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//This Class will provide utility Methods to be used in all measurements
public class FunctionsController {

    /**
     * Displays a confirmation dialog to the user for all measurements
     * @param context        In which context the dialog will be shown
     * @param title          The tittle of the dialog
     * @param Dialog         The message will be shown inside the dialog box
     * @param yesButton      What is the positive button called
     * @param onPositive     The action to be performed when user click on yesButton
     */
    public static void askForConfirmation(Context context, String title, String Dialog, String yesButton, Runnable onPositive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(Dialog);

        //Configure the positive button
        builder.setPositiveButton(yesButton, (dialog, which) -> {
            if(onPositive != null){
                onPositive.run();
            }
        });

        //Configure the negative button
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Saves measurements to the database and updates the UI
     * @param context           In which context the toast will be shown
     * @param dbHelper          The database helper to interact with the database
     * @param patientId         The ID of the patient taking the measurement
     * @param leftAngle         Left angle measurement results
     * @param rightAngle        Right angle measurement results
     * @param SaveButton        The button that will be hidden after saving

     */
    public static void saveMeasurement(Context context,
                                 DatabaseHelper dbHelper,
                                 long patientId,
                                 String measurementType,
                                 int leftAngle,
                                 int rightAngle , Button SaveButton) {

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

        //Hide the save button after saving
        SaveButton.setVisibility(View.GONE);
    }
}
