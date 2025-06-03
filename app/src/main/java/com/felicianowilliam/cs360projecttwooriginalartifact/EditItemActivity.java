//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_ID;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_NAME;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_QUANTITY;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//
//import androidx.core.content.ContextCompat;
//
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.telephony.SmsManager;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import com.felicianowilliam.cs360projecttwo.InventoryActivity;
//import com.felicianowilliam.cs360projecttwo.MyDatabaseHelper;
//import com.felicianowilliam.cs360projecttwo.R;
//import com.felicianowilliam.cs360projecttwo.SessionManager;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//
//
//public class EditItemActivity extends AppCompatActivity {
//
//    private MyDatabaseHelper dbHelper;
//
//    // UI Elements
//    private Toolbar toolbar;
//    private TextInputLayout tilItemName, tilItemQuantity;
//    private TextInputEditText editTextItemName, editTextItemQuantity;
//    private MaterialButton buttonSaveItem;
//    private String usernameExtra;
//    private long itemId;
//    private SessionManager sessionManager;
//    private String itemNameToNotify;
//
//    private final ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // Permission is granted.
//                    Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show();
//                    // If we were waiting to send an SMS, send it now
//                    if (itemNameToNotify != null) {
//                        sendLowStockSms(itemNameToNotify);
//                        itemNameToNotify = null;
//                    } else {
//                        // This case shouldn't ideally happen if triggered correctly, but log just in case
//                        Log.w("EditItemActivity", "SMS permission granted via launcher, but no item name was stored.");
//                    }
//                } else {
//                    // Permission denied. Explain consequences.
//                    Toast.makeText(this, "SMS Permission Denied. Low stock notifications disabled.", Toast.LENGTH_LONG).show();
//                    itemNameToNotify = null;
//                }
//            });
//
//    private boolean hasSmsPermission() {
//        return ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void sendLowStockSms(String itemName) {
//        String phoneNumber = "1234567890"; // Emulator phone number
//
//        String message = "Inventory Alert: No more " + itemName + " left in stock.";
//
//       try {
//            if (phoneNumber == null || TextUtils.isEmpty(phoneNumber)) {
//            Toast.makeText(this, "Target phone number for SMS not configured.", Toast.LENGTH_LONG).show();
//            Log.e("EditItemActivity", "Cannot send SMS: Target phone number missing/placeholder.");
//            return;
//            }
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//            Toast.makeText(this, "Low stock notification SMS sent.", Toast.LENGTH_SHORT).show();
//            Log.i("EditItemActivity", "SMS sent to " + phoneNumber + " for item: " + itemName);
//        } catch (Exception e) {
//            Toast.makeText(this, "SMS failed to send.", Toast.LENGTH_LONG).show();
//            Log.e("EditItemActivity", "Error sending SMS", e);
//        }
//    }
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        sessionManager = new SessionManager(getApplicationContext());
//        // Use the modified layout file
//        setContentView(R.layout.activity_edit_item);
//
//        dbHelper = new MyDatabaseHelper(this);
//
//        //  Initialize UI
//        toolbar = findViewById(R.id.toolbarEdit);
//        tilItemName = findViewById(R.id.tilItemName);
//        tilItemQuantity = findViewById(R.id.tilItemQuantity);
//        editTextItemName = findViewById(R.id.editTextItemName);
//        editTextItemQuantity = findViewById(R.id.editTextItemQuantity);
//        buttonSaveItem = findViewById(R.id.buttonSaveItem);
//        usernameExtra= getIntent().getStringExtra(EXTRA_USERNAME);
//
//
//        // Setup Toolbar
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the nav button area
//            getSupportActionBar().setTitle("Edit Inventory Item");
//        }
//
//        Intent intent = getIntent();
//        itemId= intent.getLongExtra(""+EXTRA_ITEM_ID,1);
//        setTitle("Edit Inventory Item"); // Set toolbar title for editing
//        buttonSaveItem.setText("Edit Item"); // Set button text for editing
//
//        itemNameToNotify = intent.getStringExtra(EXTRA_ITEM_NAME);
//        // Populate fields with existing data
//        editTextItemName.setText(intent.getStringExtra(EXTRA_ITEM_NAME));
//        editTextItemQuantity.setText(String.valueOf(intent.getIntExtra(""+EXTRA_ITEM_QUANTITY, 2)));
//
//
//        // Set Save Button Listener
//        buttonSaveItem.setOnClickListener(v -> saveItem());
//    }
//
//    // Method to save (or update) the item
//    private void saveItem() {
//        // Clear previous errors
//        tilItemName.setError(null);
//        tilItemQuantity.setError(null);
//
//        // Get Input
//        String name = editTextItemName.getText().toString().trim();
//        String quantityStr = editTextItemQuantity.getText().toString().trim();
//
//        //  Validation
//        boolean isValid = true;
//        int quantity = 0;
//
//        if (TextUtils.isEmpty(name)) {
//            tilItemName.setError("Item name is required");
//            isValid = false;
//        }
//
//        if (TextUtils.isEmpty(quantityStr)) {
//            tilItemQuantity.setError("Quantity is required");
//            isValid = false;
//        } else {
//            try {
//                quantity = Integer.parseInt(quantityStr);
//                if (quantity < 0) {
//                    tilItemQuantity.setError("Quantity cannot be negative");
//                    isValid = false;
//                }
//            } catch (NumberFormatException e) {
//                tilItemQuantity.setError("Invalid number format");
//                isValid = false;
//            }
//        }
//
//        // Stop if validation fails
//        if (!isValid) {
//            return;
//        }
//
//
//        if(dbHelper.updateINVENTORYItem(itemId, name, quantity)){
//            if (quantity == 0) {
//                Log.d("EditItemActivity", "Quantity is zero for item: " + name);
//                itemNameToNotify = name; // Store name *before* checking/requesting permission
//
//                if (hasSmsPermission()) {
//                    // Permission already granted, send directly
//                    Log.d("EditItemActivity", "SMS permission already granted. Sending SMS.");
//                    sendLowStockSms(itemNameToNotify);
//                    itemNameToNotify = null; // Clear after use
//                } else {
//                    // Permission not granted, launch the request using the launcher
//                    Log.d("EditItemActivity", "SMS permission not granted. Launching request.");
//                    // The result will be handled by the requestPermissionLauncher callback
//                    requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
//                }
//            }
//            Intent inventoryIntent = new Intent(EditItemActivity.this, InventoryActivity.class);
//            inventoryIntent.putExtra(EXTRA_USERNAME, usernameExtra);
//            startActivity(inventoryIntent);
//            finish();
//        }else{
//            Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Toolbar Menu Handling
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Reuse the same menu as the inventory list
//        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//       if (id == R.id.logout) {
//            sessionManager.logoutUser();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//}
//
