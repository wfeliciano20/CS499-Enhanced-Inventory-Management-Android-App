//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//
//public class MyDatabaseHelper extends SQLiteOpenHelper {
//
//    private Context context;
//
//    private static final String DATABASE_NAME = "Inventory.db";
//    private static final int DATABASE_VERSION = 1;
//
//    // Authentication Table
//    private static final String AUTH_TABLE_NAME = "Authentication";
//    private static final String AUTH_COLUMN_ID = "_id";
//    private static final String AUTH_COLUMN_USERNAME = "username";
//    private static final String AUTH_COLUMN_PASSWORD = "password";
//
//    // Inventory Table
//    private static final String INVENTORY_TABLE_NAME = "Inventory";
//    private static final String INVENTORY_COLUMN_ID = "_id";
//    private static final String INVENTORY_COLUMN_ITEM_NAME = "item_name";
//    private static final String INVENTORY_COLUMN_QUANTITY = "quantity";
//    private static final String INVENTORY_COLUMN_USER_ID = "user_id";
//
//    public MyDatabaseHelper(@Nullable Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        this.context = context;
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        // Create Authentication Table
//        String createAuthTable = "CREATE TABLE " + AUTH_TABLE_NAME + " (" +
//                AUTH_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                AUTH_COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
//                AUTH_COLUMN_PASSWORD + " TEXT NOT NULL);";
//
//        // Create Inventory Table with Foreign Key Reference
//        String createInventoryTable = "CREATE TABLE " + INVENTORY_TABLE_NAME + " (" +
//                INVENTORY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                INVENTORY_COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
//                INVENTORY_COLUMN_QUANTITY + " INTEGER NOT NULL, " +
//                INVENTORY_COLUMN_USER_ID + " INTEGER NOT NULL, " +
//                "FOREIGN KEY (" + INVENTORY_COLUMN_USER_ID + ") REFERENCES " +
//                AUTH_TABLE_NAME + " (" + AUTH_COLUMN_ID + ") ON DELETE CASCADE);";
//
//
//        db.execSQL(createAuthTable);
//        db.execSQL(createInventoryTable);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // Drop old tables and recreate them
//        db.execSQL("DROP TABLE IF EXISTS " + INVENTORY_TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + AUTH_TABLE_NAME);
//        onCreate(db);
//    }
//
//    public boolean registerUser(String username, String password) {
//        //  Validation
//        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
//            Log.w("MyDatabaseHelper", "Registration attempt with null or empty username/password.");
//            Toast.makeText(context, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        SQLiteDatabase db;
//        Cursor cursor = null;
//
//        try {
//            // Check if username already exists
//            db = this.getReadableDatabase();
//            cursor = getUserByUsername(username);
//
//            if (cursor != null && cursor.getCount() > 0) {
//                // Username already exists
//                Log.w("MyDatabaseHelper", "Registration failed: Username '" + username + "' already exists.");
//                Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//            // Close the cursor used for checking right away
//            if (cursor != null) {
//                cursor.close();
//                cursor = null;
//            }
//
//            // register user
//            db = this.getWritableDatabase();
//            ContentValues cv = new ContentValues();
//            cv.put(AUTH_COLUMN_USERNAME, username);
//            cv.put(AUTH_COLUMN_PASSWORD, password);
//
//            long result = db.insert(AUTH_TABLE_NAME, null, cv);
//
//            // registration failed
//            if (result == -1) {
//                Log.e("MyDatabaseHelper", "Database insertion failed for username: " + username);
//                Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
//                return false;
//            } else {
//                // Registration successful
//                Log.i("MyDatabaseHelper", "User '" + username + "' registered successfully.");
//                Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//
//        } catch (Exception e) {
//            // Unexpected error during registration
//            Log.e("MyDatabaseHelper", "Error during registration for user: " + username, e);
//            Toast.makeText(context, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
//            return false;
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//    }
//
//
//    // Your existing getUserByUsername remains the same:
//    public Cursor getUserByUsername(String username) {
//        SQLiteDatabase db = this.getReadableDatabase(); // Good practice to get readable here
//
//        if (username == null || username.trim().isEmpty()) {
//            Log.w("MyDatabaseHelper", "getUserByUsername called with null or empty username.");
//            return null; // Return null if username is invalid
//        }
//
//        String query = "SELECT * FROM " + AUTH_TABLE_NAME + " WHERE " + AUTH_COLUMN_USERNAME + " = ?";
//
//        if (db != null) {
//            try {
//                return db.rawQuery(query, new String[]{username.trim()}); // Trim username before query
//            } catch (Exception e) {
//                Log.e("MyDatabaseHelper", "Error querying user by username: " + username, e);
//                return null; // Return null on query error
//            }
//        } else {
//            Log.e("MyDatabaseHelper", "Failed to get readable database in getUserByUsername.");
//            return null;
//        }
//    }
//
//
//    public boolean login(String username, String password) {
//        //  Validation
//        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
//            Log.w("MyDatabaseHelper", "login attempt with null or empty username/password.");
//            Toast.makeText(context, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        Cursor cursor = getUserByUsername(username);
//        if (cursor != null && cursor.moveToFirst()) {
//            // Retrieve stored password
//            String storedPassword;
//            int index = cursor.getColumnIndex(AUTH_COLUMN_PASSWORD);
//            if(index >= 0){
//                storedPassword = cursor.getString(index);
//                cursor.close();
//                return storedPassword.equals(password);
//            }else{
//                return false;
//            }
//        }
//        // User not found
//        return false;
//    }
//
//    public boolean insertINVENTORYItem(String itemName, int quantity, int userId) {
//        //  Validation
//        if (itemName == null || itemName.trim().isEmpty() || quantity <= 0 || userId <= 0) {
//            Log.w("MyDatabaseHelper", "Add Item attempt with invalid item name quantity or usedId.");
//            Toast.makeText(context, "itemName cannot be empty, quantity must be greater than 0, and userId must be greater than 0", Toast.LENGTH_LONG).show();
//            return false;
//        }
//        Log.d("MyDatabaseHelper", "Inserting item: " + itemName + ", Quantity: " + quantity + ", User ID: " + userId);
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(INVENTORY_COLUMN_ITEM_NAME, itemName);
//        cv.put(INVENTORY_COLUMN_QUANTITY, quantity);
//        cv.put(INVENTORY_COLUMN_USER_ID, userId);
//        long result = db.insert(INVENTORY_TABLE_NAME,null, cv);
//        if(result == -1){
//            Toast.makeText(context, "Failed To Add Item To Inventory", Toast.LENGTH_SHORT).show();
//            return false;
//        }else {
//            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//    }
//
//
//    public Cursor getAllItemsByUserId(int userId) {
//        //  Validation
//        if (userId <= 0) {
//            Log.w("MyDatabaseHelper", "getAllItemsByUserId attempt with invalid userId.");
//            Toast.makeText(context, "userId must be greater than 0", Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        String query = "SELECT * FROM " + INVENTORY_TABLE_NAME + " WHERE " + INVENTORY_COLUMN_USER_ID + " = ?";
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        if (db != null) {
//            return db.rawQuery(query, new String[]{String.valueOf(userId)});
//        }
//        return null;
//    }
//
//
//    public boolean updateINVENTORYItem(long row_id, String itemName, int quantity)
//    {
//        //  Validation
//        if (itemName == null || itemName.trim().isEmpty() || quantity <= 0 || row_id <= 0) {
//            Log.w("MyDatabaseHelper", "Update Item attempt with invalid item name quantity or row_id.");
//            Toast.makeText(context, "itemName cannot be empty, quantity must be greater than 0, and row_id must be greater than 0", Toast.LENGTH_LONG).show();
//            return false;
//        }
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put(INVENTORY_COLUMN_ITEM_NAME, itemName);
//        cv.put(INVENTORY_COLUMN_QUANTITY, quantity);
//
//        long result = db.update(INVENTORY_TABLE_NAME, cv, "_id=?", new String[]{""+row_id});
//        if(result == -1){
//            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
//            return false;
//        }else {
//            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//    }
//
//    public boolean deleteOneRow(String rowId){
//        if(rowId == null || rowId.trim().isEmpty()){
//            Log.w("MyDatabaseHelper", "Delete Item attempt with invalid rowId.");
//            Toast.makeText(context, "rowId cannot be empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        SQLiteDatabase db = this.getWritableDatabase();
//        long result = db.delete(INVENTORY_TABLE_NAME, "_id=?", new String[]{rowId});
//        if(result == -1){
//            Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
//            return false;
//        }else{
//            Toast.makeText(context, "Successfully Deleted.", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//    }
//
//    public void deleteAllData(){
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("DELETE FROM " + INVENTORY_TABLE_NAME);
//    }
//
//
//}
