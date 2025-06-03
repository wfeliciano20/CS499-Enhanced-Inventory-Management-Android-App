package com.felicianowilliam.cs360projecttwo;


import static com.felicianowilliam.cs360projecttwo.Constants.EMULATOR_NUMBER;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_ID;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_NAME;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_QUANTITY;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher; 
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import okhttp3.Response;


/**
 * EditItemActivity handles the modification of an existing inventory item.
 * It allows users to update the name and quantity of a selected item.
 * <ul>
 *     <li>Pre-populates input fields with the current details of the item being edited.</li>
 *     <li>Handles user input for the item's name and quantity.</li>
 *     <li>Validates the entered data:
 *         <ul>
 *             <li>Ensures the item name is not empty.</li>
 *             <li>Ensures the quantity is a non-negative integer.</li>
 *         </ul>
 *     </li>
 *     <li>Communicates with the {@link InventoryApiService} to update the item details on the backend.</li>
 *     <li>If the item's quantity is updated to zero:
 *         <ul>
 *             <li>Checks for SMS sending permissions.</li>
 *             <li>If permission is not granted, it requests the {@link Manifest.permission#SEND_SMS} permission using an {@link ActivityResultLauncher}.</li>
 *             <li>If permission is granted (either previously or after the request), it attempts to send an SMS notification about the low stock using {@link SmsManager} to the phone number stored in {@link SessionManager}.</li>
 *         </ul>
 *     </li>
 *     <li>Upon successful item update:
 *         <ul>
 *             <li>Navigates the user back to the {@link InventoryActivity}.</li>
 *         </ul>
 *     </li>
 *     <li>If the update fails, it displays an error message to the user.</li>
 *     <li>Provides a toolbar with a back navigation button and a logout option.</li>
 *     <li>Uses {@link SessionManager} to manage user session information, including the authentication token and the phone number for SMS notifications.</li>
 * </ul>
 */
public class EditItemActivity extends AppCompatActivity {

    // Service for API calls to update inventory items
    private InventoryApiService apiService;

    // Toolbar for navigation and actions
    private Toolbar toolbar;

    // Layout containers for item name and quantity input fields
    private TextInputLayout tilItemName, tilItemQuantity;

    // Input fields for editing item details
    private TextInputEditText editTextItemName, editTextItemQuantity;

    // Button to save edited item
    private MaterialButton buttonSaveItem;

    // Stores username passed from previous activity
    private String userNameExtra;

    // ID of the item being edited
    private String itemId;

    // Manages user session and preferences
    private SessionManager sessionManager;

    // Stores item name for SMS notification when quantity reaches zero
    private String itemNameToNotify;

    // Launcher for requesting SMS permission from user
    private final ActivityResultLauncher<String> requestPermissionLauncher =

            // Register callback for permission request result
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

                // Check if permission was granted
                if (isGranted) {

                    // Show success message
                    Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show();

                    // If we have an item waiting for notification
                    if (itemNameToNotify != null) {

                        // Send the low stock SMS
                        sendLowStockSms(itemNameToNotify);

                        // Clear the stored item name
                        itemNameToNotify = null;
                    } else {

                        // Log unexpected case where permission was granted but no item to notify
                        Log.w("EditItemActivity", "SMS permission granted via launcher, but no item name was stored.");
                    }
                } else {

                    // Show permission denied message
                    Toast.makeText(this, "SMS Permission Denied. Low stock notifications disabled.", Toast.LENGTH_LONG).show();

                    // Clear the stored item name
                    itemNameToNotify = null;
                }
            });

    /**
     * Checks if the app has been granted the {@link Manifest.permission#SEND_SMS} permission.
     *
     * This method uses {@link ContextCompat#checkSelfPermission(android.content.Context, String)}
     * to determine if the SEND_SMS permission is granted.
     *
     * No inputs are directly passed as parameters, but it uses the current Activity context (`this`).
     *
     * @return {@code true} if the SEND_SMS permission has been granted, {@code false} otherwise.
     */
    private boolean hasSmsPermission() {

        // Returns true if SEND_SMS permission is granted
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Sends an SMS notification for an item that is out of stock.
     * The SMS is sent to the user's phone number (retrieved from {@link SessionManager})
     * and also to a predefined emulator number (from {@link Constants#EMULATOR_NUMBER}) for testing.
     *
     * @param itemName The name of the item that is out of stock. This is included in the SMS message.
     *                 No output (void method), but has side effects: sends SMS messages and displays Toasts.
     */
    private void sendLowStockSms(String itemName) {

        // Get phone number from session
        String phoneNumber = sessionManager.getNumber();

        // Get emulator number from constants
        String emulatorNumber = EMULATOR_NUMBER;

        // Create message content
        String message = "Inventory Alert: No more " + itemName + "'s left in stock.";

        try {

            // Check if phone number is valid
            if (phoneNumber == null || TextUtils.isEmpty(phoneNumber)) {

                // Show error if no phone number
                Toast.makeText(this, "Target phone number for SMS not configured.", Toast.LENGTH_LONG).show();

                // Log the error
                Log.e("EditItemActivity", "Cannot send SMS: Target phone number missing/placeholder.");

                return;
            }

            // Format phone number for US
            String usaPhoneNumber = phoneNumber.startsWith("+1") ? phoneNumber : "+1" + phoneNumber;

            // Get SMS manager instance
            SmsManager smsManager = SmsManager.getDefault();

            // Send SMS to user's phone
            smsManager.sendTextMessage(usaPhoneNumber, null, message, null, null);

            // Send SMS to emulator for testing
            smsManager.sendTextMessage(emulatorNumber,null,message,null,null);

            // Show success message
            Toast.makeText(this, "Low stock notification SMS sent.", Toast.LENGTH_SHORT).show();

            // Log successful SMS to user
            Log.i("EditItemActivity", "SMS sent to " + usaPhoneNumber + " for item: " + itemName);

            // Log successful SMS to emulator
            Log.i("EditItemActivity", "SMS sent to " + emulatorNumber + " for item: " + itemName);

        } catch (Exception e) {

            // Show error message if SMS fails
            Toast.makeText(this, "SMS failed to send.", Toast.LENGTH_LONG).show();

            // Log the exception
            Log.e("EditItemActivity", "Error sending SMS", e);
        }
    }


    /**
     * Called when the activity is first created.
     * This method initializes the activity's layout, toolbar, input fields, and other UI elements.
     * It also retrieves data passed from the previous activity via an Intent, such as the item ID,
     * item name, and item quantity to be edited.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call parent onCreate
        super.onCreate(savedInstanceState);

        // Initialize session manager
        sessionManager = new SessionManager(getApplicationContext());

        // Set content view from layout
        setContentView(R.layout.activity_edit_item);

        // Initialize API service
        apiService = new InventoryApiService(getApplicationContext());

        // Find and initialize toolbar
        toolbar = findViewById(R.id.toolbarEdit);

        // Find input layouts
        tilItemName = findViewById(R.id.tilItemName);
        
        tilItemQuantity = findViewById(R.id.tilItemQuantity);

        // Find input fields
        editTextItemName = findViewById(R.id.editTextItemName);

        editTextItemQuantity = findViewById(R.id.editTextItemQuantity);

        // Find save button
        buttonSaveItem = findViewById(R.id.buttonSaveItem);

        // Get username from intent
        userNameExtra= getIntent().getStringExtra(EXTRA_USERNAME);

        // Set toolbar as action bar
        setSupportActionBar(toolbar);

        // Configure action bar
        if (getSupportActionBar() != null) {

            // Enable back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Set title
            getSupportActionBar().setTitle("Edit Inventory Item");
        }

        // Get intent that started this activity
        Intent intent = getIntent();

        // Get item ID from intent
        itemId= intent.getStringExtra(EXTRA_ITEM_ID);

        // Set activity title
        setTitle("Edit Inventory Item");

        // Set button text
        buttonSaveItem.setText("Edit Item");

        // Get item name for potential notification
        itemNameToNotify = intent.getStringExtra(EXTRA_ITEM_NAME);

        // Set existing item name in field
        editTextItemName.setText(intent.getStringExtra(EXTRA_ITEM_NAME));

        // Set existing quantity in field
        editTextItemQuantity.setText(String.valueOf(intent.getIntExtra(""+EXTRA_ITEM_QUANTITY, 2)));

        // Set click listener for save button
        buttonSaveItem.setOnClickListener(v -> saveItem());
    }


    /**
     * Saves the edited item.
     * <p>
     * This method validates the input fields (item name and quantity).
     * If validation passes, it calls the API to update the item.
     * If the updated item quantity is zero, it attempts to send an SMS notification
     * for low stock, requesting permission if necessary.
     * On successful update, it navigates back to the InventoryActivity.
     * Displays error messages via Toasts and logs errors if the update fails.
     * </p>
     */
    private void saveItem() {

        // Clear previous errors
        tilItemName.setError(null);

        tilItemQuantity.setError(null);

        // Get input values
        String name = Objects.requireNonNull(editTextItemName.getText()).toString().trim();

        String quantityStr = Objects.requireNonNull(editTextItemQuantity.getText()).toString().trim();

        // Validation flag
        boolean isValid = true;

        // Initialize quantity
        int quantity = 0;

        // Validate name
        if (TextUtils.isEmpty(name)) {

            tilItemName.setError("Item name is required");

            isValid = false;
        }

        // Validate quantity
        if (TextUtils.isEmpty(quantityStr)) {

            tilItemQuantity.setError("Quantity is required");

            isValid = false;
        } else {
            try {
                // Parse quantity
                quantity = Integer.parseInt(quantityStr);

                // Check for negative quantity
                if (quantity < 0) {

                    tilItemQuantity.setError("Quantity cannot be negative");

                    isValid = false;
                }

            } catch (NumberFormatException e) {

                tilItemQuantity.setError("Invalid number format");

                isValid = false;
            }
        }

        // Return if validation failed
        if (!isValid) {
            return;
        }

        // Call API to update item
        apiService.updateInventoryItem(itemId, name, quantity, new InventoryApiService.ApiCallback<InventoryItem>() {

            /**
             * Called when the API request to update the item is successful.
             * If the updated quantity is zero, it checks for SMS permission and attempts to send
             * a low stock notification.
             * Navigates back to the InventoryActivity.
             *
             * @param result The {@link InventoryItem} object returned by the API, representing the updated item.
             * @return null (This method is part of an interface and its return type is determined by the interface,
             *         but in this context, it doesn't need to return a value other than null).
             */
            @Override
            public Response onSuccess(InventoryItem result) {

                // Check if quantity is zero
                if(result.getQuantity() == 0){

                    // Store item name for notification
                    itemNameToNotify = name;

                    // Check SMS permission
                    if (hasSmsPermission()) {

                        Log.d("EditItemActivity", "SMS permission already granted. Sending SMS.");

                        sendLowStockSms(itemNameToNotify);

                        itemNameToNotify = null;

                    } else {

                        Log.d("EditItemActivity", "SMS permission not granted. Launching request.");

                        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                    }
                }

                // Log successful update
                Log.d("edit Activity", "Item with name: " + result.getName() + " and quantity: " + result.getQuantity() + " updated successfully");
                
                // Create intent to return to inventory
                Intent inventoryIntent = new Intent(EditItemActivity.this, InventoryActivity.class);
                
                // Pass username to Inventory Activity
                inventoryIntent.putExtra(EXTRA_USERNAME, sessionManager.getUsername());

                startActivity(inventoryIntent);
                
                finish();

                return null;
            }

            /**
             * Called when the API request to update the item fails.
             * Displays an error message to the user and logs the error.
             *
             * @param error A string describing the error.
             * @param statusCode The HTTP status code of the error response, or -1 if not applicable.
             */
            @Override
            public void onError(String error, int statusCode) {

                // Show error message to user
                Toast.makeText(getApplicationContext(), "Error updating item", Toast.LENGTH_SHORT).show();

                // Log the error for debugging
                Log.e("EditItemActivity", "Error updating item: " + error);
            }

        });

    }


    /**
     * Initializes the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate toolbar menu
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    /**
     * This method is called when a menu item is selected.
     *
     * @param item The selected menu item.
     * @return True if the event was handled, false otherwise.
     */ // Handles menu item selection
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Get selected item ID
        int id = item.getItemId();

        // Handle logout
        if (id == R.id.logout) {

            sessionManager.logoutUser();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

