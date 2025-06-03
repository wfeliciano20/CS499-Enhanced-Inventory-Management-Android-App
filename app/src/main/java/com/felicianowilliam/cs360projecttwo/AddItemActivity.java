package com.felicianowilliam.cs360projecttwo;


import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;


import android.content.Intent;

import android.os.Bundle;

import android.text.TextUtils;

import android.util.Log;

import android.view.Menu;

import android.view.MenuItem;

import android.widget.Toast;


import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;


import com.google.android.material.button.MaterialButton;

import com.google.android.material.textfield.TextInputEditText;

import com.google.android.material.textfield.TextInputLayout;


import java.util.Objects;


import okhttp3.Response;


/**
 * Activity for adding a new inventory item.
 * <p>
 * This activity provides a user interface for inputting the name and quantity of a new inventory item.
 * It validates the input and then saves the item to the backend API.
 * The user must be logged in to access this functionality.
 * </p>
 */
public class AddItemActivity extends AppCompatActivity {

    // Service for API calls related to inventory
    private InventoryApiService apiService;

    // Layout containers for input fields
    private TextInputLayout tilItemName, tilItemQuantity;

    // Input fields for item details
    private TextInputEditText editTextItemName, editTextItemQuantity;

    // Button to save the item
    private MaterialButton buttonSaveItem;

    // Manager for user session handling
    private SessionManager sessionManager;

    /**
     * Called when the activity is first created.
     * This method initializes the activity's layout, UI elements, and sets up event listeners.
     * It also retrieves the username passed via an Intent and validates it.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call parent class onCreate
        super.onCreate(savedInstanceState);

        // Initialize session manager
        sessionManager = new SessionManager(getApplicationContext());

        // Set the layout for this activity
        setContentView(R.layout.activity_add_item);

        // Initialize API service
        apiService = new InventoryApiService(getApplicationContext());

        // Find and initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAdd);

        // Initialize input layouts
        tilItemName = findViewById(R.id.tilItemName);

        tilItemQuantity = findViewById(R.id.tilItemQuantity);

        // Initialize input fields
        editTextItemName = findViewById(R.id.editTextItemName);

        editTextItemQuantity = findViewById(R.id.editTextItemQuantity);

        // Initialize save button
        buttonSaveItem = findViewById(R.id.buttonSaveItem);

        // Set toolbar as action bar
        setSupportActionBar(toolbar);

        // Configure toolbar if available
        if (getSupportActionBar() != null) {

            // Enable back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Set title
            getSupportActionBar().setTitle("Add Inventory Item");

            // Set title color
            toolbar.setTitleTextColor(getResources().getColor(R.color.dark_purple_text));

            // Set navigation icon color
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.dark_purple_text));

        }

        // Get username from intent
        Intent intent = getIntent();

        String username = intent.getStringExtra(EXTRA_USERNAME);

        Log.d("AddItemActivity", "Username received: " + username);

        // Validate username
        if (username == null || username.isEmpty()) {

            Log.e("AddItemActivity", "Username not passed correctly via Intent extra: " + EXTRA_USERNAME);

            Toast.makeText(this, "Error: Cannot determine user. Unable to add item.", Toast.LENGTH_LONG).show();

            finish();

            return;

        }

        // Set activity title
        setTitle("Add Inventory Item");

        // Set button text
        buttonSaveItem.setText("Add Item");

        // Set click listener for save button
        buttonSaveItem.setOnClickListener(v -> saveItem());

    }

    /**
     * Saves a new inventory item.
     *
     * This method performs the following steps:
     * 1. Clears any previous error messages displayed on the input fields.
     * 2. Retrieves the item name and quantity from the respective EditText fields.
     * 3. Validates the input:
     *    - Ensures the item name is not empty.
     *    - Ensures the quantity is not empty.
     *    - Ensures the quantity is a valid non-negative integer.
     * 4. If validation fails, appropriate error messages are displayed on the input fields,
     *    and the method returns without further processing.
     * 5. If validation is successful, it calls the `apiService.createInventoryItem` method
     *    to create the new inventory item via an API call.
     * 6. Handles the API call response:
     *    - On success:
     *        - Navigates back to the `InventoryActivity`.
     *        - Passes the current username as an extra in the intent.
     *        - Logs the successful addition of the item.
     *        - Finishes the current `AddItemActivity`.
     *    - On error:
     *        - Displays a Toast message indicating an error occurred.
     *        - Logs the error details.
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

        int quantity = 0;


        // Validate item name
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

                quantity = Integer.parseInt(quantityStr);

                if (quantity < 0) {

                    tilItemQuantity.setError("Quantity cannot be negative");

                    isValid = false;

                }

            } catch (NumberFormatException e) {

                tilItemQuantity.setError("Invalid number format");

                isValid = false;

            }
            
        }

        // Stop if validation fails
        if (!isValid) {

            return;

        }

        // Call API to create item
        apiService.createInventoryItem(name, quantity, new InventoryApiService.ApiCallback<InventoryItem>() {

            /**
             * Callback method invoked when the API call to create an inventory item is successful.
             * This method handles the response from the API, which includes the newly created inventory item.
             * It then navigates the user back to the InventoryActivity, passing the username
             * to maintain the user's context. Finally, it finishes the current AddItemActivity.
             *
             * @param result The {@link InventoryItem} object representing the newly created item,
             *               returned by the API.
             * @return null. This method is part of an interface that expects a Response, but in this
             *         specific implementation, a null return is appropriate as the success handling
             *         involves UI navigation and logging rather than returning a direct HTTP response object.
             */
            @Override
            public Response onSuccess(InventoryItem result) {
                // Return to inventory activity on success
                Intent resultIntent = new Intent(AddItemActivity.this, InventoryActivity.class);
                resultIntent.putExtra(EXTRA_USERNAME, sessionManager.getUsername());
                startActivity(resultIntent);
                Log.d("AddItemActivity", "Item with name: " + result.getName() + " and quantity: " + result.getQuantity() + " added successfully");
                finish();
                return null;
            }

            /**
             * Callback method invoked when the API call to create an inventory item fails.
             * It displays an error message to the user and logs the error details.
             *
             * @param error A string describing the error that occurred.
             * @param statusCode The HTTP status code associated with the error (e.g., 400, 500).
             *                   This parameter might not always be present or relevant depending on the nature of the error.
             */
            @Override
            public void onError(String error, int statusCode) {

                // Show error message
                Toast.makeText(getApplicationContext(), "Error adding item", Toast.LENGTH_SHORT).show();

                Log.e("AddItemActivity", "Error adding item: " + error);

            }

        });

    }

    /**
     * Initializes the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * <p>This method inflates the menu resource (defined in {@code R.menu.toolbar_menu})
     * into the provided {@code Menu} object. This populates the options menu with
     * items defined in the XML file.</p>
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu from XML
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;

    }

    /**
     * Handles the selection of items from the options menu in the toolbar.
     *
     * <p>This method is called when an item in the options menu is selected.
     * It checks the ID of the selected item and performs the corresponding action.
     * Currently, it handles the "logout" action.</p>
     *
     * @param item The menu item that was selected. Cannot be null.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        // Handle logout action
        if (id == R.id.logout) {

            sessionManager.logoutUser();

            return true;

        }

        return super.onOptionsItemSelected(item);

    }

}
