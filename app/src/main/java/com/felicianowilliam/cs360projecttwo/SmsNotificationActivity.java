// Import necessary packages for the application
package com.felicianowilliam.cs360projecttwo;


import static com.felicianowilliam.cs360projecttwo.Constants.EMULATOR_NUMBER;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

/**
 * No Longer utilized on enhancement three
 *
 * {@code SmsNotificationActivity} is responsible for managing SMS sending permissions
 * and allowing the user to send a test SMS notification.
 *
 * <p>This activity guides the user through the process of granting the necessary
 * {@link Manifest.permission#SEND_SMS} permission. It provides UI elements
 * to display the current permission status and to request the permission if it's not
 * already granted. Once the permission is granted, the user can send a test SMS
 * notification.</p>
 *
 * <p>Key functionalities include:</p>
 * <ul>
 *   <li>Checking the current SMS permission status on creation.</li>
 *   <li>Providing a button to request the SMS permission.</li>
 *   <li>Handling the result of the permission request.</li>
 *   <li>Updating the UI dynamically based on the permission status.</li>
 *   <li>Providing a button to send a test SMS notification (enabled only when permission is granted).</li>
 *   <li>Simulating the sending of an SMS and displaying the result.</li>
 *   <li>Redirecting to the {@link SigninActivity} if permissions are already granted upon launch.</li>
 *   <li>Redirecting to the {@link RegisterActivity} after a successful test SMS is sent.</li>
 * </ul>
 *
 * <p>This activity uses {@link ActivityResultLauncher} for a modern approach to
 * handling permission requests.</p>
 */
public class SmsNotificationActivity extends AppCompatActivity {

    private TextView tvPermissionStatus; // TextView to display the permission status

    private TextView tvNotificationResult; // TextView to display the result of the notification

    private MaterialButton btnRequestPermission; // Button to request SMS permission

    private MaterialButton btnSendTestNotification; // Button to send a test notification


    // Activity result launcher for requesting SMS permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =

            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

                if (isGranted) {

                    // Permission is granted. Continue the action or workflow in your app.
                    Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show();

                    updateUiBasedOnPermission(true);
                } else {

                    Toast.makeText(this, "SMS Permission Denied. Notifications disabled.", Toast.LENGTH_LONG).show();

                    updateUiBasedOnPermission(false);
                }

            });

    /**
     * Initializes the activity. This method is called when the activity is first created.
     * It sets up the user interface, checks for SMS permission, and sets up click listeners
     * for the buttons.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms_notification);

        // If SMS  permissions are already granted, redirect to SigninActivity
        if(hasSmsPermission()){
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        }


        //  Find Views by their IDs
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);

        tvNotificationResult = findViewById(R.id.tvNotificationResult);

        btnRequestPermission = findViewById(R.id.btnRequestPermission);

        btnSendTestNotification = findViewById(R.id.btnSendTestNotification);

        //  Initial Check & UI Update
        checkAndSetInitialPermissionState();

        // Set Click Listeners
        btnRequestPermission.setOnClickListener(v -> { // Listener for the request permission button

            // Launch the permission request
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        });

        btnSendTestNotification.setOnClickListener(v -> {

            // Double-check permission before attempting to "send"
            if (hasSmsPermission()) { // If SMS permission is granted

                simulateSendingNotification();
            } else {

                Toast.makeText(this, "SMS permission is required to send notifications.", Toast.LENGTH_SHORT).show();

                updateUiBasedOnPermission(false);
            }
        });
    }

    /**
     * Redirects the user to the RegisterActivity.
     * This method is typically called after a successful operation, such as sending a test SMS.
     */
    // Redirects to the target activity (RegisterActivity)
    private void redirectToTargetActivity() {

        Intent intent = new Intent(this, RegisterActivity.class);

        startActivity(intent);

        finish();
    }

    /**
     * Checks if the app has been granted the SEND_SMS permission.
     *
     * @return True if the permission is granted, false otherwise.
     */
    // Checks if SMS permission is granted
    private boolean hasSmsPermission() {

        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Checks if the SEND_SMS permission has been denied permanently by the user.
     * A permission is considered permanently denied if the user has previously denied it and
     * also checked the "Don't ask again" option.
     * @return True if the permission is denied permanently, false otherwise.
     */
    // Checks if SMS permission is denied permanently
    private boolean isPermissionDeniedPermanently() {

        return !shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)
                && !hasSmsPermission();
    }

    /**
     * Checks the initial state of the SEND_SMS permission when the activity starts
     * and updates the UI accordingly.
     */
    // Checks the initial permission state and updates the UI accordingly
    private void checkAndSetInitialPermissionState() {

        updateUiBasedOnPermission(hasSmsPermission());
    }

    /**
     * Updates the user interface elements based on whether the SEND_SMS permission is granted.
     * This includes changing the text and color of the permission status TextView,
     * and showing/hiding or enabling/disabling the permission request and send test notification buttons.
     * @param granted True if the SEND_SMS permission is granted, false otherwise.
     */
    // Updates the UI based on whether SMS permission is granted or denied
    @SuppressLint("SetTextI18n")
    private void updateUiBasedOnPermission(boolean granted) {

        if (granted) {

            tvPermissionStatus.setText("Permission Status: GRANTED");

            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_color_granted));

            btnRequestPermission.setVisibility(View.GONE); // Hide the request permission button

            btnSendTestNotification.setVisibility(View.VISIBLE); // Show the send notification button

            btnSendTestNotification.setEnabled(true); // Enable the send notification button

            // Notifications enabled message could be displayed here or in tvNotificationResult
            tvNotificationResult.setText("SMS notifications are enabled.");

        } else {

            tvPermissionStatus.setText("Permission Status: DENIED");

            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_color_denied));

            btnRequestPermission.setVisibility(View.VISIBLE); // Show the request permission button

            btnSendTestNotification.setVisibility(View.GONE); // Hide the send notification button

            tvNotificationResult.setText("SMS notifications are disabled. Grant permission to enable.");
        }
    }

    /**
     * Simulates sending an SMS notification.
     * This method constructs a predefined message and attempts to send it to the emulator's number
     * using {@link SmsManager}. It updates the UI with the result of the SMS sending attempt
     * (success or failure) and then redirects to the {@link RegisterActivity} upon successful sending.
     */
    @SuppressLint("SetTextI18n")
    private void simulateSendingNotification() {

        //  Simulate sending a notification
        String notificationMessage = "Simulated Notification: Low inventory for Item 'Gadget Pro'.";

        tvNotificationResult.setText(notificationMessage);

        Toast.makeText(this, "Simulating SMS send...", Toast.LENGTH_SHORT).show();

        // Get phone number and message from session manager
        String phoneNumber = EMULATOR_NUMBER;

        String message = "Your inventory for Gadget Pro is low.";

        try {
             SmsManager smsManager = SmsManager.getDefault();

             smsManager.sendTextMessage(phoneNumber, null, message, null, null);

             Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();

              tvNotificationResult.setText("Test SMS Sent to " + phoneNumber); // Display success message

              redirectToTargetActivity(); // Redirect to target activity after sending SMS
        } catch (Exception e) { // Catch any exceptions during SMS sending

             Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();

             tvNotificationResult.setText("SMS Sending failed: " + e.getMessage()); // Display failure message

             Log.e("SMS", "SMS Sending failed: " + e.getMessage()); // Log the error
         }

    }
}
