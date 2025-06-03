//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//
//import com.felicianowilliam.cs360projecttwo.SigninActivity;
//
//import java.util.HashMap;
//
//public class SessionManager {
//
//    // Shared Preferences file name
//    private static final String PREF_NAME = "Pref";
//
//    // Shared Preferences Keys
//    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
//    public static final String KEY_USERNAME = "username";
//
//    // Shared Preferences instance
//    private SharedPreferences pref;
//
//    // Editor for Shared preferences
//    private SharedPreferences.Editor editor;
//
//    // Context
//    private Context _context;
//
//    // Shared pref mode
//    private int PRIVATE_MODE = 0;
//
//    // Constructor
//    public SessionManager(Context context) {
//        this._context = context;
//        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
//        editor = pref.edit();
//    }
//
//    /**
//     * Create login session
//     * */
//    public void createLoginSession(String username) {
//        // Storing login value as TRUE
//        editor.putBoolean(KEY_IS_LOGGED_IN, true);
//
//        // Storing username in pref
//        editor.putString(KEY_USERNAME, username);
//
//        // commit changes
//        editor.commit();
//    }
//
//    /**
//     * Check login method will check user login status
//     * If false it will redirect user to login page
//     * Else won't do anything
//     * */
//    public void checkLogin() {
//        // Check login status
//        if (!this.isLoggedIn()) {
//            // user is not logged in redirect him to Login Activity
//            Intent i = new Intent(_context, SigninActivity.class);
//
//            // Closing all the Activities from stack
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            // Add new Flag to start new Activity
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            // Staring Login Activity
//            _context.startActivity(i);
//
//
//        }
//
//    }
//
//    /**
//     * Get stored session data
//     * */
//    public HashMap<String, String> getUserDetails() {
//        HashMap<String, String> user = new HashMap<>();
//        // username
//        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
//
//        // return user
//        return user;
//    }
//
//    /**
//     * Get username directly
//     */
//    public String getUsername() {
//        return pref.getString(KEY_USERNAME, null);
//    }
//
//
//    /**
//     * Clear session details
//     * */
//    public void logoutUser() {
//        // Clearing all data from Shared Preferences
//        editor.clear();
//        editor.commit();
//
//        // After logout redirect user to Login Activity
//        Intent i = new Intent(_context, SigninActivity.class);
//
//        // Closing all the Activities
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        // Staring Login Activity
//        _context.startActivity(i);
//    }
//
//    /**
//     * Quick check for login
//     * **/
//    // Get Login State
//    public boolean isLoggedIn() {
//        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
//    }
//}
