//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.telephony.SmsManager;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.felicianowilliam.cs360projecttwo.R;
//import com.felicianowilliam.cs360projecttwo.RegisterActivity;
//import com.google.android.material.button.MaterialButton;
//
//public class SmsNotificationActivity extends AppCompatActivity {
//
//    private TextView tvPermissionStatus;
//    private TextView tvNotificationResult;
//    private MaterialButton btnRequestPermission;
//    private MaterialButton btnSendTestNotification;
//
//
//    private final ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // Permission is granted. Continue the action or workflow in your app.
//                    Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show();
//                    updateUiBasedOnPermission(true);
//                } else {
//                    Toast.makeText(this, "SMS Permission Denied. Notifications disabled.", Toast.LENGTH_LONG).show();
//                    updateUiBasedOnPermission(false);
//                }
//
//            });
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sms_notification);
//
//        //  Find Views
//        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
//        tvNotificationResult = findViewById(R.id.tvNotificationResult);
//        btnRequestPermission = findViewById(R.id.btnRequestPermission);
//        btnSendTestNotification = findViewById(R.id.btnSendTestNotification);
//
//        //  Initial Check & UI Update
//        checkAndSetInitialPermissionState();
//
//        // Set Click Listeners
//        btnRequestPermission.setOnClickListener(v -> {
//            // Launch the permission request
//            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
//        });
//
//        btnSendTestNotification.setOnClickListener(v -> {
//            // Double-check permission before attempting to "send"
//            if (hasSmsPermission()) {
//                simulateSendingNotification();
//            } else {
//                Toast.makeText(this, "SMS permission is required to send notifications.", Toast.LENGTH_SHORT).show();
//
//                updateUiBasedOnPermission(false);
//            }
//        });
//    }
//
//    private void redirectToTargetActivity() {
//    Intent intent = new Intent(this, RegisterActivity.class);
//    startActivity(intent);
//    finish();
//}
//
//    private boolean hasSmsPermission() {
//        return ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private boolean isPermissionDeniedPermanently() {
//        return !shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)
//                && !hasSmsPermission();
//    }
//
//    private void checkAndSetInitialPermissionState() {
//        updateUiBasedOnPermission(hasSmsPermission());
//    }
//
//    @SuppressLint("SetTextI18n")
//    private void updateUiBasedOnPermission(boolean granted) {
//        if (granted) {
//            tvPermissionStatus.setText("Permission Status: GRANTED");
//            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_color_granted));
//            btnRequestPermission.setVisibility(View.GONE); // Hide request button
//            btnSendTestNotification.setVisibility(View.VISIBLE); // Show send button
//            btnSendTestNotification.setEnabled(true); // Ensure send button is enabled
//            // Notifications enabled message could be displayed here or in tvNotificationResult
//            tvNotificationResult.setText("SMS notifications are enabled.");
//
//        } else {
//            tvPermissionStatus.setText("Permission Status: DENIED");
//            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_color_denied));
//            btnRequestPermission.setVisibility(View.VISIBLE); // Show request button
//            btnSendTestNotification.setVisibility(View.GONE); // Hide send button (or disable)
//            tvNotificationResult.setText("SMS notifications are disabled. Grant permission to enable.");
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private void simulateSendingNotification() {
//
//        //  SIMULATION
//        String notificationMessage = "Simulated Notification: Low inventory for Item 'Gadget Pro'.";
//        tvNotificationResult.setText(notificationMessage);
//        Toast.makeText(this, "Simulating SMS send...", Toast.LENGTH_SHORT).show();
//
//
//        String phoneNumber = "1234567890";
//        String message = "Your inventory for Gadget Pro is low.";
//        try {
//             SmsManager smsManager = SmsManager.getDefault();
//             smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//             Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
//              tvNotificationResult.setText("Test SMS Sent to " + phoneNumber);
//              redirectToTargetActivity();
//        } catch (Exception e) {
//             Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
//             tvNotificationResult.setText("SMS Sending failed: " + e.getMessage());
//             Log.d("SMS", "SMS Sending failed: " + e.getMessage());
//         }
//
//    }
//}
