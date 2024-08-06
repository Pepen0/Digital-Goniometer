package com.example.goniometer;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.opencsv.CSVWriter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class DataExtractionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private DatabaseHelper dbHelper;
    private TextView textViewCSVData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_extraction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        Button buttonDataExtract = findViewById(R.id.buttonDataExtract);
        buttonDataExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportToCSV();
            }

        });

        Button buttonShowPatientData = findViewById(R.id.buttonShowPatientData);
        buttonShowPatientData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showCSVPatients(); }

        });


        textViewCSVData = findViewById(R.id.textViewCSVData);
        if (textViewCSVData == null) {
            Log.e("MainActivity", "Data in TextView is null");
        }

    }

    private void showCSVPatients() {
        Cursor cursor = dbHelper.getCSVData();
        StringBuilder CSVData = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                for (int i=0; i < cursor.getColumnCount(); i++) {
                    CSVData.append(cursor.getColumnName(i)).append(": ").append(cursor.getString(i)).append("\n");
                }
                CSVData.append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        textViewCSVData.setText(CSVData.toString());
    }

    private void exportToCSV() {
        Cursor cursor = dbHelper.getCSVData();

        String fileName = "exportData.csv";
        Uri csvUri = createCSVFileUri(this, fileName);
        if (csvUri == null) {
            Log.e("MainActivity", "Failed to create CSV file URI");
            return;
        }
        try (OutputStream outputStream = getContentResolver().openOutputStream(csvUri);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
             CSVWriter csvWriter = new CSVWriter(outputStreamWriter)) {

            csvWriter.writeNext(cursor.getColumnNames());

            while (cursor.moveToNext()) {
                String[] row = new String[cursor.getColumnCount()];
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    row[i] = cursor.getString(i);
                }
                csvWriter.writeNext(row);
            }
            Log.i("MainActivity", "CSV file created successfully");
        } catch (Exception e) {
            Log.e("MainActivity", "Error writing CSV file", e);
        } finally {
            cursor.close();
        }

    }

    private Uri createCSVFileUri(Context context, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Data Extraction Export");
        } else {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "Data Extraction Export");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, fileName);
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        }
        return context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
    }
}