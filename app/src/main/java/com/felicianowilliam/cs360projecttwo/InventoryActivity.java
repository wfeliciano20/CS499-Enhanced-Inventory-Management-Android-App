package com.felicianowilliam.cs360projecttwo;

import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_ID;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_NAME;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_QUANTITY;
import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

/**
 * Activity to display and manage the user's inventory.
 * <p>
 * This activity handles:
 * <ul>
 *     <li>Displaying a list of inventory items for the logged-in user.</li>
 *     <li>Allowing the user to add new items to their inventory.</li>
 *     <li>Allowing the user to edit existing inventory items.</li>
 *     <li>Allowing the user to delete inventory items.</li>
 *     <li>Handling user logout.</li>
 * </ul>
 * It uses a {@link RecyclerView} to display the inventory items and a
 * {@link FloatingActionButton} to initiate adding a new item.
 * User authentication is managed by {@link SessionManager}.
 * Inventory data is fetched from a remote API via {@link InventoryApiService}.
 */
public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {

    // RecyclerView to display inventory items
    private RecyclerView recyclerViewInventory;
    
    // Adapter for the RecyclerView
    private InventoryAdapter inventoryAdapter;
    
    // List to hold inventory items
    private List<InventoryItem> inventoryItemList;
    
    // Floating action button for adding new items
    private FloatingActionButton fabAddItem;
    
    // Toolbar at the top of the activity
    private Toolbar toolbar;

    // Current logged in username
    private String currentUsername;
    
    // Session manager for user authentication
    private SessionManager sessionManager;
    
    // API service for inventory operations
    private InventoryApiService apiService;

    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the UI, and loads inventory items.
     * Checks if the user is logged in, and redirects to the sign-in screen if not.
     * Also handles cases where the username might be missing or invalid.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize session manager instance
        sessionManager = new SessionManager(getApplicationContext());

        // Initialize API service instance
        apiService = new InventoryApiService(getApplicationContext());

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {

            Log.w("InventoryActivity", "User not logged in. Redirecting to Signin.");

            // Create intent to redirect to signin activity
            Intent i = new Intent(InventoryActivity.this, SigninActivity.class);
            
            // Clear back stack
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            // Start signin activity
            startActivity(i);
            
            // Finish current activity
            finish();

            return;
        }

        // Set content view from layout
        setContentView(R.layout.activity_inventory);

        // Initialize toolbar from layout
        toolbar = findViewById(R.id.toolbar);
        
        // Initialize RecyclerView from layout
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        
        // Initialize FAB from layout
        fabAddItem = findViewById(R.id.fabAddItem);

        // Get username from intent extras
        String usernameExtra = getIntent().getStringExtra(EXTRA_USERNAME);
        
        // Fallback to session manager if not in intent
        if (usernameExtra == null) {

            usernameExtra = sessionManager.getUsername();
        }

        // Set current username from session
        currentUsername = sessionManager.getUsername();
        
        // Ensure username is not null
        usernameExtra = usernameExtra == null ? sessionManager.getUsername() : usernameExtra;

        // Final fallback for username
        if (currentUsername == null) {

            currentUsername = usernameExtra;
        }

        // Validate username exists
        if (currentUsername == null || currentUsername.isEmpty()) {

            Log.e("InventoryActivity", "Username is NULL even after login check. Logging out.");
            
            // Show error toast
            Toast.makeText(this, "Session error. Logging out.", Toast.LENGTH_LONG).show();
            
            // Logout user
            sessionManager.logoutUser();
            
            // Close activity
            finish();

            return;
        }

        // Initialize empty inventory list
        inventoryItemList = new ArrayList<>();

        // Set toolbar as action bar
        setSupportActionBar(toolbar);
        
        // Set toolbar title with username
        if (getSupportActionBar() != null) {

            getSupportActionBar().setTitle(currentUsername+"'s Inventory");
        }

        // Setup RecyclerView layout
        int numberOfColumns = 1;
        
        // Set grid layout manager
        recyclerViewInventory.setLayoutManager(new GridLayoutManager(this,numberOfColumns));
        
        // Initialize adapter with click listener
        inventoryAdapter = new InventoryAdapter(this, inventoryItemList, this);
        
        // Set adapter on RecyclerView
        recyclerViewInventory.setAdapter(inventoryAdapter);

        // Load initial inventory items
        loadInventoryItems();

        // Set click listener for FAB
        fabAddItem.setOnClickListener(view -> {
            
            // Create intent for add item activity
            Intent addIntent = new Intent(this, AddItemActivity.class);
            
            // Pass username to add activity
            addIntent.putExtra(EXTRA_USERNAME, currentUsername);
            
            // Start activity for result
            this.startActivityForResult(addIntent,1);
        });
    }

    /**
     * Loads inventory items from the API and updates the UI.
     *
     * This method performs the following steps:
     * <ol>
     *     <li>Logs the current authentication token for debugging purposes.</li>
     *     <li>Calls the `getAllInventoryItems` method of the `apiService`.</li>
     *     <li>On successful API response:</li>
     *      <ol>
     *        <li>
     *         Clears the existing items from the `inventoryItemList`
     *        </li>
     *        <li>
     *            Adds the newly fetched inventory items to `inventoryItemList`.
     *        </li>
     *        <li>
     *            Notifies the `inventoryAdapter` that the underlying data has changed,
     *            triggering a UI refresh to display the updated list.
     *        </li>
     *      </ol>
     *     <li>On API error:</li>
     *      <ol>
     *         <li>
     *             Displays a Toast message to the user indicating that there was an error fetching the inventory.
     *
     *         </li>
     *      </ol>
     * </ol>
     */
    private void loadInventoryItems() {
        // Log current token for debugging
        Log.i("Inventory Activity", sessionManager.getToken());

        // Call API to get all inventory items
        apiService.getAllInventoryItems(new InventoryApiService.ApiCallback<List<InventoryItem>>() {
            /**
             * Callback method invoked when the API call to fetch all inventory items is successful.
             * This method processes the list of {@link InventoryItem} objects returned by the API.
             *
             * <p>The method performs the following actions:
             * <ol>
             *     <li>Clears the existing items from the {@code inventoryItemList}.</li>
             *     <li>Adds all the inventory items from the {@code result} (the list fetched from the API)
             *         to the {@code inventoryItemList}.</li>
             *     <li>Notifies the {@code inventoryAdapter} that the underlying data set has changed,
             *         which triggers a refresh of the UI to display the updated inventory.</li>
             * </ol>
             *
             * @param result A {@link List} of {@link InventoryItem} objects representing the inventory
             *               items successfully fetched from the API.
             * @return {@code null}. The return value is not used in this implementation.
             */
            @Override
            public Response onSuccess(List<InventoryItem> result) {
                // Clear existing items
                inventoryItemList.clear();
                // Add new items
                inventoryItemList.addAll(result);
                // Notify adapter of data change
                inventoryAdapter.notifyDataSetChanged();
                return null;
            }

            /**
             * Callback method invoked when an API call fails or returns an error.
             * This method handles the error by displaying a Toast message to the user
             * and logging the error details.
             *
             * <p>The method performs the following actions:
             * <ol>
             *     <li>Displays a Toast message "Error fetching inventory!" to the user.</li>
             *     <li>Logs an error message to the console containing the error description
             *         and the HTTP status code.</li>
             * </ol>
             * @param error A {@link String} describing the error that occurred.
             * @param statusCode The HTTP status code associated with the error (e.g., 404, 500).
             */
            @Override
            public void onError(String error, int statusCode) {
                // Show error toast
                Toast.makeText(getApplicationContext(), "Error fetching inventory!", Toast.LENGTH_SHORT).show();
                Log.e("InventoryActivity", "Error fetching inventory: " + error + " with status code: " + statusCode);
            }
        });
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.
     *
     * In this specific implementation, it checks if the result is from the
     * "add item" activity (identified by `requestCode == 1`). If it is,
     * the current activity is recreated to reflect any changes made (e.g.,
     * a new item being added to a list).
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *             (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Check if result is from add item activity
        if (requestCode == 1) {

            // Refresh activity
            recreate();

        }
    }

    /**
     * Called when the activity will start interacting with the user.
     * At this point your activity is at the top of the activity stack,
     * with user input focused on it.
     *
     * <p>This method performs the following actions:
     * <ol>
     *     <li>Calls the superclass's {@code onResume} method.</li>
     *     <li>Logs a debug message indicating that {@code onResume} was called and that inventory is being reloaded.</li>
     *     <li>Checks if {@code currentUsername} is not null and not empty.
     *         <ul>
     *             <li>If true, it clears the {@code inventoryItemList} and calls {@link #loadInventoryItems()} to refresh the inventory data.</li>
     *             <li>If false, it logs an error message indicating that the username is null or empty and inventory cannot be reloaded.</li>
     *         </ul>
     *     </li>
     * </ol>
     * No explicit inputs or outputs beyond the standard Android lifecycle behavior.
     */
    @Override
    protected void onResume() {

        super.onResume();

        // Log resume event
        Log.d("InventoryActivity", "onResume called. Reloading inventory.");

        // Check username exists
        if (currentUsername != null && !currentUsername.isEmpty()) {

            // Clear existing items
            inventoryItemList.clear();
            
            // Reload items
            loadInventoryItems();

        } else {

            Log.e("InventoryActivity", "onResume: Username is null or empty, cannot reload inventory.");

        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate toolbar menu
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    /**
     * This method is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to let the
     * normal processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate). You can use this method for any item handling
     * that you want to insert first.
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     *
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Get selected menu item ID
        int id = item.getItemId();

        // Handle logout menu item
        if (id == R.id.logout) {
            // Logout user
            sessionManager.logoutUser();
            
            // Close activity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles the click event for the edit button on an inventory item.
     *
     * This method is called when the user clicks the "Edit" button associated with an
     * {@link InventoryItem} in the list. It creates an {@link Intent} to start the
     * {@link EditItemActivity}, passing the necessary item details and the current username
     * as extras. The activity is started for a result, allowing the calling activity
     * to receive updates if the item is modified.
     *
     * @param item The {@link InventoryItem} that the user wants to edit.
     */
    @Override
    public void onEditClick(InventoryItem item) {
        // Create intent for edit activity
        Intent editIntent = new Intent(this, EditItemActivity.class);
        
        // Pass username to edit activity
        editIntent.putExtra(EXTRA_USERNAME, getIntent().getStringExtra(EXTRA_USERNAME));
        
        // Pass item details to edit activity
        editIntent.putExtra(EXTRA_ITEM_ID, item.getId());
        editIntent.putExtra(EXTRA_ITEM_NAME, item.getName());
        editIntent.putExtra(String.valueOf(EXTRA_ITEM_QUANTITY), item.getQuantity());
        
        // Start activity for result
        this.startActivityForResult(editIntent,1);
    }

    /**
     * Handles the click event for deleting an inventory item.
     * <p>
     * This method is called when the delete button/action for an {@link InventoryItem} is triggered.
     * It initiates an API call to delete the specified item from the backend.
     * <p>
     * Upon successful deletion:
     * <ul>
     *     <li>A log message is recorded.</li>
     *     <li>The local list of inventory items ({@code inventoryItemList}) is cleared.</li>
     *     <li>The inventory items are reloaded from the API ({@link #loadInventoryItems()}).</li>
     *     <li>A success toast message is displayed to the user.</li>
     * </ul>
     * <p>
     * In case of an error during deletion:
     * <ul>
     *     <li>If the item is not found (HTTP status code 404), a specific "Item not found" toast is shown.</li>
     *     <li>For other errors, a generic "Error deleting item" toast is displayed.</li>
     *     <li>Error details are logged for debugging purposes.</li>
     * </ul>
     *
     * @param item The {@link InventoryItem} to be deleted.
     */
    @Override
    public void onDeleteClick(InventoryItem item) {

        /**
         * Handles the API call for deleting an inventory item.
         *
         * This is an anonymous inner class implementing {@link InventoryApiService.ApiCallback}
         * to handle the asynchronous response of the {@code deleteInventoryItem} API call.
         *
         * {@code onSuccess} method:
         *  - Called when the API call to delete an item is successful.
         *  - Logs the successful deletion.
         *  - Clears the local {@code inventoryItemList}.
         *  - Calls {@link #loadInventoryItems()} to refresh the inventory from the server.
         *  - Displays a success Toast message to the user.
         *  - Inputs:
         *    - {@code result}: A {@link String} from the API, typically a success message.
         *  - Outputs:
         *    - Returns {@code null} (as the Response object is not directly used here).
         *    - Side effects: Modifies {@code inventoryItemList}, reloads items, shows a Toast.
         *
         * {@code onError} method: Documented separately below.
         */
        apiService.deleteInventoryItem(item.getId(), new InventoryApiService.ApiCallback<String>() {
            @Override
            public Response onSuccess(String result) {
                // Log successful deletion
                Log.d("inventoryActivity", "Item with name " + item.getName() + " and quantity " + item.getQuantity() + " deleted successfully");
                
                // Clear existing items
                inventoryItemList.clear();
                
                // Reload items
                loadInventoryItems();
                
                // Show success toast
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                return null;
            }

            /**
             * Callback method invoked when an API call to delete an inventory item fails or returns an error.
             * This method handles the error by displaying an appropriate Toast message to the user
             * and logging the error details.
             *
             * <p>The method differentiates between a "Not Found" error (HTTP status code 404)
             * and other types of errors:
             * <ul>
             *     <li>If the {@code statusCode} is 404, a Toast message "Item not found for deletion."
             *         is shown.</li>
             *     <li>For any other error, a generic Toast message "Error deleting item!" is shown.</li>
             * </ul>
             * In both cases, an error message is logged to the console containing the error
             * description.
             *
             * @param error A {@link String} describing the error that occurred during the deletion
             *              attempt.
             * @param statusCode The HTTP status code associated with the error (e.g., 404 for
             *                   "Not Found", 500 for "Internal Server Error").
             */
            @Override
            public void onError(String error, int statusCode) {
                // Handle not found error
                if (!error.isEmpty() && statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Item not found for deletion.", Toast.LENGTH_SHORT).show();
                    Log.e("InventoryActivity", "Error deleting item: " + error);
                } else {
                    // Handle other errors
                    Log.e("InventoryActivity", "Error deleting item: " + error);
                    Toast.makeText(getApplicationContext(), "Error deleting item!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
