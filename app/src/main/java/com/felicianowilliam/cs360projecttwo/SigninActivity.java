package com.felicianowilliam.cs360projecttwo;


import androidx.appcompat.app.AppCompatActivity;

import static com.felicianowilliam.cs360projecttwo.Constants.EMULATOR_NUMBER;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import okhttp3.Response;

/**
 * SigninActivity handles user authentication.
 * It allows users to sign in with their email and password.
 * <ul>
 *     <li>Handles user input for email and password.</li>
 *     <li>Validates the entered credentials.</li>
 *     <li>Communicates with the {@link InventoryApiService} to authenticate the user.</li>
 *     <li>If authentication is successful:
 *         <ul>
 *             <li>Checks if a phone number is stored locally for the user.</li>
 *             <li>If no phone number is found, it prompts the user to enter one for SMS alerts regarding low stock items.</li>
 *             <li>Creates a user session using {@link SessionManager}.</li>
 *             <li>Redirects the user to the {@link InventoryActivity}.</li>
 *         </ul>
 *     </li>
 *     <li>If authentication fails, it displays an error message.</li>
 *     <li>Provides a link to navigate to the {@link RegisterActivity} for new users.</li>
 * </ul>
 */
public class SigninActivity extends AppCompatActivity {

    // Database helper instance for local storage operations
    private MyDatabaseHelper myDbHelper;

    // UI elements for email input field layout
    private TextInputLayout tilEmailSignin;
    
    // UI elements for password input field layout
    private TextInputLayout tilPasswordSignin;

    // UI element for email input field
    private TextInputEditText editTextEmailSignin;
    
    // UI element for password input field
    private TextInputEditText editTextPasswordSignin;

    // UI button for sign in action
    private MaterialButton buttonSigninAction;
    
    // UI button/link for registration
    private MaterialButton buttonRegisterLink;

    // Session manager for handling user sessions
    private SessionManager sessionManager;

    // Dialog for phone number input
    private AlertDialog phoneNumberDialog;

    // API service for authentication
    private InventoryApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call parent class onCreate
        super.onCreate(savedInstanceState);

        // Set the content view from layout resource
        setContentView(R.layout.activity_signin);

        // Initialize session manager
        sessionManager = new SessionManager(getApplicationContext());

        // Initialize database helper
        myDbHelper =  new MyDatabaseHelper(this);

        // Initialize API service
        apiService = new InventoryApiService(getApplicationContext());

        // Initialize email input layout
        tilEmailSignin = findViewById(R.id.tilEmailSignin);

        // Initialize password input layout
        tilPasswordSignin = findViewById(R.id.tilPasswordSignin);

        // Initialize email input field
        editTextEmailSignin = findViewById(R.id.editTextEmailSignin);

        // Initialize password input field
        editTextPasswordSignin = findViewById(R.id.editTextPasswordSignin);

        // Initialize sign in button
        buttonSigninAction = findViewById(R.id.buttonSigninAction);

        // Initialize register link button
        buttonRegisterLink = findViewById(R.id.buttonRegisterLink);

        // Set click listener for sign in button
        buttonSigninAction.setOnClickListener(v -> {

            // Call method to handle sign in logic
            handleSignin();
        });

        // Set click listener for the Register button/link
        buttonRegisterLink.setOnClickListener(v -> {

            // Navigate to your RegisterActivity
            Intent registerIntent = new Intent(SigninActivity.this, RegisterActivity.class);

            startActivity(registerIntent);

            finish();
        });
    }

    /**
     *    This method manages the user sign-in workflow:
     *
     *   <h3>Input Validation</h3>
     *   <ul>
     *       <li>Clears previous error messages</li>
     *       <li>Validates email format and presence</li>
     *       <li>Validates password presence</li>
     *   </ul>
     *
     *   <h3>Authentication Flow</h3>
     *   <ol>
     *       <li>Makes API call to authenticate user</li>
     *       <li>On success:
     *           <ul>
     *               <li>Checks for stored phone number</li>
     *               <li>Prompts for phone number if missing</li>
     *               <li>Creates user session</li>
     *               <li>Navigates to inventory screen</li>
     *           </ul>
     *       </li>
     *       <li>On failure: Displays error message</li>
     *   </ol>
     * 
     */
    private void handleSignin() {

        // Clear previous error messages from email field
        tilEmailSignin.setError(null);

        // Clear previous error messages from password field
        tilPasswordSignin.setError(null);

        // Get email input value and trim whitespace
        String email = Objects.requireNonNull(editTextEmailSignin.getText()).toString().trim();

        // Get password input value and trim whitespace
        String password = Objects.requireNonNull(editTextPasswordSignin.getText()).toString().trim();

        // Flag to track validation status
        boolean isValid = true;

        // Validate email is not empty
        if (TextUtils.isEmpty(email)) {

            // Set error message for empty email
            tilEmailSignin.setError("Email is required");

            // Mark validation as failed
            isValid = false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            // Set error message for invalid email format
            tilEmailSignin.setError("Enter a valid email address");

            // Mark validation as failed
            isValid = false;
        }

        // Validate password is not empty
        if (TextUtils.isEmpty(password)) {

            // Set error message for empty password
            tilPasswordSignin.setError("Password is required");

            // Mark validation as failed
            isValid = false;
        }

        // Exit if validation failed
        if (!isValid) {

            return;
        }

        // Make API call to authenticate user
        apiService.loginUser(email,password,new InventoryApiService.ApiCallback<AuthResponse>() {

            /**
             * Callback method invoked when the API call for user authentication is successful.
             * <p>
             * This method handles the logic after a user successfully signs in. It performs the following steps:
             * <ol>
             *     <li>Retrieves the user's phone number from the local database ({@link MyDatabaseHelper}).</li>
             *     <li>If the phone number is not found:
             *         <ul>
             *             <li>A dialog is shown to prompt the user to enter their phone number for SMS alerts about low stock items.</li>
             *             <li>If the user provides a valid phone number, it's stored in the local database and a {@link SessionManager} session is created with this number.</li>
             *             <li>If the user dismisses the dialog or doesn't provide a number, a session is created with a default phone number ({@link Constants#EMULATOR_NUMBER}).</li>
             *         </ul>
             *     </li>
             *     <li>If the phone number is found in the database, a {@link SessionManager} session is created with the retrieved number.</li>
             *     <li>Finally, it navigates the user to the {@link InventoryActivity}.</li>
             * </ol>
             * In case of any exceptions during phone number retrieval or storage, an error is logged, and a "Sign In Failed" toast message is displayed.
             * @param result The {@link AuthResponse} object containing the authentication result (username, token, refresh token).
             * @return Always returns null, as the Response object is not directly used in this callback.
             */
            @Override
            public Response onSuccess(AuthResponse result) {

                // Variable to store phone number
                String number = "";

                try {
                    // Query database for user's phone number
                    Cursor cursor = myDbHelper.GetPhoneNumber(result.getUserName());

                    // Check if phone number exists in database
                    if(cursor.getCount() == 0 ){

                        // Close cursor if no data found
                        cursor.close();

                        // Build dialog to request phone number
                        AlertDialog.Builder builder = new AlertDialog.Builder(SigninActivity.this);

                        // Set dialog title
                        builder.setTitle("Enter Phone Number For Low Stock Items SMS");

                        // Create input field for phone number
                        final TextInputEditText input = new TextInputEditText(SigninActivity.this);

                        // Add input field to dialog
                        builder.setView(input);

                        // Set positive button action
                        builder.setPositiveButton("OK", (dialog, which) -> {

                            // Get entered phone number
                            String phoneNumber = Objects.requireNonNull(input.getText()).toString();

                            // Validate phone number format
                            if (!TextUtils.isEmpty(phoneNumber) && !Patterns.PHONE.matcher(phoneNumber).matches()) {

                                // Store phone number in database
                                myDbHelper.StorePhoneNumber(result.getUserName(), phoneNumber);

                                // Create login session with phone number
                                sessionManager.createLoginSession(result.getUserName(), result.getToken(), result.getRefreshToken(), phoneNumber);

                                // Navigate to inventory screen
                                navigateToInventory(result.getUserName());

                                // Log successful sign-in
                                Log.d("SigninActivity", "user with email: " + email + " and phone number: " + phoneNumber + " now stored in database;");

                            } else {

                                // Show error message for invalid phone number
                                Toast.makeText(getApplicationContext(),"Please Sign in again an enter a phone number for the text alerts for low stock items", Toast.LENGTH_LONG).show();
                            }

                        });

                        // Set dialog dismiss listener
                        builder.setOnDismissListener(dialog -> {

                            // Handle case where dialog is dismissed without entering phone number
                            if (sessionManager.getNumber() == null) {

                                // Create session with default emulator number
                                sessionManager.createLoginSession(result.getUserName(), result.getToken(), result.getRefreshToken(), EMULATOR_NUMBER);
                            }

                            // Navigate to inventory screen
                            navigateToInventory(result.getUserName());
                        });

                        // Create and show dialog
                        phoneNumberDialog = builder.create();

                        phoneNumberDialog.show();

                    } else {

                        // Handle case where phone number exists in database
                        if(cursor.getCount() != 0 && cursor.moveToFirst() ){

                            // Get phone number from cursor
                            number = cursor.getString(0);

                            // Close cursor
                            cursor.close();

                            // Create login session with retrieved phone number
                            sessionManager.createLoginSession(result.getUserName(), result.getToken(), result.getRefreshToken(), number);

                            // Navigate to inventory screen
                            navigateToInventory(result.getUserName());

                            // Log successful sign-in
                            Log.d("SigninActivity", "user with email: " + email + " and phone number: " + number + " found in database;");
                        }

                    }
                }catch (Exception e){

                    // Log and handle errors
                    Log.e("SigninActivity", "Error getting/setting phone number", e);

                    // Show error message
                    Toast.makeText(getApplicationContext(), "Sign In Failed", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            /**
             * Callback method invoked when the API call for user authentication fails.
             * <p>
             * This method handles the scenario where user sign-in fails. It displays a "Sign In Failed" toast
             * message to the user and logs the error details, including the error message and status code,
             * for debugging purposes.
             * @param error A string describing the error that occurred.
             * @param statusCode The HTTP status code associated with the error (if applicable).
             *
             */
            @Override
            public void onError(String error, int statusCode) {

                // Show error message for failed authentication
                Toast.makeText(getApplicationContext(), "Sign In Failed", Toast.LENGTH_LONG).show();

                // Log error details
                Log.e("SigninActivity", "Error during sign in: " + error);
            }
        });

    }

    /**
     * Navigates to the InventoryActivity after successful sign-in.
     *
     *<p> This method creates an Intent to start the {@link InventoryActivity}.
     * It passes the signed-in user's username as an extra in the Intent.
     * To prevent the user from navigating back to the SigninActivity using the back button
     * after a successful login, this method clears the activity stack.
     * Finally, it starts the InventoryActivity and finishes the current SigninActivity.</p>
     *
     * @param userName The username of the successfully signed-in user.
     */
    private void navigateToInventory(String userName) {

        // Create an Intent to start the InventoryActivity
        Intent inventoryIntent = new Intent(SigninActivity.this, InventoryActivity.class);

        // Add the username as an extra to the Intent
        inventoryIntent.putExtra(EXTRA_USERNAME, userName);

        // Clear the activity stack to prevent back navigation to SigninActivity
        inventoryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Start the InventoryActivity
        startActivity(inventoryIntent);

        // Finish the current SigninActivity
        finish();
    }



    /**
     * <p>Called as part of the activity lifecycle when the activity is being destroyed.
     * Performs cleanup tasks including dismissing any showing dialogs to prevent window leaks.
     * Always calls the superclass's implementation first.</p>
     */
    @Override
    protected void onDestroy() {

        // Call the parent class's onDestroy method
        super.onDestroy();

        // Check if the phone number dialog exists and is currently showing
        if (phoneNumberDialog != null && phoneNumberDialog.isShowing()) {

            // Dismiss the dialog to prevent window leaks
            phoneNumberDialog.dismiss();
        }
    }
}

