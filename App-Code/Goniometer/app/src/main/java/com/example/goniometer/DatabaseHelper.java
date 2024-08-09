package com.example.goniometer;

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
    private static final int DATABASE_VERSION = 2; // Incremented for schema changes

    public static final String TABLE_PATIENTS = "patients";
    public static final String KEY_ID = "id";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";

    public static final String TABLE_MEASUREMENTS = "measurements";
    public static final String KEY_MEASUREMENT_ID = "measurement_id";
    public static final String KEY_PATIENT_ID = "patient_id";
    public static final String KEY_MEASUREMENT_TYPE = "measurement_type";
    public static final String KEY_LEFT_ANGLE = "left_angle";
    public static final String KEY_RIGHT_ANGLE = "right_angle";
    public static final String KEY_TIMESTAMP = "timestamp";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");

        String CREATE_PATIENTS_TABLE = "CREATE TABLE " + TABLE_PATIENTS +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_FIRST_NAME + " TEXT," +
                KEY_LAST_NAME + " TEXT" +
                ")";

        String CREATE_MEASUREMENTS_TABLE = "CREATE TABLE " + TABLE_MEASUREMENTS +
                "(" +
                KEY_MEASUREMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_PATIENT_ID + " INTEGER," +
                KEY_MEASUREMENT_TYPE + " TEXT," +
                KEY_LEFT_ANGLE + " REAL," +
                KEY_RIGHT_ANGLE + " REAL," +
                KEY_TIMESTAMP + " TEXT," +
                "FOREIGN KEY(" + KEY_PATIENT_ID + ") REFERENCES " + TABLE_PATIENTS + "(" + KEY_ID + ")" +
                ")";

        try {
            db.execSQL(CREATE_PATIENTS_TABLE);
            db.execSQL(CREATE_MEASUREMENTS_TABLE);
            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENTS);
            onCreate(db);
            Log.d(TAG, "Database upgraded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        }
    }

    public long addPatient(String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_FIRST_NAME, firstName);
            values.put(KEY_LAST_NAME, lastName);

            id = db.insert(TABLE_PATIENTS, null, values);
            if (id == -1) {
                Log.e(TAG, "Failed to insert patient: " + firstName + " " + lastName);
            } else {
                Log.d(TAG, "Patient added with ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding patient: " + e.getMessage());
        } finally {
            db.close();
        }
        return id;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patientList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PATIENTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Check column indices
                int idIndex = cursor.getColumnIndex(KEY_ID);
                int firstNameIndex = cursor.getColumnIndex(KEY_FIRST_NAME);
                int lastNameIndex = cursor.getColumnIndex(KEY_LAST_NAME);

                // Log column index issues
                if (idIndex == -1) {
                    Log.e(TAG, "Column " + KEY_ID + " does not exist.");
                }
                if (firstNameIndex == -1) {
                    Log.e(TAG, "Column " + KEY_FIRST_NAME + " does not exist.");
                }
                if (lastNameIndex == -1) {
                    Log.e(TAG, "Column " + KEY_LAST_NAME + " does not exist.");
                }

                // Read data if columns exist
                if (idIndex != -1 && firstNameIndex != -1 && lastNameIndex != -1) {
                    do {
                        long id = cursor.getLong(idIndex);
                        String firstName = cursor.getString(firstNameIndex);
                        String lastName = cursor.getString(lastNameIndex);
                        Patient patient = new Patient(id, firstName, lastName);
                        patientList.add(patient);
                    } while (cursor.moveToNext());
                }
            } else {
                Log.d(TAG, "No patients found or cursor is null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving patients: " + e.getMessage());
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
        try {
            int rowsAffected = db.delete(TABLE_PATIENTS, KEY_ID + " = ?", new String[]{String.valueOf(patientId)});
            if (rowsAffected > 0) {
                db.delete(TABLE_MEASUREMENTS, KEY_PATIENT_ID + " = ?", new String[]{String.valueOf(patientId)});
                Log.d(TAG, "Patient and associated measurements deleted successfully");
            } else {
                Log.e(TAG, "Failed to delete patient with ID: " + patientId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    public long addMeasurement(long patientId, String measurementType, int leftAngle, int rightAngle, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PATIENT_ID, patientId);
            values.put(KEY_MEASUREMENT_TYPE, measurementType);
            values.put(KEY_LEFT_ANGLE, leftAngle);
            values.put(KEY_RIGHT_ANGLE, rightAngle);
            values.put(KEY_TIMESTAMP, timestamp);

            id = db.insert(TABLE_MEASUREMENTS, null, values);
            if (id == -1) {
                Log.e(TAG, "Failed to insert measurement for patient ID: " + patientId);
            } else {
                Log.d(TAG, "Measurement added with ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding measurement: " + e.getMessage());
        } finally {
            db.close();
        }
        return id;
    }

    public String getPatientNameById(long patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String fullName = null;

        try {
            // Query to select first name and last name of the patient by ID
            String query = "SELECT " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME +
                    " FROM " + TABLE_PATIENTS +
                    " WHERE " + KEY_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

            if (cursor != null && cursor.moveToFirst()) {
                String firstName = cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME));
                fullName = firstName + " " + lastName;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving patient name for ID: " + patientId + " - " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return fullName;
    }


    public List<Measurement> getMeasurementsForPatient(long patientId) {
        List<Measurement> measurementList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE " + KEY_PATIENT_ID + " = " + patientId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Check column indices
                int measurementIdIndex = cursor.getColumnIndex(KEY_MEASUREMENT_ID);
                int measurementTypeIndex = cursor.getColumnIndex(KEY_MEASUREMENT_TYPE);
                int leftAngleIndex = cursor.getColumnIndex(KEY_LEFT_ANGLE);
                int rightAngleIndex = cursor.getColumnIndex(KEY_RIGHT_ANGLE);
                int timestampIndex = cursor.getColumnIndex(KEY_TIMESTAMP);

                // Log column index issues
                if (measurementIdIndex == -1) {
                    Log.e(TAG, "Column " + KEY_MEASUREMENT_ID + " does not exist.");
                }
                if (measurementTypeIndex == -1) {
                    Log.e(TAG, "Column " + KEY_MEASUREMENT_TYPE + " does not exist.");
                }
                if (leftAngleIndex == -1) {
                    Log.e(TAG, "Column " + KEY_LEFT_ANGLE + " does not exist.");
                }
                if (rightAngleIndex == -1) {
                    Log.e(TAG, "Column " + KEY_RIGHT_ANGLE + " does not exist.");
                }
                if (timestampIndex == -1) {
                    Log.e(TAG, "Column " + KEY_TIMESTAMP + " does not exist.");
                }

                // Read data if columns exist
                if (measurementIdIndex != -1 && measurementTypeIndex != -1 && leftAngleIndex != -1 && rightAngleIndex != -1 && timestampIndex != -1) {
                    do {
                        long id = cursor.getLong(measurementIdIndex);
                        String measurementType = cursor.getString(measurementTypeIndex);
                        int leftAngle = cursor.getInt(leftAngleIndex);
                        int rightAngle = cursor.getInt(rightAngleIndex);
                        String timestamp = cursor.getString(timestampIndex);
                        Measurement measurement = new Measurement(id, patientId, measurementType, leftAngle, rightAngle, timestamp);
                        measurementList.add(measurement);
                    } while (cursor.moveToNext());
                } else {
                    Log.e(TAG, "One or more columns are missing.");
                }
            } else {
                Log.d(TAG, "No measurements found or cursor is null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving measurements for patient ID: " + patientId + " - " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return measurementList;
    }

    public Cursor getCSVData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PATIENTS;
        return db.rawQuery(query, null);
    }

}
