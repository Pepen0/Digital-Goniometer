package com.example.goniometer;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
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
}
