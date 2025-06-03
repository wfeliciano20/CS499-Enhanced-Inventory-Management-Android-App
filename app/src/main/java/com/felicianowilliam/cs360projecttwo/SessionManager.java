package com.felicianowilliam.cs360projecttwo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

/**
 * Manages user session data using SharedPreferences.
 * This class provides methods to create, retrieve, and clear user login sessions,
 * including storing and accessing user details like username, tokens, and phone number.
 * It also handles redirection to the login screen if the user is not logged in.
 */
public class SessionManager {

    // Shared Preferences file name
    private static final String PREF_NAME = "Pref";

    // Shared Preferences Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public static final String KEY_USERNAME = "username";

    public static final String KEY_TOKEN = "TOKEN";

    public static final String KEY_REFRESH_TOKEN = "REFRESH_TOKEN";

    public static final String KEY_NUMBER = "NUMBER";

    // Shared Preferences instance
    private SharedPreferences sharedPreferences;

    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Constructor
    public SessionManager(Context context) {

        this._context = context;

        sharedPreferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

        editor = sharedPreferences.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String username, String token, String refreshToken, String number) {


        // Storing login value as TRUE
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Storing username in shared preferences
        editor.putString(KEY_USERNAME, username);

        editor.putString(KEY_TOKEN, token);

        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        editor.putString(KEY_NUMBER,number);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin() {

        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, SigninActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();

        // username
        user.put(KEY_USERNAME, sharedPreferences.getString(KEY_USERNAME, null));
        // token
        user.put(KEY_TOKEN, sharedPreferences.getString(KEY_TOKEN, null));
        // refresh token
        user.put(KEY_REFRESH_TOKEN, sharedPreferences.getString(KEY_REFRESH_TOKEN, null));
        // number
        user.put(KEY_NUMBER, sharedPreferences.getString(KEY_NUMBER, null));

        // return user
        return user;
    }

    /**
     * Get username directly
     * @return username
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * Get JWT Token directly
     * @return jwt token
     */
    public String getToken(){
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Get JWT Refresh Token
     * @return refresh token
     */
    public String getRefreshToken(){
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Set JWT Token
     * @param token the new jwt token
     */
    public void setToken(String token){

        editor.putString(KEY_TOKEN, token);

        editor.commit();
    }

    /**
     * Set JWT Refresh Token
     * @param refreshToken the new refresh token
     */
    public void setRefreshToken(String refreshToken){

        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        editor.commit();
    }

    /**
     * Get Phone Number
     * @return phone number
     */
    public String getNumber(){
        return sharedPreferences.getString(KEY_NUMBER, null);
    }

    /**
     * Clear session details
     * */
    public void logoutUser() {

        // Clearing all data from Shared Preferences
        editor.clear();

        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, SigninActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
