package com.example.goniometer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "PatientDatabase";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_PATIENTS = "patients";
    public static final String KEY_ID = "id";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PATIENTS_TABLE = "CREATE TABLE " + TABLE_PATIENTS +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_FIRST_NAME + " TEXT," +
                KEY_LAST_NAME + " TEXT" +
                ")";
        db.execSQL(CREATE_PATIENTS_TABLE);
        Log.d(TAG, "Database table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        onCreate(db);
    }

    public long addPatient(String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FIRST_NAME, firstName);
        values.put(KEY_LAST_NAME, lastName);

        long id = db.insert(TABLE_PATIENTS, null, values);
        db.close();
        return id;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patientList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PATIENTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                    @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME));
                    @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME));
                    Patient patient = new Patient(id, firstName, lastName);
                    patientList.add(patient);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return patientList;
    }

    public void deletePatient(long patientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PATIENTS, KEY_ID + " = ?", new String[]{String.valueOf(patientId)});
        db.close();
    }
}
