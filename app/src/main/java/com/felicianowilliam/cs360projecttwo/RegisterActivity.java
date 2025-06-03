package com.felicianowilliam.cs360projecttwo;


import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;
import static com.felicianowilliam.cs360projecttwo.Constants.SMS_PERMISSION_CODE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
 * RegisterActivity handles user registration.
 * <ul>
 *     <li>Allows new users to create an account by providing their name, phone number, email, and password.</li>
 *     <li>Handles user input for name, phone number, email, and password.</li>
 *     <li>Validates the entered information (e.g., checks for valid email format, phone number format, and password length).</li>
 *     <li>Communicates with the {@link InventoryApiService} to submit registration details.</li>
 *     <li>If registration is successful:
 *         <ul>
 *             <li>Creates a user session using {@link SessionManager}, storing the user's name, authentication token, and refresh token.</li>
 *             <li>Stores the user's phone number locally using {@link MyDatabaseHelper}.</li>
 *             <li>Redirects the user to the {@link InventoryActivity}.</li>
 *         </ul>
 *     </li>
 *     <li>If registration fails, it displays an error message.</li>
 *     <li>Manages the `SEND_SMS` permission:
 *         <ul>
 *             <li>Checks if the permission is granted.</li>
 *             <li>If not granted, requests the permission from the user.</li>
 *             <li>If permission is denied after request, the activity may finish, as SMS features might be integral or planned.</li>
 *             <li>This permission is likely for future SMS-related features (e.g., verification, alerts), and ensuring it's granted prepares the app for these.</li>
 *         </ul>
 *     </li>
 *     <li>Provides a link to navigate to the {@link SigninActivity} for users who already have an account.</li>
 * </ul>
 */
public class RegisterActivity extends AppCompatActivity {

    // Database helper instance for local database operations
    private MyDatabaseHelper myDbHelper;

    // API service instance for network operations
    private InventoryApiService apiService;

    // TextInputLayout for name input field
    private TextInputLayout tilName;

    // TextInputLayout for phone number input field
    private TextInputLayout tilNumber;

    // TextInputLayout for email input field
    private TextInputLayout tilEmail;

    // TextInputLayout for password input field
    private TextInputLayout tilPassword;

    // TextInputEditText for name input
    private TextInputEditText editTextName;

    // TextInputEditText for phone number input
    private TextInputEditText editTextNumber;

    // TextInputEditText for email input
    private TextInputEditText editTextEmail;

    // TextInputEditText for password input
    private TextInputEditText editTextPassword;

    // Session manager for user session management
    private SessionManager sessionManager;

    /**
     * Called when the activity is first created.
     * Initializes UI components, API service, session manager, and database helper.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call parent class onCreate
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.activity_register);

        // Initialize API service
        apiService = new InventoryApiService(getApplicationContext());

        // Initialize session manager
        sessionManager = new SessionManager(getApplicationContext());

        // Initialize database helper
        myDbHelper = new MyDatabaseHelper(this);

        // Initialize name TextInputLayout
        tilName = findViewById(R.id.tilName);

        // Initialize phone number TextInputLayout
        tilNumber = findViewById(R.id.tilNumber);

        // Initialize email TextInputLayout
        tilEmail = findViewById(R.id.tilEmail);

        // Initialize password TextInputLayout
        tilPassword = findViewById(R.id.tilPassword);

        // Initialize name TextInputEditText
        editTextName = findViewById(R.id.editTextName);

        // Initialize phone number TextInputEditText
        editTextNumber = findViewById(R.id.editTextNumber);

        // Initialize email TextInputEditText
        editTextEmail = findViewById(R.id.editTextEmail);

        // Initialize password TextInputEditText
        editTextPassword = findViewById(R.id.editTextPassword);

        // Initialize register button
        MaterialButton buttonRegister = findViewById(R.id.buttonRegister);

        // Initialize sign in button
        MaterialButton buttonSignin = findViewById(R.id.buttonSignin);

        // Set click listener for register button
        buttonRegister.setOnClickListener(v -> {

            // Call method to handle registration
            handleRegistration();
        });

        // Set click listener for sign in button
        buttonSignin.setOnClickListener(v -> {

            // Show toast message
            Toast.makeText(RegisterActivity.this, "Navigate to sign in screen", Toast.LENGTH_SHORT).show();

            // Create intent for sign in activity
            Intent signinIntent = new Intent(RegisterActivity.this, SigninActivity.class);

            // Start sign in activity
            startActivity(signinIntent);

            // Finish current activity
            finish();
        });

        // Check required permissions
        checkPermissions();
    }

    /**
     * Checks if the app has the SEND_SMS permission.
     * If the permission is not granted, it requests the permission from the user.
     * This method doesn't take any inputs and doesn't return any outputs.
     * The result of the permission request is handled by the onRequestPermissionsResult callback.
     */
    private void checkPermissions() {

        // Check if SMS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Request SMS permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {

            // Log permission status
            Log.d("RegisterActivity", "All permissions granted");
        }
    }

    /**
     * Handles the user registration process.
     * It validates user input (name, phone number, email, password),
     * and if valid, calls the API service to register the user.
     * On successful registration, it stores user data, creates a session, and navigates to the InventoryActivity.
     */
    private void handleRegistration() {

        // Clear previous errors
        tilEmail.setError(null);

        tilPassword.setError(null);

        // Get name input value
        String name = Objects.requireNonNull(editTextName.getText()).toString().trim();

        // Get phone number input value
        String number = Objects.requireNonNull(editTextNumber.getText()).toString().trim();

        // Get email input value
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();

        // Get password input value
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

        // Flag for validation status
        boolean isValid = true;

        // Validate name
        if(TextUtils.isEmpty(name)){

            tilName.setError("Name is required");

            isValid = false;
        }

        // Validate phone number
        else if(TextUtils.isEmpty(number)){

            tilNumber.setError("Number is required");

            isValid = false;
        }

        // Validate phone number format
        else if(!Patterns.PHONE.matcher(number).matches()){

            tilNumber.setError("Enter a valid phone number");

            isValid = false;
        }

        // Validate email
        else if (TextUtils.isEmpty(email)) {

            tilEmail.setError("Email is required");

            isValid = false;

        }

        // Validate email format
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            tilEmail.setError("Enter a valid email address");

            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {

            tilPassword.setError("Password is required");

            isValid = false;

        }

        // Validate password length
        else if (password.length() < 6) {

            tilPassword.setError("Password must be at least 6 characters");

            isValid = false;
        }

        // Return if validation failed
        if (!isValid) {

            return;
        }

        // Call API to register user
        apiService.registerUser(name, email, password, new InventoryApiService.ApiCallback<AuthResponse>() {

            /**
             * Callback method invoked when the API call is successful.
             * This method handles the response from a successful user registration.
             * It creates a user session, stores the phone number locally, and navigates to the InventoryActivity.
             * @param result The AuthResponse object containing the authentication token and refresh token.
             * @return null, as this method does not directly return a value but handles navigation and session creation.
             */
            @Override
            public Response onSuccess(AuthResponse result) {

                // Create user session
                sessionManager.createLoginSession(name, result.getToken(), result.getRefreshToken(), number);

                // Log registration
                Log.d("RegisterActivity", "Beginning registration");

                // Store phone number in local database
                try {

                    myDbHelper.StorePhoneNumber(name, number);

                } catch (Exception e) {

                    Log.d("RegisterActivity", "Error storing phone number: " + e.getMessage());
                }

                // Create intent for inventory activity
                Intent mainIntent = new Intent(RegisterActivity.this, InventoryActivity.class);

                // Add username as extra
                mainIntent.putExtra(EXTRA_USERNAME, name);

                // Set flags to clear activity stack
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Start inventory activity
                startActivity(mainIntent);

                // Finish current activity
                finish();

                return null;
            }

            /**
             * Callback method invoked when the API call results in an error.
             * This method logs the error and displays a toast message to the user.
             * @param error A string describing the error.
             * @param statusCode The HTTP status code of the error response.
             */
            @Override
            public void onError(String error, int statusCode) {

                // Log error
                Log.d("RegisterActivity", "Registration error: " + error);

                // Show error toast
                Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_LONG).show();
            }
        });
    }
}
