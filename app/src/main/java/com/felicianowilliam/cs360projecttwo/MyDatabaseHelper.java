package com.felicianowilliam.cs360projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;


/**
 * Manages the application's local SQLite database for storing inventory-related data.
 *
 * This class extends {@link SQLiteOpenHelper} and provides methods for creating,
 * upgrading, and interacting with the database tables. Currently, it supports
 * a single table for storing phone numbers associated with usernames.
 *
 * Key responsibilities:
 * - Defining the database schema (table names, column names, data types).
 * - Creating the database and tables when the application is first installed.
 * - Handling database upgrades when the schema changes.
 * - Providing methods to store and retrieve data (e.g., phone numbers).
 * - Implementing basic error handling for database operations.
 *
 * The database name is "Inventory.db".
 *
 * The primary table managed by this helper is {@link #PHONE_TABLE_NAME}, which stores:
 * - {@link #PHONE_COLUMN_ID}: A unique identifier for each entry (auto-incrementing integer).
 * - {@link #PHONE_COLUMN_USERNAME}: The username associated with the phone number (text, not null).
 * - {@link #PHONE_COLUMN_NUMBER}: The phone number itself (text, not null).
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    // Database name
    private static final String DATABASE_NAME = "Inventory.db";
    // Database version
    private static final int DATABASE_VERSION = 1;

    // Phone Number Table
    private static final String PHONE_TABLE_NAME = "Phone";
    // Phone Number Table Column ID
    private static final String PHONE_COLUMN_ID = "_id";

    // Phone Number Table Column Number
    private static final String PHONE_COLUMN_NUMBER = "number";

    // Phone Number Table Column Username
    private static final String PHONE_COLUMN_USERNAME = "username";


    public MyDatabaseHelper(@Nullable Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method is called when the database is created for the first time.
     * It's where the creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createPhoneNumberTable = "CREATE TABLE " + PHONE_TABLE_NAME + " (" +
                PHONE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PHONE_COLUMN_USERNAME + " TEXT NOT NULL, " +
                PHONE_COLUMN_NUMBER + " TEXT NOT NULL);";

        db.execSQL(createPhoneNumberTable);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method will drop the existing tables and recreate them.
     * In a production environment, you would typically implement a migration strategy
     * to preserve user data.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop the existing phone number table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + PHONE_TABLE_NAME);

        // Recreate the database tables by calling onCreate
        onCreate(db);
    }

    /**
     * Stores a phone number associated with a username in the database.
     *
     * This method attempts to insert a new row into the `PHONE_TABLE_NAME` table.
     * It populates the `PHONE_COLUMN_USERNAME` with the provided username and
     * `PHONE_COLUMN_NUMBER` with the provided phone number.
     *
     * If any exception occurs during the database operation, an error message
     * is logged to LogCat, including the username for which the operation failed
     * and the stack trace of the exception.
     *
     * @param username The username of the user whose phone number is being stored.
     *                 This will be stored in the `PHONE_COLUMN_USERNAME` column.
     * @param number   The phone number to be stored. This will be stored in the
     *                 `PHONE_COLUMN_NUMBER` column.
     */
    public void StorePhoneNumber(String username, String number){

        // Try block to handle potential database exceptions
        try {

            // Get a writable database instance
            SQLiteDatabase db = this.getWritableDatabase();

            // Create ContentValues object to store column values
            ContentValues cv = new ContentValues();

            // Put username into ContentValues
            cv.put(PHONE_COLUMN_USERNAME, username);

            // Put phone number into ContentValues
            cv.put(PHONE_COLUMN_NUMBER, number);

            // Insert the values into the phone table
            db.insert(PHONE_TABLE_NAME, null, cv);
        }

        // Catch any exceptions that occur during database operations
        catch (Exception e){

            // Log error with username and stack trace
            Log.e("MyDatabaseHelper", "Error storing phone number for user: " + username, e);
        }
    }

    /**
     * Retrieves the phone number for a given username.
     * If the username is not found in the database, the returned Cursor will be non-null
     * but its `getCount()` method will return 0.
     * If an error occurs during the database operation, null will be returned.
     * @param username The username to search for.
     * @return A Cursor containing the phone number information, or null if an error occurred.
     */
    public Cursor GetPhoneNumber(String username){

        try{
            // Get a readable database instance
            SQLiteDatabase db = this.getReadableDatabase();

            // Create SQL query to select phone number for the given username
            String query = "SELECT * FROM " + PHONE_TABLE_NAME + " WHERE " + PHONE_COLUMN_USERNAME + " = ?";

            // Execute the query with username parameter and return the result cursor
            return db.rawQuery(query, new String[]{username});

        }catch (Exception e){
            // Log error if database operation fails
            Log.e("MyDatabaseHelper", "Error retrieving phone number for user: " + username, e);

            // Return null to indicate failure
            return null;
        }
    }
}
