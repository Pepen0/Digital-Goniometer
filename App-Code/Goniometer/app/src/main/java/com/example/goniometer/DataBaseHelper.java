package com.example.goniometer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "profiles.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PROFILE = "Profile";
    private static final String COLUMN_PATIENT_ID = "ProfileID";
    private static final String COLUMN_FIRSTNAME = "Name";
    private static final String COLUMN_LASTNAME = "Surname";

public DataBaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
}

@Override
public void onCreate(SQLiteDatabase db) {
    String createProfileTable = "CREATE TABLE " + TABLE_PROFILE + " (" +
            COLUMN_PATIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_FIRSTNAME + " TEXT," +
            COLUMN_LASTNAME + " TEXT)";

    db.execSQL(createProfileTable);

}
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        onCreate(db);
    }
// Insert Profile
public boolean insertProfile(Profile profile) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(COLUMN_FIRSTNAME, profile.getFirstName());
    contentValues.put(COLUMN_LASTNAME, profile.getLastName());

    long result = db.insert(TABLE_PROFILE, null, contentValues);
    return result != -1;
}

// Check if profile ID exists
public boolean isProfileIDExists(int patientID) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROFILE + " WHERE " + COLUMN_PATIENT_ID + " = ?", new String[]{String.valueOf(patientID)});
    boolean exists = (cursor.getCount() > 0);
    cursor.close();
    return exists;
}

// Get all profiles
public List<Profile> getAllProfiles() {
    List<Profile> profiles = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROFILE, null);

    if (cursor.moveToFirst()) {
        do {
            profiles.add(new Profile(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            ));
        } while (cursor.moveToNext());
    }
    cursor.close();
    return profiles;
}

// Get a single profile by ID
public Profile getProfile(int profileID) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.query(TABLE_PROFILE,
            new String[]{COLUMN_PATIENT_ID, COLUMN_FIRSTNAME, COLUMN_LASTNAME},
            COLUMN_PATIENT_ID + "=?",
            new String[]{String.valueOf(profileID)},
            null, null, null, null);

    if (cursor != null) {
        cursor.moveToFirst();
        Profile profile = new Profile(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2)
        );
        cursor.close();
        return profile;
    } else {
        return null;
    }
}
}
